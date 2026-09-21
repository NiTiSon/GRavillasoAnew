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
}