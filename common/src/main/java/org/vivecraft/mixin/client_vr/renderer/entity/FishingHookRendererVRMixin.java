package org.vivecraft.mixin.client_vr.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererVRMixin extends EntityRenderer<FishingHook> {

    // dummy constructor
    protected FishingHookRendererVRMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    private Vec3 CachedHandPos;

    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 25)
    private double fishingLineStartX(double value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            int j = 1;
            if (fishingHook.getPlayerOwner().getMainHandItem().getItem() instanceof FishingRodItem) {
                j = 0;
            }
            Vec3 vec31 = ((GameRendererExtension) Minecraft.getInstance().gameRenderer).getControllerRenderPos(j);
            Vec3 vec32 = ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.getHand(j).getDirection();
            CachedHandPos = vec31.add(vec32.scale(0.47 * ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.worldScale));
            return CachedHandPos.x;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 27)
    private double fishingLineStartY(double value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            return CachedHandPos.y;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 29)
    private double fishingLineStartZ(double value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            return CachedHandPos.z;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 31)
    private float fishingLineStartOffset(float value, FishingHook fishingHook) {
        if (!RenderPassType.isVanilla() && (this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && fishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            return 0.0F;
        } else {
            return value;
        }
    }
}
