package org.vivecraft.neoforge.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Timer;
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
    private Timer timer;

    @Shadow
    private volatile boolean pause;

    @Shadow
    private float pausePartialTick;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/event/EventHooks;onRenderTickEnd(F)V", shift = At.Shift.AFTER), method = "runTick", locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$renderVRPassesNeoForge(boolean renderLevel, CallbackInfo ci, long nanoTime) {
        if (VRState.vrRunning) {
            VRPassHelper.renderAndSubmit(renderLevel, nanoTime, this.pause ? this.pausePartialTick : this.timer.partialTick);
        }
    }
}
