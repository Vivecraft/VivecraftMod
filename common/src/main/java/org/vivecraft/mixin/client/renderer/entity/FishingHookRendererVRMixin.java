package org.vivecraft.mixin.client.renderer.entity;

import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.GameRendererExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FishingHookRenderer.class)
public class FishingHookRendererVRMixin {

    FishingHook myHook;
    float myG;
    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void getFisher(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci){
        myHook = fishingHook;
        myG = g;
    }

    @ModifyArgs(method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/FishingHookRenderer;stringVertex(FFFLcom/mojang/blaze3d/vertex/VertexConsumer;Lcom/mojang/blaze3d/vertex/PoseStack$Pose;FF)V"))
    private void injected(Args args) {
        if(myHook.getPlayerOwner() == Minecraft.getInstance().player) {
            int index = 1;

            if (myHook.getPlayerOwner().getMainHandItem().getItem() instanceof FishingRodItem) {
                index = 0;
            }
            Vec3 vec31 = ((GameRendererExtension)Minecraft.getInstance().gameRenderer).getControllerRenderPos(index);
            Vec3 vec32 = ClientDataHolder.getInstance().vrPlayer.vrdata_world_render.getHand(index).getDirection();

            double o = vec31.x + vec32.x * (double)0.47F;
            double p = vec31.y + vec32.y * (double)0.47F;
            double q = vec31.z + vec32.z * (double)0.47F;

            double s = Mth.lerp((double)myG, myHook.xo, myHook.getX());
            double t = Mth.lerp((double)myG, myHook.yo, myHook.getY()) + 0.25;
            double u = Mth.lerp((double)myG, myHook.zo, myHook.getZ());
            float v = (float)(o - s);
            float w = (float)(p - t);
            float x = (float)(q - u);
            args.set(0, v);
            args.set(1, w);
            args.set(2, x);
        }
    }
}
