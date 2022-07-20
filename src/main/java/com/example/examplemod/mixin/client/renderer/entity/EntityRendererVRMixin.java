package com.example.examplemod.mixin.client.renderer.entity;

import com.example.examplemod.EntityRenderDispatcherExtension;
import com.mojang.math.Quaternion;
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

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lcom/mojang/math/Quaternion;"), method = "renderNameTag")
    public Quaternion cameraOffset(EntityRenderDispatcher instance) {
        return ((EntityRenderDispatcherExtension)this.entityRenderDispatcher).getCameraOrientationOffset(0.5f);
    }
}
