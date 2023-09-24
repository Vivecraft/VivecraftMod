package org.vivecraft.mod_compat_vr.resolutioncontrol.mixin;

import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrRunning;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(ResolutionControlMod.class)
public class ResolutionControlModMixin {

    @Inject(at = @At("HEAD"), method = "setShouldScale", remap = false, cancellable = true)
    private void dontResizeGUI(boolean shouldScale, CallbackInfo ci) {
        // we handle the resize ourselves
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "updateFramebufferSize", remap = false)
    private void resizeVRBuffers(CallbackInfo ci) {
        if (vrRunning) {
            dh.vrRenderer.resizeFrameBuffers("");
        }
    }

    @Inject(at = @At("HEAD"), method = "getCurrentWidth", remap = false, cancellable = true)
    public void  getVRWidth(CallbackInfoReturnable<Integer> cir) {
        if (vrRunning) {
            cir.setReturnValue(WorldRenderPass.stereoXR.target.width);
        }
    }

    @Inject(at = @At("HEAD"), method = "getCurrentHeight", remap = false, cancellable = true)
    public void  getVRHeight(CallbackInfoReturnable<Integer> cir) {
        if (vrRunning) {
            cir.setReturnValue(WorldRenderPass.stereoXR.target.height);
        }
    }
}
