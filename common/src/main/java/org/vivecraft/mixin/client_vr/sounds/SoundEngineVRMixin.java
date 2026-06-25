package org.vivecraft.mixin.client_vr.sounds;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.math.Vector3f;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.common.utils.MathUtils;

@Mixin(SoundEngine.class)
public class SoundEngineVRMixin {

    @ModifyExpressionValue(method = "updateSource", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getLookVector()Lcom/mojang/math/Vector3f;"))
    private Vector3f vivecraft$useHeadForward(Vector3f original) {
        return VRState.VR_RUNNING ?
            MathUtils.toMcVector3f(ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER)
                .getDirection()) : original;
    }

    @ModifyExpressionValue(method = "updateSource", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getUpVector()Lcom/mojang/math/Vector3f;"))
    private Vector3f vivecraft$useHeadUp(Vector3f original) {
        return VRState.VR_RUNNING ?
            MathUtils.toMcVector3f(ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER)
                .getCustomVector(MathUtils.UP)) : original;
    }
}
