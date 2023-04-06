package org.vivecraft.mixin.client_vr.renderer.entity;

import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;
import org.joml.Quaternionf;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class EntityRendererVRMixin {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lorg/joml/Quaternionf;"), method = "renderNameTag")
    public Quaternionf cameraOffset(EntityRenderDispatcher instance) {
        return ((EntityRenderDispatcherVRExtension)this.entityRenderDispatcher).getCameraOrientationOffset(0.5f);
    }
}
