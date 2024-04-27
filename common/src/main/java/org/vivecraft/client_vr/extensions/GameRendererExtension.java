package org.vivecraft.client_vr.extensions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public interface GameRendererExtension {

    boolean vivecraft$isInWater();

    boolean vivecraft$wasInWater();

    void vivecraft$setWasInWater(boolean b);

    boolean vivecraft$isOnFire();

    boolean vivecraft$isInPortal();

    float vivecraft$isInBlock();

    void vivecraft$setupRVE();

    void vivecraft$cacheRVEPos(LivingEntity e);

    void vivecraft$restoreRVEPos(LivingEntity e);

    double vivecraft$getRveY();

    Vec3 vivecraft$getRvePos(float partialTicks);

    Vec3 vivecraft$getCrossVec();

    void vivecraft$resetProjectionMatrix(float partialTicks);

    Matrix4f vivecraft$getThirdPassProjectionMatrix();

    void vivecraft$setupClipPlanes();

    float vivecraft$getMinClipDistance();

    float vivecraft$getClipDistance();

    void vivecraft$setShouldDrawScreen(boolean shouldDrawScreen);

    void vivecraft$setShouldDrawGui(boolean shouldDrawGui);
}
