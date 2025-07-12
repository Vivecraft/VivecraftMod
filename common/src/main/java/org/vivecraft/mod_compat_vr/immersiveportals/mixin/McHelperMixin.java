package org.vivecraft.mod_compat_vr.immersiveportals.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import qouteall.imm_ptl.core.McHelper;

@Mixin(McHelper.class)
public class McHelperMixin {

    @WrapMethod(method = "setEyePos")
    private static void vivecraft$adjustRoomOriginOnEyePosUpdate(Entity entity, Vec3 eyePos, Vec3 lastTickEyePos, Operation<Void> original) {
        if (entity instanceof Player p && p.isLocalPlayer() && VRPlayer.get() != null) {
            // Move the room origin after the player portal teleport to be in the same position relative to the player
            // as before
            Vec3 oldPos = entity.position();
            Vec3 oldRoomOrigin = VRPlayer.get().roomOrigin;
            Vec3 offset = oldRoomOrigin.subtract(oldPos);
            original.call(entity, eyePos, lastTickEyePos);
            Vec3 newPos = entity.position();
            Vec3 newRoomOrigin = newPos.add(offset);
            VRPlayer.get().setRoomOrigin(newRoomOrigin.x, newRoomOrigin.y, newRoomOrigin.z, true);
        } else {
            original.call(entity, eyePos, lastTickEyePos);
        }
    }
}
