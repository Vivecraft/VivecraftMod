package org.vivecraft.client_vr.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mixins annotated with this class only apply if the
 * given Class is present.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClassDependentMixin {
    /**
     * The classname of the class that should be present to
     * load the mixin annotated with this Interface.
     *
     * @return The String classname.
     */
    String value();
}
