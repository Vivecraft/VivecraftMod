package org.vivecraft.mixin.client_vr.renderer.entity;

import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(net.minecraft.client.renderer.entity.FishingHookRenderer.class)
public abstract class FishingHookRendererVRMixin extends net.minecraft.client.renderer.entity.EntityRenderer<FishingHook> {

    // dummy constructor
    protected FishingHookRendererVRMixin(Context context) {
        super(context);
    }

    private Vec3 CachedHandPos;

    @ModifyVariable(at = @At("LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 25)
    private double fishingLineStartX(double value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == mc.player) {
            int j = 1;
            if (fishingHook.getPlayerOwner().getMainHandItem().getItem() instanceof FishingRodItem) {
                j = 0;
            }
            Vec3 vec31 = ((GameRendererExtension) mc.gameRenderer).getControllerRenderPos(j);
            Vec3 vec32 = dh.vrPlayer.vrdata_world_render.getHand(j).getDirection();
            this.CachedHandPos = vec31.add(vec32.scale(0.47 * dh.vrPlayer.vrdata_world_render.worldScale));
            return this.CachedHandPos.x;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At("LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 27)
    private double fishingLineStartY(double value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == mc.player) {
            return this.CachedHandPos.y;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At("LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 29)
    private double fishingLineStartZ(double value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == mc.player) {
            return this.CachedHandPos.z;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At("LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 31)
    private float fishingLineStartOffset(float value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == mc.player) {
            return 0.0F;
        } else {
            return value;
        }
    }
}
