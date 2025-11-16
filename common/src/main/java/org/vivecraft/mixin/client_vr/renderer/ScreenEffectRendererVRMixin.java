package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.common.utils.MathUtils;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererVRMixin {

    @WrapOperation(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void vivecraft$noTranslateItemInVR(
        PoseStack instance, float x, float y, float z, Operation<Void> original)
    {
        if (RenderPassType.isVanilla()) {
            original.call(instance, x, y, z);
        } else {
            // negate head rotation
            instance.mulPose(RenderHelper.getVRModelView(ClientDataHolderVR.getInstance().currentPass).invert());
        }
    }

    @WrapOperation(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void vivecraft$noScaleItem(
        PoseStack poseStack, float x, float y, float z, Operation<Void> original, @Local(ordinal = 5) float progress)
    {
        if (RenderPassType.isVanilla()) {
            original.call(poseStack, x, y, z);
        } else {
            float sinProgress = Mth.sin(progress) * 0.5F;
            poseStack.translate(0.0F, 0.0F, sinProgress - 1.0F);
            RenderPass pass = ClientDataHolderVR.getInstance().currentPass;
            if (pass == RenderPass.THIRD) {
                // make the item the same size, independent of FOV
                sinProgress *= ClientDataHolderVR.getInstance().vrSettings.mixedRealityFov / 70.0F;
            } else if (pass == RenderPass.CENTER) {
                // make the item the same size, independent of FOV
                sinProgress *= Minecraft.getInstance().options.fov().get() / 70.0F;
            } else if (pass == RenderPass.LEFT || pass == RenderPass.RIGHT) {
                // apply stereo offset, but screen relative, not world
                VRData data = ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld();
                Vector3f offset = MathUtils.subtractToVector3f(data.getEye(pass).getPosition(),
                    data.getEye(RenderPass.CENTER).getPosition());
                data.getEye(RenderPass.CENTER).getMatrix().invert().transformPosition(offset);
                poseStack.translate(-offset.x, -offset.y, -offset.z);
            }

            // call the scale with original to allow operation stacking
            original.call(poseStack, sinProgress, sinProgress, sinProgress);
        }
    }

    @ModifyArg(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Lighting;setupFor(Lcom/mojang/blaze3d/platform/Lighting$Entry;)V"))
    private Lighting.Entry vivecraft$worldLighting(Lighting.Entry entry) {
        if (!RenderPassType.isVanilla()) {
            return Lighting.Entry.LEVEL;
        }
        return entry;
    }
}
