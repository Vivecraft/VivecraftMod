package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.entity.player.Input;
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
public class KeyboardInputVRMixin extends ClientInput {

    @WrapOperation(method = "tick", at = @At(value = "NEW", target = "net/minecraft/world/entity/player/Input"))
    private Input vivecraft$noMovementWhenClimbing(
        boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean shift, boolean sprint,
        Operation<Input> original, @Share("climbing") LocalBooleanRef climbing)
    {
        if (VRState.VR_RUNNING) {
            climbing.set(Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isInWater() &&
                ClientDataHolderVR.getInstance().climbTracker.isClimbeyClimb() &&
                ClientDataHolderVR.getInstance().climbTracker.isGrabbingLadder());

            forward = (forward || VivecraftVRMod.INSTANCE.keyTeleportFallback.isDown()) && !climbing.get();
            backward &= !climbing.get();
            left &= !climbing.get();
            right &= !climbing.get();

            ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

            jump &= Minecraft.getInstance().screen == null && !climbing.get() &&
                (dataHolder.vrPlayer.getFreeMove() || dataHolder.vrSettings.simulateFalling);

            shift = Minecraft.getInstance().screen == null &&
                (dataHolder.sneakTracker.sneakCounter > 0 || dataHolder.sneakTracker.sneakOverride || shift);
        }
        return original.call(forward, backward, left, right, jump, shift, sprint);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void vivecraft$analogInput(CallbackInfo ci, @Share("climbing") LocalBooleanRef climbing) {
        if (!VRState.VR_RUNNING) return;

        // we only need to set it when using analog input
        if (MCVR.get().isMovement && ClientDataHolderVR.getInstance().vrSettings.analogMovement &&
            !(CreateHelper.isLoaded() && CreateHelper.blocksMovement()))
        {
            this.forwardImpulse = MCVR.get().movement.y;
            this.leftImpulse = -MCVR.get().movement.x;
        }
    }
}
