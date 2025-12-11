package org.vivecraft.mixin.client_vr.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import java.util.HashMap;
import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherVRMixin implements EntityRenderDispatcherVRExtension {

    @Unique
    private final Map<PlayerModelType, VRArmRenderer> vivecraft$armSkinMap = new HashMap<>();

    @Shadow
    public Camera camera;

    @Inject(method = "distanceToSqr*", at = @At("HEAD"), cancellable = true)
    private void vivecraft$checkCameraNull(CallbackInfoReturnable<Double> cir) {
        // in case someone wants to get the camera distance, before the camera got set
        if (this.camera == null) {
            cir.setReturnValue(0.0D);
        }
    }

    @Inject(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createAvatarRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", ordinal = 0))
    private void vivecraft$reload(
        ResourceManager resourceManager, CallbackInfo ci, @Local EntityRendererProvider.Context context)
    {
        this.vivecraft$armSkinMap.put(PlayerModelType.WIDE, new VRArmRenderer(context, false));
        this.vivecraft$armSkinMap.put(PlayerModelType.SLIM, new VRArmRenderer(context, true));
    }

    @Inject(method = "extractEntity", at = @At("HEAD"))
    private void vivecraft$storeEntityAndRestorePos(
        CallbackInfoReturnable<EntityRenderState> cir, @Local(argsOnly = true) Entity entity,
        @Share("capturedEntity") LocalRef<Entity> capturedEntity)
    {
        if (!RenderPassType.isVanilla() && this.camera != null && entity == this.camera.entity()) {
            capturedEntity.set(entity);
            ((GameRendererExtension) Minecraft.getInstance().gameRenderer).vivecraft$restoreRVEPos(
                capturedEntity.get());
        }
    }

    @Inject(method = "extractEntity", at = @At("TAIL"))
    private void vivecraft$clearEntityAndSetupPos(
        CallbackInfoReturnable<EntityRenderState> cir, @Local(argsOnly = true) Entity entity,
        @Share("capturedEntity") LocalRef<Entity> capturedEntity)
    {
        if (capturedEntity.get() != null) {
            ((GameRendererExtension) Minecraft.getInstance().gameRenderer).vivecraft$cacheRVEPos(capturedEntity.get());
            ((GameRendererExtension) Minecraft.getInstance().gameRenderer).vivecraft$setupRVE();
        }
    }

    @Override
    @Unique
    public Map<PlayerModelType, VRArmRenderer> vivecraft$getArmSkinMap() {
        return this.vivecraft$armSkinMap;
    }
}
