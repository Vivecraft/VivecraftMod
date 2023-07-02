package org.vivecraft.client_vr.extensions;

import net.minecraft.world.phys.AABB;

public interface FrustumExtension {

    public void setCameraPosition(double var1, double var3, double var5);

    public boolean isBoundingBoxInFrustum(AABB var1);
}
