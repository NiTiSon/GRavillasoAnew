package nitis.gravillaso.annotations.processors.impl;

import com.squareup.javapoet.*;
import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.annotations.processors.*;

import javax.lang.model.element.*;
import java.util.*;

import static nitis.gravillaso.annotations.processors.BaseProcessor.*;

/**
 * Emits, for every {@link Remote @Remote} method, a packet class plus a matching send method in a
 * generated {@code <classPrefix>Call} class, in the same shape as Mindustry's own generated
 * {@code mindustry.gen.Call}/{@code *CallPacket} (verified against v160.5 bytecode).
 *
 * Serializers resolve from {@link TypeIOHandler handler} classes first, then the baked-in
 * {@link mindustry.io.TypeIO} methods; entities fall back to {@code TypeIO.writeEntity/readEntity},
 * primitives are inlined.
 */
public class CallGenerator{

	private static final String entityRead = "mindustry.io.TypeIO.readEntity";
	private static final String entityWrite = "mindustry.io.TypeIO.writeEntity";
	private static final ClassName packetBase = ClassName.get("mindustry.net", "Packet");
	private static final ClassName netClass = ClassName.get("mindustry.net", "Net");
	private static final ClassName netConnection = ClassName.get("mindustry.net", "NetConnection");

	public static void generate(BaseProcessor processor, Map<String, String> writers, Map<String, String> readers, List<MethodEntry> methods, String callClassName) throws Exception{
		TypeSpec.Builder call = TypeSpec.classBuilder(callClassName).addModifiers(Modifier.PUBLIC);
		MethodSpec.Builder register = MethodSpec.methodBuilder("registerPackets").addModifiers(Modifier.PUBLIC, Modifier.STATIC);

		for(MethodEntry ent : methods){
			List<? extends VariableElement> params = ent.element.getParameters();
			TypeSpec.Builder packet = TypeSpec.classBuilder(ent.packetName)
				.addModifiers(Modifier.PUBLIC)
				.superclass(packetBase)
				.addField(FieldSpec.builder(TypeName.get(byte[].class), "DATA", Modifier.PRIVATE).initializer("NODATA").build());

			if(ent.priority != PacketPriority.normal){
				packet.addMethod(MethodSpec.methodBuilder("getPriority")
					.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).returns(int.class)
					.addStatement("return $L", ent.priority.ordinal()).build());
			}

			makeWriter(packet, ent, writers, params.size());
			makeReader(packet, ent, readers, params.size());

			//remote execution happens on the opposite side of the invocation source
			if(ent.where.isClient){
				packet.addMethod(handleServer(ent));
			}
			if(ent.where.isServer){
				packet.addMethod(handleClient(ent));
			}

			if(!(ent.where.isClient && ent.where.isServer)){
				packet.addMethod(MethodSpec.methodBuilder("allow")
					.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
					.addParameter(boolean.class, "server").returns(boolean.class)
					.addStatement("return $L", ent.where.isClient ? "server" : "!server").build());
			}

			register.addStatement("$T.registerPacket($L.$L::new)", netClass, packageName, ent.packetName);

			for(int i = 0; i < params.size(); i++){
				if(isSkippedOnSource(ent, i)) continue;
				packet.addField(FieldSpec.builder(TypeName.get(params.get(i).asType()), params.get(i).getSimpleName().toString(), Modifier.PUBLIC).build());
			}

			if(ent.where.isClient || ent.variant.isAll) writeCallMethod(call, ent, true, false);
			if(ent.where.isServer && ent.variant.isOne) writeCallMethod(call, ent, false, false);
			if(ent.where.isServer && ent.forward) writeCallMethod(call, ent, true, true);

			//packet is a same-package class, just importing it
			JavaFile.builder(packageName, packet.build()).skipJavaLangImports(true).indent("    ").build().writeTo(processor.filer);
		}

