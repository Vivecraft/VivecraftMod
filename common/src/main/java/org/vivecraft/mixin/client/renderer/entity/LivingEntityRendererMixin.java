package org.vivecraft.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {

    @Shadow
    protected M model;

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "addLayer", at = @At("HEAD"))
    protected void vivecraft$onAddLayer(RenderLayer<S, M> renderLayer, CallbackInfoReturnable<Boolean> cir) {}

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void vivecraft$vrPlayerHeightScale(
        CallbackInfo ci, @Local(argsOnly = true) LivingEntityRenderState renderState,
        @Local(argsOnly = true) PoseStack poseStack)
    {
        // PoseStack is already pushed before this
        // need to do this here, because doing it in the VRPlayerRenderer doesn't work,
        // because forge/neoforge override the render method, and arch can't link that correctly
        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) renderState).vivecraft$getRotInfo();
        if (rotInfo != null) {
            float scale = rotInfo.heightScale;
            if (((EntityRenderStateExtension) renderState).vivecraft$isFirstPersonPlayer() ||
                ClientDataHolderVR.getInstance().vrSettings.applyPlayerWorldscale)
            {
                // remove entity scale, since the entity is already scaled by that before
                scale *= rotInfo.worldScale / ((EntityRenderStateExtension) renderState).vivecraft$getTotalScale();
            }

            if (renderState.isAutoSpinAttack) {
                // offset player to head
                float offset = renderState.xRot / 90F * 0.2F;
                poseStack.translate(0, rotInfo.headPos.y() + offset, 0);
            }

            poseStack.scale(scale, scale, scale);
        }
    }
}
