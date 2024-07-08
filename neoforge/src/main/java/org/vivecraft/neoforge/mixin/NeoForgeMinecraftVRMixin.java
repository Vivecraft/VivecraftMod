package org.vivecraft.neoforge.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.VRPassHelper;

@Mixin(Minecraft.class)
public class NeoForgeMinecraftVRMixin {

    @Shadow
    @Final
    private DeltaTracker.Timer timer;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;fireRenderFramePost(Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER), method = "runTick", locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$renderVRPassesNeoForge(boolean renderLevel, CallbackInfo ci) {
        if (VRState.vrRunning) {
            VRPassHelper.renderAndSubmit(renderLevel, timer);
        }
    }
}
