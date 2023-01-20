package org.vivecraft.mixin.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.projectile.FishingHook;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.GameRendererExtension;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererVRMixin extends EntityRenderer<FishingHook> {

    // dummy constructor
    protected FishingHookRendererVRMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    private FishingHook currentlyRenderingFishingHook;
    private Vec3 CachedHandPos;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void storeFishingHook(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
        currentlyRenderingFishingHook = fishingHook;
    }

    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 25)
    private double fishingLineStartX(double value) {
        if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && currentlyRenderingFishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            int j = 1;
            if (currentlyRenderingFishingHook.getPlayerOwner().getMainHandItem().getItem() instanceof FishingRodItem) {
                j = 0;
            }
            Vec3 vec31 = ((GameRendererExtension) Minecraft.getInstance().gameRenderer).getControllerRenderPos(j);
            Vec3 vec32 = ClientDataHolder.getInstance().vrPlayer.vrdata_world_render.getHand(j).getDirection();
            CachedHandPos = vec31.add(vec32.scale(0.47 * ClientDataHolder.getInstance().vrPlayer.vrdata_world_render.worldScale));
            return CachedHandPos.x;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 27)
    private double fishingLineStartY(double value) {
        if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && currentlyRenderingFishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            return CachedHandPos.y;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 29)
    private double fishingLineStartZ(double value) {
        if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && currentlyRenderingFishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            return CachedHandPos.z;
        } else {
            return value;
        }
    }
    @ModifyVariable(at = @At(value = "LOAD"),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 31)
    private float fishingLineStartOffset(float value) {
        if ((this.entityRenderDispatcher.options == null || this.entityRenderDispatcher.options.getCameraType().isFirstPerson()) && currentlyRenderingFishingHook.getPlayerOwner() == Minecraft.getInstance().player) {
            return 0.0F;
        } else {
            return value;
        }
    }
}
