package org.vivecraft.client_vr.extensions;

import net.minecraft.world.phys.AABB;

public interface FrustumExtension {

    void vivecraft$setCameraPosition(double var1, double var3, double var5);

    boolean vivecraft$isBoundingBoxInFrustum(AABB var1);
}
