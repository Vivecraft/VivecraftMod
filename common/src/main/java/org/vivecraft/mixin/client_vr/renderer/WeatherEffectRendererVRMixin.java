package org.vivecraft.mixin.client_vr.renderer;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(WeatherEffectRenderer.class)
public class WeatherEffectRendererVRMixin {
    @ModifyVariable(method = "extractRenderState", at = @At("HEAD"), argsOnly = true)
    private Vec3 vivecraft$rainCenterPos(Vec3 cameraPos) {
        if (!RenderPassType.isVanilla() && (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT ||
            ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT
        ))
        {
            return ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.hmd.getPosition();
        } else {
            return cameraPos;
        }
    }

    @Inject(method = "renderInstances", at = @At("HEAD"))
    private void vivecraft$centerPos(CallbackInfo ci, @Share("centerPos") LocalRef<Vec3> centerPos) {
        if (!RenderPassType.isVanilla() && (ClientDataHolderVR.getInstance().currentPass == RenderPass.LEFT ||
            ClientDataHolderVR.getInstance().currentPass == RenderPass.RIGHT
        ))
        {
            centerPos.set(ClientDataHolderVR.getInstance().vrPlayer.vrdata_world_render.hmd.getPosition());
        }
    }

    @ModifyArg(method = "renderInstances", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 0))
    private double vivecraft$centerPosZ(double z, @Share("centerPos") LocalRef<Vec3> centerPos) {
        return centerPos.get() != null ? centerPos.get().z : z;
    }

    @ModifyArg(method = "renderInstances", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1))
    private double vivecraft$centerPosX(double x, @Share("centerPos") LocalRef<Vec3> centerPos) {
        return centerPos.get() != null ? centerPos.get().x : x;
    }
}
