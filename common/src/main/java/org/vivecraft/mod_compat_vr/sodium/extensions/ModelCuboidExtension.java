package org.vivecraft.mod_compat_vr.sodium.extensions;

import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;

public interface ModelCuboidExtension {

    float[][] vivecraft$getOverrides();

    void vivecraft$addOverrides(int overrideFaceIndex, int sourceFaceIndex, ModelCuboid source);
}
