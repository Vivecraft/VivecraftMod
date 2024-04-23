package org.vivecraft.client_vr.extensions;

import net.minecraft.world.phys.AABB;

public interface FrustumExtension {

    void vivecraft$setCameraPosition(double camX, double camY, double camZ);

    boolean vivecraft$isBoundingBoxInFrustum(AABB bb);
}
