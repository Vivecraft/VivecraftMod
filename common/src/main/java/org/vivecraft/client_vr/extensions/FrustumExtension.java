package org.vivecraft.client_vr.extensions;

import net.minecraft.world.phys.AABB;

public interface FrustumExtension {

    /**
     * checks if {@code bb} is partially or fully in the frustum
     *
     * @param bb bounding box to check
     * @return if the AABB is partially or fully in the frustum
     */
    boolean vivecraft$isBoundingBoxInFrustum(AABB bb);
}
