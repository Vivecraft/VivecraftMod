package org.vivecraft.modCompatMixin.irisMixin;

import jdk.jfr.Percentage;
import org.spongepowered.asm.mixin.Pseudo;
import org.vivecraft.extensions.GameRendererExtension;
import com.mojang.blaze3d.vertex.PoseStack;
import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.ShadowRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(ShadowRenderer.class)
public class IrisShadowRendererMixin {
    @Inject(method = "renderPlayerEntity", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/mixin/LevelRendererAccessor;invokeRenderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", ordinal = 2))
    private void setRVE(LevelRendererAccessor par1, EntityRenderDispatcher par2, MultiBufferSource.BufferSource par3, PoseStack par4, float par5, Frustum par6, double par7, double par8, double par9, CallbackInfoReturnable<Integer> cir) {
        ((GameRendererExtension) Minecraft.getInstance().gameRenderer).restoreRVEPos(Minecraft.getInstance().player);
    }

    @Inject(method = "renderPlayerEntity", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/mixin/LevelRendererAccessor;invokeRenderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", ordinal = 2, shift = At.Shift.AFTER))
    private void resetRVE(LevelRendererAccessor par1, EntityRenderDispatcher par2, MultiBufferSource.BufferSource par3, PoseStack par4, float par5, Frustum par6, double par7, double par8, double par9, CallbackInfoReturnable<Integer> cir) {
        ((GameRendererExtension) Minecraft.getInstance().gameRenderer).cacheRVEPos(Minecraft.getInstance().player);
        ((GameRendererExtension) Minecraft.getInstance().gameRenderer).setupRVE();
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/mixin/LevelRendererAccessor;invokeRenderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private void setRVE2(LevelRendererAccessor instance, Entity entity, double x, double y, double z, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        if (entity == Minecraft.getInstance().getCameraEntity()) {
            ((GameRendererExtension) Minecraft.getInstance().gameRenderer).restoreRVEPos((LivingEntity) entity);
        }

        instance.invokeRenderEntity(entity, x, y, z, f, poseStack, multiBufferSource);

        if (entity == Minecraft.getInstance().getCameraEntity()) {
            ((GameRendererExtension) Minecraft.getInstance().gameRenderer).cacheRVEPos((LivingEntity) entity);
            ((GameRendererExtension) Minecraft.getInstance().gameRenderer).setupRVE();
        }
    }
}
