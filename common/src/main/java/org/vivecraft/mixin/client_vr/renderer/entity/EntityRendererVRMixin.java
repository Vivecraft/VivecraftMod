package org.vivecraft.mixin.client_vr.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;

@Mixin(EntityRenderer.class)
public class EntityRendererVRMixin {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lorg/joml/Quaternionf;"), method = "renderNameTag")
    public Quaternionf vivecraft$cameraOffset(EntityRenderDispatcher instance) {
        return ((EntityRenderDispatcherVRExtension) this.entityRenderDispatcher).vivecraft$getCameraOrientationOffset(0.5f);
    }
}
