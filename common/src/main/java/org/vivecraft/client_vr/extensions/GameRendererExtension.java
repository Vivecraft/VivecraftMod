package org.vivecraft.client_vr.extensions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public interface GameRendererExtension {

    boolean vivecraft$isInWater();

    boolean vivecraft$wasInWater();

    void vivecraft$setWasInWater(boolean b);

    boolean vivecraft$isInPortal();

    float vivecraft$isInBlock();

    void vivecraft$setupRVE();

    void vivecraft$cacheRVEPos(LivingEntity entity);

    void vivecraft$restoreRVEPos(LivingEntity entity);

    double vivecraft$getRveY();

    Vec3 vivecraft$getRvePos(float partialTick);

    Vec3 vivecraft$getCrossVec();

    void vivecraft$resetProjectionMatrix(float partialTick);

    Matrix4f vivecraft$getThirdPassProjectionMatrix();

    void vivecraft$setupClipPlanes();

    float vivecraft$getMinClipDistance();

    float vivecraft$getClipDistance();

    void vivecraft$setShouldDrawScreen(boolean shouldDrawScreen);

    void vivecraft$setShouldDrawGui(boolean shouldDrawGui);
}