		call.addMethod(register.build());
		processor.write(call.build());
	}

	private static void makeWriter(TypeSpec.Builder packet, MethodEntry ent, Map<String, String> writers, int size){
		MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
			.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
			.addParameter(ClassName.get("arc.util.io", "Writes"), "WRITE");

		for(int i = 0; i < size; i++){
			if(isSkippedOnSource(ent, i)) continue;

			VariableElement var = ent.element.getParameters().get(i);
			String name = var.getSimpleName().toString();
			String type = var.asType().toString();

			if(isServerWriter(ent, i)){
				builder.beginControlFlow("if($L)", "mindustry.Vars.net.server()");
			}

			if(isPrimitive(type)){
				builder.addStatement("WRITE.$L($L)", ioName(type), name);
			}else{
				String ser = writerFor(type, writers);
				builder.addStatement("$L(WRITE, $L)", ser, name);
			}

			if(isServerWriter(ent, i)){
				builder.endControlFlow();
			}
		}

		packet.addMethod(builder.build());
	}

	private static void makeReader(TypeSpec.Builder packet, MethodEntry ent, Map<String, String> readers, int size){
		packet.addMethod(MethodSpec.methodBuilder("read")
			.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
			.addParameter(ClassName.get("arc.util.io", "Reads"), "READ")
			.addParameter(int.class, "LENGTH")
			.addStatement("DATA = READ.b(LENGTH)").build());

		MethodSpec.Builder builder = MethodSpec.methodBuilder("handled")
			.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
			.addStatement("BAIS.setBytes(DATA)");

		for(int i = 0; i < size; i++){
			if(isSkippedOnSource(ent, i)) continue;

			VariableElement var = ent.element.getParameters().get(i);
			String name = var.getSimpleName().toString();
			String type = var.asType().toString();

			if(isClientReader(ent, i)){
				builder.beginControlFlow("if($L)", "mindustry.Vars.net.client()");
			}

			if(isPrimitive(type)){
				builder.addStatement("$L = READ.$L()", name, ioName(type));
			}else{
				builder.addStatement("$L = $L(READ)", name, readerFor(type, readers));
			}

			if(isClientReader(ent, i)){
				builder.endControlFlow();
			}
		}

		packet.addMethod(builder.build());
	}

	/** Runs when a client-originated packet arrives on the server. First param is served from the connection, not the packet. */
	private static MethodSpec handleServer(MethodEntry ent){
		List<? extends VariableElement> params = ent.element.getParameters();

		MethodSpec.Builder builder = MethodSpec.methodBuilder("handleServer")
			.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
			.addParameter(netConnection, "con")
			.beginControlFlow("if(con.player == null || con.kicked)")
			.addStatement("return")
			.endControlFlow()
			.addStatement("$L player = con.player", "mindustry.gen.Player")
			.addStatement("$L.$L($L)", ent.enclosing, ent.element.getSimpleName(), conArgs(params));

		if(ent.forward && ent.where.isServer){
			builder.addStatement("$L.$L.$L__forward(con, $L)", packageName, ent.callClassName, ent.element.getSimpleName(), conArgs(params));
		}

		return builder.build();
	}

	/** First argument is always the connection-owned player. */
	private static String conArgs(List<? extends VariableElement> params){
		StringBuilder out = new StringBuilder("player");
		for(int i = 1; i < params.size(); i++){
			out.append(", ").append(params.get(i).getSimpleName());
		}
		return out.toString();
	}

	/** Runs when a server-originated packet arrives on the client. */
	private static MethodSpec handleClient(MethodEntry ent){
		return MethodSpec.methodBuilder("handleClient")
			.addAnnotation(Override.class).addModifiers(Modifier.PUBLIC)
			.addStatement("$L.$L($L)", ent.enclosing, ent.element.getSimpleName(), args(ent.element.getParameters()))
			.build();
	}

	/** A send variant: to all/one player, optionally the forwarded server re-broadcast. */
	private static void writeCallMethod(TypeSpec.Builder call, MethodEntry ent, boolean toAll, boolean forwarded){
		String name = ent.element.getSimpleName().toString() + (forwarded ? "__forward" : "");
		MethodSpec.Builder method = MethodSpec.methodBuilder(name)
			.addModifiers(Modifier.STATIC)
			.returns(void.class);
		if(!forwarded){
			method.addModifiers(Modifier.PUBLIC);
		}
		if(!toAll){
			method.addParameter(netConnection, "playerConnection");
		}
		if(forwarded){
			method.addParameter(netConnection, "exceptConnection");
		}

		List<? extends VariableElement> params = ent.element.getParameters();

		//invoke locally if requested, so the sender's own game also runs the method
		if(!forwarded && ent.local != Loc.none){
			if(ent.local != Loc.both){
				method.beginControlFlow("if($L || !$L)", check(ent.local), "mindustry.Vars.net.active()");
			}
			method.addStatement("$L.$L($L)", ent.enclosing, ent.element.getSimpleName(), localArgs(ent, params));
			if(ent.local != Loc.both){
				method.endControlFlow();
			}
		}

		method.beginControlFlow("if($L)", check(ent.where));
		method.addStatement("$T packet = new $T()", ClassName.get(packageName, ent.packetName), ClassName.get(packageName, ent.packetName));

		for(int i = 0; i < params.size(); i++){
			if(isSkippedOnSource(ent, i)) continue;

			VariableElement var = params.get(i);
			String varName = var.getSimpleName().toString();
			method.addParameter(TypeName.get(var.asType()), varName);

			if(isServerWriter(ent, i)){
				method.beginControlFlow("if($L)", "mindustry.Vars.net.server()");
			}
			method.addStatement("packet.$L = $L", varName, varName);
			if(isServerWriter(ent, i)){
				method.endControlFlow();
			}
		}

		String send;
		if(forwarded){
			send = ent.local.isClient ? "mindustry.Vars.net.sendExcept(exceptConnection, " : "mindustry.Vars.net.send(";
		}else if(toAll){
			send = "mindustry.Vars.net.send(";
		}else{
			send = "playerConnection.send(";
		}

		method.addStatement("$Lpacket, $L)", send, !ent.unreliable);
		method.endControlFlow();

		call.addMethod(method.build());
	}

	private static String check(Loc loc){
		return loc.isClient && loc.isServer ? "mindustry.Vars.net.server() || mindustry.Vars.net.client()"
			: loc.isClient ? "mindustry.Vars.net.client()"
			: loc.isServer ? "mindustry.Vars.net.server()" : "false";
	}

	/** Player param is served from the connection on the server and sent by the server, never by the client. */
	private static boolean isSkippedOnSource(MethodEntry ent, int i){
		return !ent.where.isServer && i == 0;
	}

	private static boolean isServerWriter(MethodEntry ent, int i){
		return ent.where == Loc.both && i == 0;
	}

	private static boolean isClientReader(MethodEntry ent, int i){
		return ent.where == Loc.both && i == 0;
	}

	private static String args(List<? extends VariableElement> params){
		StringBuilder out = new StringBuilder();
		for(VariableElement v : params){
			if(out.length() > 0) out.append(", ");
			out.append(v.getSimpleName());
		}
		return out.toString();
	}

	private static String localArgs(MethodEntry ent, List<? extends VariableElement> params){
		StringBuilder out = new StringBuilder();
		for(int i = 0; i < params.size(); i++){
			if(out.length() > 0) out.append(", ");
			if(i == 0 && ent.where == Loc.client){
				out.append("mindustry.Vars.player");
			}else{
				out.append(params.get(i).getSimpleName());
			}
		}
		return out.toString();
	}

	private static boolean isPrimitive(String type){
		return switch(type){
			case "byte", "boolean", "short", "int", "long", "float", "double", "char" -> true;
			default -> false;
		};
	}

	private static String ioName(String type){
		return type.equals("boolean") ? "bool" : type.charAt(0) + "";
	}

	private static String writerFor(String type, Map<String, String> writers){
		String ser = writers.get(key(type));
		if(ser == null && isEntity(type)){
			ser = entityWrite;
		}
		if(ser == null){
			throw new RuntimeException("No method to write class type '" + type + "'; add a @TypeIOHandler method or pass an entity.");
		}
		return ser;
	}

	private static String readerFor(String type, Map<String, String> readers){
		String ser = readers.get(key(type));
		if(ser == null && isEntity(type)){
			ser = entityRead;
		}
		if(ser == null){
			throw new RuntimeException("No method to read class type '" + type + "'; add a @TypeIOHandler method or pass an entity.");
		}
		return ser;
	}

	/** Serializer lookup key, identical to the one used when building the maps: vanilla strips its own gen package. */
	public static String key(String type){
		return type.replace("mindustry.gen", "").replace(packageName, "");
	}

	/** Package-less or generated types are assumed to be entities (serialized by class ID). */
	public static boolean isEntity(String type){
		return (!type.contains(".") && !type.startsWith("byte")) || type.startsWith("mindustry.gen.") || type.startsWith(packageName + ".");
	}

	/** One annotated @Remote method, its generation params and metadata. */
	public static class MethodEntry{
		public final String enclosing, callClassName, packetName;
		public final Loc where, local;
		public final Variant variant;
		public final boolean unreliable, forward;
		public final PacketPriority priority;
		public final ExecutableElement element;

		public MethodEntry(String enclosing, String callClassName, String packetName, Loc where, Variant variant, Loc local,
			boolean unreliable, boolean forward, PacketPriority priority, ExecutableElement element){
			this.enclosing = enclosing;
			this.callClassName = callClassName;
			this.packetName = packetName;
			this.where = where;
			this.variant = variant;
			this.local = local;
			this.unreliable = unreliable;
			this.forward = forward;
			this.priority = priority;
			this.element = element;
		}
	}
}