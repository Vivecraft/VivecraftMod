package org.vivecraft.mod_compat_vr.sodium.mixin;

import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import org.spongepowered.asm.mixin.*;
import org.vivecraft.mod_compat_vr.sodium.extensions.ModelCuboidExtension;

@Mixin(ModelCuboid.class)
public class ModelCuboidMixin implements ModelCuboidExtension {
    @Unique
    private float[][] vivecraft$overrides = null;

    @Override
    public float[][] vivecraft$getOverrides() {
        return vivecraft$overrides;
    }

    @Override
    public void vivecraft$addOverrides(int overrideFaceIndex, int sourceFaceIndex, ModelCuboid source) {
        if (vivecraft$overrides == null) {
            vivecraft$overrides = new float[6][5];
        }
        vivecraft$overrides[overrideFaceIndex][0] = 1F;
        // order taken from me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer.prepareVertices
        switch (sourceFaceIndex) {
            default -> {
                vivecraft$overrides[overrideFaceIndex][1] = source.u1;
                vivecraft$overrides[overrideFaceIndex][2] = source.v0;
                vivecraft$overrides[overrideFaceIndex][3] = source.u2;
                vivecraft$overrides[overrideFaceIndex][4] = source.v1;
            }
            case 1 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source.u2;
                vivecraft$overrides[overrideFaceIndex][2] = source.v1;
                vivecraft$overrides[overrideFaceIndex][3] = source.u3;
                vivecraft$overrides[overrideFaceIndex][4] = source.v0;
            }
            case 2 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source.u1;
                vivecraft$overrides[overrideFaceIndex][2] = source.v1;
                vivecraft$overrides[overrideFaceIndex][3] = source.u2;
                vivecraft$overrides[overrideFaceIndex][4] = source.v2;
            }
            case 3 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source.u4;
                vivecraft$overrides[overrideFaceIndex][2] = source.v1;
                vivecraft$overrides[overrideFaceIndex][3] = source.u5;
                vivecraft$overrides[overrideFaceIndex][4] = source.v2;
            }
            case 4 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source.u2;
                vivecraft$overrides[overrideFaceIndex][2] = source.v1;
                vivecraft$overrides[overrideFaceIndex][3] = source.u4;
                vivecraft$overrides[overrideFaceIndex][4] = source.v2;
            }
            case 5 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source.u0;
                vivecraft$overrides[overrideFaceIndex][2] = source.v1;
                vivecraft$overrides[overrideFaceIndex][3] = source.u1;
                vivecraft$overrides[overrideFaceIndex][4] = source.v2;
            }
        }
    }
}
