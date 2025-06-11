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
        return this.vivecraft$overrides;
    }

    @Override
    public void vivecraft$addOverrides(int overrideFaceIndex, int sourceFaceIndex, float[][] source) {
        if (this.vivecraft$overrides == null) {
            this.vivecraft$overrides = new float[6][5];
        }
        this.vivecraft$overrides[overrideFaceIndex][0] = 1F;
        // order taken from me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer.prepareVertices
        switch (sourceFaceIndex) {
            case 1 -> {
                this.vivecraft$overrides[overrideFaceIndex][1] = source[0][2];
                this.vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                this.vivecraft$overrides[overrideFaceIndex][3] = source[0][3];
                this.vivecraft$overrides[overrideFaceIndex][4] = source[1][0];
            }
            case 2 -> {
                this.vivecraft$overrides[overrideFaceIndex][1] = source[0][1];
                this.vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                this.vivecraft$overrides[overrideFaceIndex][3] = source[0][2];
                this.vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 3 -> {
                this.vivecraft$overrides[overrideFaceIndex][1] = source[0][4];
                this.vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                this.vivecraft$overrides[overrideFaceIndex][3] = source[0][5];
                this.vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 4 -> {
                this.vivecraft$overrides[overrideFaceIndex][1] = source[0][2];
                this.vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                this.vivecraft$overrides[overrideFaceIndex][3] = source[0][4];
                this.vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 5 -> {
                this.vivecraft$overrides[overrideFaceIndex][1] = source[0][0];
                this.vivecraft$overrides[overrideFaceIndex][2] = source[1][1];
                this.vivecraft$overrides[overrideFaceIndex][3] = source[0][1];
                this.vivecraft$overrides[overrideFaceIndex][4] = source[1][2];
            }
            // 0 case
            default -> {
                this.vivecraft$overrides[overrideFaceIndex][1] = source[0][1];
                this.vivecraft$overrides[overrideFaceIndex][2] = source[1][0];
                this.vivecraft$overrides[overrideFaceIndex][3] = source[0][2];
                this.vivecraft$overrides[overrideFaceIndex][4] = source[1][1];
            }
        }
    }
}
