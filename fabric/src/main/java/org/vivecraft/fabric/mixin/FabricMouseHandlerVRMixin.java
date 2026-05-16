package org.vivecraft.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import net.minecraft.client.MouseHandler;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.provider.MCVR;

@Mixin(MouseHandler.class)
public class FabricMouseHandlerVRMixin {

    // this is stupid, but the locals have differnt ordinals on different modloaders
    @Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getTutorial()Lnet/minecraft/client/tutorial/Tutorial;"))
    private void vivecraft$modifyMouseTravel(
        CallbackInfo ci, @Local(ordinal = 1) LocalDoubleRef fabricX, @Local(ordinal = 2) LocalDoubleRef fabricY)
    {
        if (VRState.VR_RUNNING) {
            Vector2d aimVelocity = MCVR.get().getControllerVelocity();

            fabricX.set(aimVelocity.x);
            fabricY.set(aimVelocity.y);
        }
    }
}
