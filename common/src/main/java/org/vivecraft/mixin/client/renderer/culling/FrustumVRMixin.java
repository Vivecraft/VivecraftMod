package org.vivecraft.mixin.client.renderer.culling;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client_vr.extensions.FrustumExtension;

@Mixin(Frustum.class)
public abstract class FrustumVRMixin implements FrustumExtension {

    @Shadow
    private double camX;

    @Shadow
    private double camY;

    @Shadow
    private double camZ;

    @Shadow
    protected abstract boolean cubeInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ);

    @Override
    @Unique
    public void vivecraft$setCameraPosition(double var1, double var3, double var5) {
        this.camX = var1;
        this.camY = var3;
        this.camZ = var5;
    }

    @Override
    @Unique
    public boolean vivecraft$isBoundingBoxInFrustum(AABB var1) {
        return this.cubeInFrustum(var1.minX, var1.minY, var1.minZ, var1.maxX, var1.maxY, var1.maxZ);
    }
}
