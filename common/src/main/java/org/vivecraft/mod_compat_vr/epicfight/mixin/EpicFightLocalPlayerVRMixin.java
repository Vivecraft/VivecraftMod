package org.vivecraft.mod_compat_vr.epicfight.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;

@ClassDependentMixin("yesman.epicfight.api.client.camera.EpicFightCameraAPI")
// to inject after the epic fight override
@Mixin(value = LocalPlayer.class, priority = 1100)
public class EpicFightLocalPlayerVRMixin {

    @WrapOperation(method = "moveRelative", at = @At(value = "INVOKE", target = "Lyesman/epicfight/api/client/camera/EpicFightCameraAPI;getRelativeMove(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 vivecraft$epicFightFreeMove(
        EpicFightCameraAPI api, Vec3 relative, float amount, Operation<Vec3> original)
    {
        if (!VRState.VR_RUNNING ||
            !(this.getClass().equals(LocalPlayer.class) || Minecraft.getInstance().player == (Object) this))
        {
            return original.call(api, relative, amount);
        } else {
            return ClientDataHolderVR.getInstance().vrPlayer.freemoveDirection(Minecraft.getInstance().player, relative,
                amount);
        }
    }

    @Inject(method = "moveRelative", at = @At(value = "TAIL"))
    protected void vivecraft$epicFightAfterMoveRelative(CallbackInfo ci, @Local(ordinal = 1) Vec3 movement) {
        // do drag after setting the delta movement
        if (VRState.VR_RUNNING &&
            (this.getClass().equals(LocalPlayer.class) || Minecraft.getInstance().player == (Object) this))
        {
            ClientDataHolderVR.getInstance().vrPlayer.applyDrag((LocalPlayer) (Object) this, movement);
        }
    }
}
