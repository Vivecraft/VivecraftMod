package org.vivecraft.client_vr.extensions;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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

    Vector3f vivecraft$getRvePos(float partialTicks, Vector3f dest);

    boolean vivecraft$isInMenuRoom();

    boolean vivecraft$willBeInMenuRoom(Screen newScreen);

    Vector3f vivecraft$getCrossVec(Vector3f dest);

    void vivecraft$resetProjectionMatrix(float partialTicks);

    Matrix4f vivecraft$getThirdPassProjectionMatrix();

    void vivecraft$setupClipPlanes();

    float vivecraft$getMinClipDistance();

    float vivecraft$getClipDistance();

    void vivecraft$setShouldDrawScreen(boolean shouldDrawScreen);

    void vivecraft$setShouldDrawGui(boolean shouldDrawGui);
}
