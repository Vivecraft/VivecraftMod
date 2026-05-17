package org.vivecraft.neoforge.mixin;

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
public class NeoForgeMouseHandlerVRMixin {

    // this is stupid, but the locals have differnt ordinals on different modloaders
    @Inject(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getTutorial()Lnet/minecraft/client/tutorial/Tutorial;"))
    private void vivecraft$modifyMouseTravel(
        CallbackInfo ci, @Local(ordinal = 4) LocalDoubleRef forgeX, @Local(ordinal = 5) LocalDoubleRef forgeY)
    {
        if (VRState.VR_RUNNING) {
            Vector2d aimVelocity = MCVR.get().getControllerVelocity();

            forgeX.set(aimVelocity.x);
            forgeY.set(aimVelocity.y);
        }
    }
}
