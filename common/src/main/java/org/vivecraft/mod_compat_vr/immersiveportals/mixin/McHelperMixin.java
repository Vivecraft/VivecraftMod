package org.vivecraft.mod_compat_vr.immersiveportals.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import qouteall.imm_ptl.core.McHelper;

@Mixin(McHelper.class)
public class McHelperMixin {

    @Inject(at = @At("HEAD"), method = "updateBoundingBox")
    private static void updateBoundingBox(Entity player, CallbackInfo ci) {
        if (VRPlayer.get() != null) {
            Vec3 newPos = player.position();
            Vec3 offset = newPos.subtract(VRPlayer.get().roomOrigin);
            VRPlayer.get().vrdata_world_render.origin = VRPlayer.get().vrdata_world_render.origin.add(offset);
            VRPlayer.get().vrdata_world_pre.origin = VRPlayer.get().vrdata_world_pre.origin.add(offset);
            VRPlayer.get().vrdata_world_post.origin = VRPlayer.get().vrdata_world_post.origin.add(offset);
            VRPlayer.get().setRoomOrigin(newPos.x, newPos.y, newPos.z, true);
        }
    }

}
