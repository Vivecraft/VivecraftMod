package org.vivecraft.forge.mixin;

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

@Mixin(value = Minecraft.class)
public class ForgeMinecraftVRMixin {

    @Shadow
    @Final
    private DeltaTracker.Timer timer;

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/event/ForgeEventFactoryClient;onRenderTickEnd(Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER), method = "runTick", locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$renderVRPassesForge(boolean renderLevel, CallbackInfo ci) {
        if (VRState.vrRunning) {
            VRPassHelper.renderAndSubmit(renderLevel, this.timer);
        }
    }
}
