package org.vivecraft.mixin.client_vr.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(EntityRenderer.class)
public class EntityRendererVRMixin {

    @WrapOperation(method = "renderNameTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lorg/joml/Quaternionf;"))
    private Quaternionf vivecraft$cameraOffset(EntityRenderDispatcher instance, Operation<Quaternionf> original) {
        if (RenderPassType.isWorldOnly()) {
            return ((EntityRenderDispatcherVRExtension) instance).vivecraft$getVRCameraOrientation(1.0F, 0.5F);
        } else {
            return original.call(instance);
        }
    }
}
