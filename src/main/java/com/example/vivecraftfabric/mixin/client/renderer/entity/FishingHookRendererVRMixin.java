package com.example.vivecraftfabric.mixin.client.renderer.entity;

import com.example.vivecraftfabric.DataHolder;
import com.example.vivecraftfabric.GameRendererExtension;
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

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getEyeHeight()F"), method = "render(Lnet/minecraft/world/entity/projectile/FishingHook;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", locals = LocalCapture.CAPTURE_FAILHARD)
    public void updateX(FishingHook fishingHook, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci, Player player, PoseStack.Pose pose, Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, int j, ItemStack itemStack, float h, float k, float l, double d, double e, double m, double n, double o, double p, double q, float r) {

        int index = 1;

        if (player.getMainHandItem().getItem() instanceof FishingRodItem) {
            index = 0;
        }
        Vec3 vec31 = ((GameRendererExtension)Minecraft.getInstance().gameRenderer).getControllerRenderPos(index);
        Vec3 vec32 = DataHolder.getInstance().vrPlayer.vrdata_world_render.getHand(index).getDirection();

        o = vec31.x + vec32.x * (double)0.47F;
        p = vec31.y + vec32.y * (double)0.47F;
        q = vec31.z + vec32.z * (double)0.47F;
        r = 0.0F;
    }
}
