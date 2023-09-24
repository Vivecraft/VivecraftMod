package org.vivecraft.mod_compat_vr.iris.mixin;

import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import net.coderbot.iris.mixin.LevelRendererAccessor;
import net.coderbot.iris.pipeline.ShadowRenderer;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(ShadowRenderer.class)
public class IrisShadowRendererMixin {
    @Inject(method = "renderPlayerEntity", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/mixin/LevelRendererAccessor;invokeRenderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", ordinal = 2))
    private void setRVE(LevelRendererAccessor par1, EntityRenderDispatcher par2, BufferSource par3, PoseStack par4, float par5, Frustum par6, double par7, double par8, double par9, CallbackInfoReturnable<Integer> cir) {
        if (!RenderPassType.isVanilla()) {
            ((GameRendererExtension) mc.gameRenderer).restoreRVEPos(mc.player);
        }
    }

    @Inject(method = "renderPlayerEntity", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/mixin/LevelRendererAccessor;invokeRenderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", ordinal = 2, shift = Shift.AFTER))
    private void resetRVE(LevelRendererAccessor par1, EntityRenderDispatcher par2, BufferSource par3, PoseStack par4, float par5, Frustum par6, double par7, double par8, double par9, CallbackInfoReturnable<Integer> cir) {
        if (!RenderPassType.isVanilla()) {
            ((GameRendererExtension) mc.gameRenderer).cacheRVEPos(mc.player);
            ((GameRendererExtension) mc.gameRenderer).setupRVE();
        }
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/mixin/LevelRendererAccessor;invokeRenderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private void setRVE2(LevelRendererAccessor instance, Entity entity, double x, double y, double z, float f, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        if (!RenderPassType.isVanilla()) {
            if (entity == mc.getCameraEntity()) {
                ((GameRendererExtension) mc.gameRenderer).restoreRVEPos((LivingEntity) entity);
            }

            instance.invokeRenderEntity(entity, x, y, z, f, poseStack, multiBufferSource);

            if (entity == mc.getCameraEntity()) {
                ((GameRendererExtension) mc.gameRenderer).cacheRVEPos((LivingEntity) entity);
                ((GameRendererExtension) mc.gameRenderer).setupRVE();
            }
        } else {
            instance.invokeRenderEntity(entity, x, y, z, f, poseStack, multiBufferSource);
        }
    }

    // only render shadows on the first RenderPass
    // cancel them here, or we would also cancel prepare shaders
    @Inject(method = "renderShadows", at = @At("HEAD"), cancellable = true)
    private void onlyOneShadow(LevelRendererAccessor par1, Camera par2, CallbackInfo ci) {
        if (!RenderPassType.isVanilla() && !dh.isFirstPass) {
            ci.cancel();
        }
    }
}
