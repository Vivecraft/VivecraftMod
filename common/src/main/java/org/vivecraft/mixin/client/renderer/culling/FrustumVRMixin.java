package org.vivecraft.mixin.client.renderer.culling;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.joml.FrustumIntersection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client_vr.extensions.FrustumExtension;

@Mixin(Frustum.class)
public abstract class FrustumVRMixin implements FrustumExtension {

    @Shadow
    private int cubeInFrustum(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        throw new AssertionError();
    }

    @Override
    @Unique
    public boolean vivecraft$isBoundingBoxInFrustum(AABB bb) {
        int result = this.cubeInFrustum(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        return result == FrustumIntersection.INTERSECT || result == FrustumIntersection.INSIDE;
    }
}
