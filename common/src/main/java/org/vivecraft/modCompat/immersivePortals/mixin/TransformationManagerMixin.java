package org.vivecraft.modCompat.immersivePortals.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.ClientDataHolder;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.render.TransformationManager;
import qouteall.q_misc_util.my_util.DQuaternion;

@Mixin(TransformationManager.class)
public class TransformationManagerMixin {

    @Inject(method = "managePlayerRotationAndChangeGravity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V", shift = At.Shift.BEFORE), remap = false, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void rotateRoom(Portal portal, CallbackInfo ci, LocalPlayer player, Direction oldGravityDir, DQuaternion oldCameraRotation, DQuaternion currentCameraRotationInterpolated, DQuaternion cameraRotationThroughPortal, Direction newGravityDir, DQuaternion newExtraCameraRot, DQuaternion newCameraRotationWithNormalGravity, Tuple pitchYaw, float finalYaw){
        ClientDataHolder.getInstance().vrSettings.worldRotation += player.getYRot() - finalYaw;
        ClientDataHolder.getInstance().vrSettings.worldRotation %= 360.0F;
        ClientDataHolder.getInstance().vr.seatedRot = ClientDataHolder.getInstance().vrSettings.worldRotation;
    }
}
