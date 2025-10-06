package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.mod_compat_vr.create.CreateHelper;

@Mixin(KeyboardInput.class)
public class KeyboardInputVRMixin extends Input {

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/KeyboardInput;calculateImpulse(ZZ)F", ordinal = 0))
    private void vivecraft$noMovementWhenClimbing(CallbackInfo ci, @Share("climbing") LocalBooleanRef climbing) {
        if (VRState.VR_RUNNING) {
            climbing.set(Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isInWater() &&
                ClientDataHolderVR.getInstance().climbTracker.isClimbeyClimb() &&
                ClientDataHolderVR.getInstance().climbTracker.isGrabbingLadder());

            this.up = (this.up || VivecraftVRMod.INSTANCE.keyTeleportFallback.isDown()) && !climbing.get();
            this.down &= !climbing.get();
            this.left &= !climbing.get();
            this.right &= !climbing.get();
        }
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/KeyboardInput;shiftKeyDown:Z", shift = At.Shift.AFTER))
    private void vivecraft$analogInput(
        CallbackInfo ci, @Local(argsOnly = true) boolean isSneaking, @Share("climbing") LocalBooleanRef climbing)
    {
        if (!VRState.VR_RUNNING) return;

        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        this.jumping &= Minecraft.getInstance().screen == null && !climbing.get() &&
            (dataHolder.vrPlayer.getFreeMove() || dataHolder.vrSettings.simulateFalling);

        this.shiftKeyDown = Minecraft.getInstance().screen == null &&
            (dataHolder.sneakTracker.sneakCounter > 0 || dataHolder.sneakTracker.sneakOverride || this.shiftKeyDown);
        // we only need to set it when using analog input
        if (MCVR.get().isMovement && ClientDataHolderVR.getInstance().vrSettings.analogMovement &&
            !(CreateHelper.isLoaded() && CreateHelper.blocksMovement()))
        {
            this.forwardImpulse = MCVR.get().movement.y;
            this.leftImpulse = -MCVR.get().movement.x;
        }
    }
}
