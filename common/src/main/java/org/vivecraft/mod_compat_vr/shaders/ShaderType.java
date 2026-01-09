package org.vivecraft.mod_compat_vr.shaders;

public enum ShaderType {
    BASIC_COLOR("BASIC"),
    ENTITIES_SOLID("ENTITIES"),
    ENTITIES_CUTOUT("ENTITIES"),
    ENTITIES_TRANSLUCENT("ENTITIES_TRANSLUCENT");

    public final String irisFallback;

    ShaderType(String irisFallback) {
        this.irisFallback = irisFallback;
    }
}
