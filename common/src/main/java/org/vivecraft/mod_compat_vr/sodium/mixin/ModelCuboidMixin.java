package org.vivecraft.mod_compat_vr.sodium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.mod_compat_vr.sodium.extensions.ModelCuboidExtension;

@Pseudo
@Mixin(targets = {
    "me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid",
    "net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid"})
public class ModelCuboidMixin implements ModelCuboidExtension {
    @Unique
    private float[][] vivecraft$overrides = null;

    @Override
    public float[][] vivecraft$getOverrides() {
        return vivecraft$overrides;
    }

    @Override
    public void vivecraft$addOverrides(int overrideFaceIndex, int sourceFaceIndex, float[][] source) {
        if (vivecraft$overrides == null) {
            vivecraft$overrides = new float[6][5];
        }
        vivecraft$overrides[overrideFaceIndex][0] = 1F;
        // order taken from me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer.prepareVertices
        switch (sourceFaceIndex) {
            default -> {
                vivecraft$overrides[overrideFaceIndex][1] = source[0][1];
                vivecraft$overrides[overrideFaceIndex][2] = source[1][0];
                vivecraft$overrides[overrideFaceIndex][3] = source[0][2];
                vivecraft$overrides[overrideFaceIndex][4] = source[1][1];
            }
            case 1 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source[0][2];
                vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                vivecraft$overrides[overrideFaceIndex][3] = source[0][3];
                vivecraft$overrides[overrideFaceIndex][4] = source[1][0];
            }
            case 2 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source[0][1];
                vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                vivecraft$overrides[overrideFaceIndex][3] = source[0][2];
                vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 3 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source[0][4];
                vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                vivecraft$overrides[overrideFaceIndex][3] = source[0][5];
                vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 4 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source[0][2];
                vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                vivecraft$overrides[overrideFaceIndex][3] = source[0][4];
                vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 5 -> {
                vivecraft$overrides[overrideFaceIndex][1] = source[0][0];
                vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                vivecraft$overrides[overrideFaceIndex][3] = source[0][1];
                vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
        }
    }
}
