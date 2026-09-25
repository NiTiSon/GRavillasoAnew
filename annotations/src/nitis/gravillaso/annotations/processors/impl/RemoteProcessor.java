package nitis.gravillaso.annotations.processors.impl;

import nitis.gravillaso.annotations.Annotations.*;
import nitis.gravillaso.annotations.processors.*;
import nitis.gravillaso.annotations.processors.impl.CallGenerator.*;

import javax.annotation.processing.*;
import javax.lang.model.element.*;
import java.util.*;

/**
 * Generates client/server sync code for methods annotated with {@link Remote @Remote}: a packet
 * class per method plus a send API in a generated {@code <classPrefix>Call} class, mirroring the
 * vanilla {@code Call.*} pattern.
 *
 * Also picks up {@link TypeIOHandler @TypeIOHandler} classes so custom types can be serialized;
 * the baked-in {@code mindustry.io.TypeIO} serializers are used automatically.
 */
public class RemoteProcessor extends BaseProcessor{
	{
		rounds = 1;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes(){
		return new HashSet<>(List.of(
			Remote.class.getCanonicalName(),
			TypeIOHandler.class.getCanonicalName()
		));
	}

	@Override
	public void process(RoundEnvironment roundEnv) throws Exception{
		Map<String, String> writers = new HashMap<>();
		Map<String, String> readers = new HashMap<>();

		//mod-supplied serializers win over the baked-in vanilla TypeIO methods
		for(Element element : roundEnv.getElementsAnnotatedWith(TypeIOHandler.class)){
			if(element instanceof TypeElement type){
				scan(type, type.getQualifiedName().toString(), writers, readers);
			}
		}

		TypeElement vanillaIo = elements.getTypeElement("mindustry.io.TypeIO");
		if(vanillaIo != null){
			scan(vanillaIo, "mindustry.io.TypeIO", writers, readers);
		}

		List<MethodEntry> methods = new ArrayList<>();
		Set<String> packetNames = new HashSet<>();

		for(Element element : roundEnv.getElementsAnnotatedWith(Remote.class)){
			if(!(element instanceof ExecutableElement method)){
				continue;
			}

			Remote annotation = method.getAnnotation(Remote.class);
			String name = method.getSimpleName().toString();
			List<? extends VariableElement> params = method.getParameters();

			if(!method.getModifiers().containsAll(List.of(Modifier.STATIC, Modifier.PUBLIC))){
				throw new RuntimeException("@Remote method '" + name + "' must be public and static");
			}
			if(annotation.targets() == Loc.none){
				throw new RuntimeException("@Remote method '" + name + "' cannot have targets() = none");
			}
			if(!method.getTypeParameters().isEmpty()){
				throw new RuntimeException("@Remote method '" + name + "' cannot be generic");
			}
			if(annotation.targets().isClient){
				if(params.isEmpty() || !params.get(0).asType().toString().contains("Player")){
					throw new RuntimeException("@Remote method '" + name + "' with a client target must have a first parameter of type Player");
				}
			}

			String packetName = Character.toUpperCase(name.charAt(0)) + name.substring(1) + "CallPacket";
			if(packetNames.contains(packetName)){
				int index = 2;
				while(packetNames.contains(packetName + index)){
					index++;
				}
				packetName += index;
			}
			packetNames.add(packetName);

			methods.add(new MethodEntry(
				((TypeElement)method.getEnclosingElement()).getQualifiedName().toString(),
				classPrefix + "Call",
				packetName,
				annotation.targets(),
				annotation.variants(),
				annotation.called(),
				annotation.unreliable(),
				annotation.forward(),
				annotation.priority(),
				method
			));
		}

		//always generate the call class; registerPackets() stays valid even with no @Remote methods
		CallGenerator.generate(this, writers, readers, methods, classPrefix + "Call");
	}

	/** Mirrors Mindustry's TypeIOResolver: writers are {@code writeX(Writes, T)}, readers {@code readX(Reads) -> T}; {@code *Net} variants win. */
	private void scan(TypeElement type, String prefix, Map<String, String> writers, Map<String, String> readers){
		Map<String, String> netWriters = new HashMap<>();
		Map<String, String> netReaders = new HashMap<>();

		for(Element el : type.getEnclosedElements()){
			if(!(el instanceof ExecutableElement method) || !method.getModifiers().containsAll(List.of(Modifier.STATIC, Modifier.PUBLIC))){
				continue;
			}

			List<? extends VariableElement> params = method.getParameters();
			String name = method.getSimpleName().toString();
			String fqn = prefix + "." + name;

			if(params.size() == 2 && params.get(0).asType().toString().equals("arc.util.io.Writes")){
				if(name.endsWith("Net")){
					netWriters.putIfAbsent(CallGenerator.key(params.get(1).asType().toString()), fqn);
				}else{
					writers.putIfAbsent(CallGenerator.key(params.get(1).asType().toString()), fqn);
				}
			}else if(params.size() == 1 && params.get(0).asType().toString().equals("arc.util.io.Reads") && !method.getReturnType().toString().equals("void")){
				if(name.endsWith("Net")){
					netReaders.putIfAbsent(CallGenerator.key(method.getReturnType().toString()), fqn);
				}else{
					readers.putIfAbsent(CallGenerator.key(method.getReturnType().toString()), fqn);
				}
			}
		}

		//net variants always win, matching getNetWriter/getNetReader semantics
		writers.putAll(netWriters);
		readers.putAll(netReaders);
	}
}