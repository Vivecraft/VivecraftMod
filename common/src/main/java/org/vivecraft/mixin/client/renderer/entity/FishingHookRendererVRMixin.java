package org.vivecraft.mixin.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
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
public class FishingHookRendererVRMixin {

    private FishingHook currentlyRenderingFishingHook;
    private Vec3 CachedHandPos;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void storeFishingHook(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
        currentlyRenderingFishingHook = fishingHook;
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 1),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 25)
    private double fishingLineStartX(double value) {
        int j = 1;
        if (currentlyRenderingFishingHook.getPlayerOwner().getMainHandItem().getItem() instanceof FishingRodItem)
        {
            j = 0;
        }
        Vec3 vec31 = ((GameRendererExtension) Minecraft.getInstance().gameRenderer).getControllerRenderPos(j);
        Vec3 vec32 = ClientDataHolder.getInstance().vrPlayer.vrdata_world_render.getHand(j).getDirection();
        CachedHandPos = vec31.add(vec32.multiply(0.47, 0.47, 0.47));
        return CachedHandPos.x;
    }
    @ModifyVariable(at = @At(value = "STORE", ordinal = 1),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 27)
    private double fishingLineStartY(double value) {
        return CachedHandPos.y;
    }
    @ModifyVariable(at = @At(value = "STORE", ordinal = 1),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 29)
    private double fishingLineStartZ(double value) {
        return CachedHandPos.z;
    }
    @ModifyVariable(at = @At(value = "STORE", ordinal = 1),
            method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", index = 31)
    private float fishingLineStartOffset(float value) {
        return 0.0F;
    }
}
