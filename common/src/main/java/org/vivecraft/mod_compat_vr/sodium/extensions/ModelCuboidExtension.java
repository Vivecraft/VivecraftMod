package org.vivecraft.mod_compat_vr.sodium.extensions;

public interface ModelCuboidExtension {

    float[][] vivecraft$getOverrides();

    void vivecraft$addOverrides(int overrideFaceIndex, int sourceFaceIndex, float[][] source);
}
