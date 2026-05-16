package org.vivecraft.forge.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.VRPassHelper;

@Mixin(Minecraft.class)
public class ForgeMinecraftVRMixin {

    @Shadow
    @Final
    private DeltaTracker.Timer deltaTracker;

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/event/ForgeEventFactoryClient;onRenderTickEnd(Lnet/minecraft/client/DeltaTracker;)V", shift = At.Shift.AFTER, remap = false), remap = true)
    private void vivecraft$renderVRPassesForge(boolean renderLevel, CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            VRPassHelper.renderAndSubmit(renderLevel, this.deltaTracker);
        }
    }
}
