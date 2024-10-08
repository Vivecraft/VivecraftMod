package org.vivecraft.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(GameRenderer.class)
public class FabricGameRendererVRMixin {

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 2))
    private void vivecraft$removeMulPoseX(PoseStack instance, Quaternionf quaternion, Operation<Void> original) {
        if (RenderPassType.isVanilla()) {
            original.call(instance, quaternion);
        }
    }

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 3))
    private void vivecraft$removeMulPoseY(PoseStack instance, Quaternionf quaternion, Operation<Void> original) {
        if (RenderPassType.isVanilla()) {
            original.call(instance, quaternion);
        } else {
            RenderHelper.applyVRModelView(ClientDataHolderVR.getInstance().currentPass, instance);
        }
    }

    //optifabric only
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 4), expect = 0)
    private void vivecraft$removeMulPoseZ(PoseStack instance, Quaternionf quaternion, Operation<Void> original) {
        if (RenderPassType.isVanilla()) {
            original.call(instance, quaternion);
        }
    }
}
