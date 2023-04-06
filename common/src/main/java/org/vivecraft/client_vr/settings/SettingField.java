package org.vivecraft.client_vr.settings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SettingField {
	/**
	 * The {@link VRSettings.VrOptions} that this field is associated with.
	 * If left as the default DUMMY, it will function as a hidden setting.
	 *
	 * @return option enum
	 */
	VRSettings.VrOptions value() default VRSettings.VrOptions.DUMMY;

	/**
	 * String to save the option as in the config file, or empty to use the field name.
	 *
	 * @return the config string, or empty
	 */
	String config() default "";

	/**
	 * If true, complex types like arrays, Vector3 and Quaternion will be serialized as
	 * separate keys. Otherwise, they will be serialized as a single delimited string.
	 * Has no effect for irrelevant types.
	 *
	 * @return whether to separate keys
	 */
	boolean separate() default false;
}
