package nitis.gravillaso.annotations;

import java.lang.annotation.*;

@SuppressWarnings("ALL")
public class Annotations{
	//region load
	/**
	 * Fields annotated with this will be put on the ContentRegionRegistry, separated by enclosing class.
	 * Names are parsed based on the following:
	 * <ul>
	 *   <p> @ -> mapped., mapped is the enclosing class already properly casted
	 *   <p> % -> mod name
	 *   <p> # -> INDEX, regular prefix for array variables
	 *   <p> $ -> terminator, marks the end of a variable
	 * </ul>
	 * Examples: %-glow -> <modname>-glow, @size$ -> mapped.size
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Load{
		/** Name used by the region. */
		String value();

		/** Array lengths. One value for each dimension. */
		int[] lengths() default {};

		/** Name used by the region if {@link #value()} returns error. */
		String fallBack() default "error";

		/** When true, finds the region named {@link #value()} or {@link #fallBack()}, then splits it by the integers in {@link #width()} and {@link #height()}. */
		boolean splits() default false;

		/** Width and height variable names for true {@link #splits()} annotated regions. */
		String width() default "";

		String height() default "";
	}

	/** Ensures that the ContentRegionRegistry is generated. */
	@Retention(RetentionPolicy.SOURCE)
	public @interface EnsureLoad{}
	//endregion

	//region entity
	/**
	 * Declares an entity definition for a content field. Generates an entity class
	 * extending the matching vanilla base (e.g. {@code LegsUnit} for {@code Legsc}) into the gen package,
	 * registered with a valid class ID in {@link mindustry.gen.EntityMapping} at class load.
	 * Assign it as the unit's {@code constructor} when the field is created.
	 */
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.SOURCE)
	public @interface EntityDef{
		/** List of component interfaces (e.g. {@code Unitc.class, Legsc.class}). */
		Class[] value();
	}
	//endregion

	//region net
	public enum PacketPriority{
		/** Does not get handled unless client is connected. */
		low,
		/** Gets put in a queue and processed if not connected. */
		normal,
		/** Gets handled immediately, regardless of connection status. */
		high,
	}

	/** A set of two booleans, one specifying server and one specifying client. */
	public enum Loc{
		/** Server side. */
		server(true, false),
		/** Client side. */
		client(false, true),
		/** Both server and client. */
		both(true, true),
		/** Neither server nor client. */
		none(false, false);

		public final boolean isServer;
		public final boolean isClient;

		Loc(boolean server, boolean client){
			this.isServer = server;
			this.isClient = client;
		}
	}

	public enum Variant{
		/** Method can only be invoked targeting one player. */
		one(true, false),
		/** Method can only be invoked targeting all players. */
		all(false, true),
		/** Method targets both one player and all players. */
		both(true, true);

		public final boolean isOne, isAll;

		Variant(boolean one, boolean all){
			this.isOne = one;
			this.isAll = all;
		}
	}

	/**
	 * Marks a public static method as invokable remotely across a server/client connection.
	 * Generates a packet class and a {@code <classPrefix>Call} class (in the gen package) with
	 * send methods mirroring vanilla {@code Call.*}. Call {@code <classPrefix>Call.registerPackets()}
	 * once at mod init.
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.SOURCE)
	public @interface Remote{
		/** Specifies the locations from which this method can cause remote invocations (This -> Remote) [Default: Server -> Client]. */
		Loc targets() default Loc.server;

		/** Specifies which methods are generated. Only affects server-to-client methods (Server -> Client(s)) [Default: Server -> Client & Server -> All Clients]. */
		Variant variants() default Variant.all;

		/** The locations where this method is called locally, when invoked locally (This -> This) [Default: No local invocations]. */
		Loc called() default Loc.none;

		/** Whether the server should forward this packet to all other clients upon receival from a client (Client -> Server -> Other Clients). [Default: Don't Forward Client Invocations] */
		boolean forward() default false;

		/** Whether the packet for this method is sent with UDP instead of TCP. */
		boolean unreliable() default false;

		/** Priority of this event. */
		PacketPriority priority() default PacketPriority.normal;
	}

	/** Marks a class whose public static {@code write(Writes, T)} / {@code read(Reads)} methods serialize types for {@link Remote remote call} packets. */
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.SOURCE)
	public @interface TypeIOHandler{}
	//endregion
}