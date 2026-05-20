package org.vivecraft.mixin.client_vr.sounds;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.sounds.SoundEngine;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.common.utils.MathUtils;

@Mixin(SoundEngine.class)
public class SoundEngineVRMixin {
    @ModifyExpressionValue(method = "updateSource", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;upVector()Lorg/joml/Vector3fc;"))
    private Vector3fc vivecraft$useHeadUp(Vector3fc original) {
        return VRState.VR_RUNNING ? ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER)
            .getCustomVector(MathUtils.UP) : original;
    }
}
