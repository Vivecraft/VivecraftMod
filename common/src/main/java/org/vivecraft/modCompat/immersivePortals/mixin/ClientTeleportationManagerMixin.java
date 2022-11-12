package org.vivecraft.modCompat.immersivePortals.mixin;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.render.RenderPass;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;

@Mixin(ClientTeleportationManager.class)
public class ClientTeleportationManagerMixin {


    @Inject(method = "manageTeleportation", at = @At("HEAD"), cancellable = true, remap = false)
    private void onlyOneTeleport(float tickDelta, CallbackInfo ci){
        if (ClientDataHolder.getInstance().currentPass != RenderPass.LEFT) {
            ci.cancel();
        }
    }

    @Inject(method = "teleportPlayer", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/McHelper;updateBoundingBox(Lnet/minecraft/world/entity/Entity;)V", shift = At.Shift.AFTER), remap = false)
    private void moveRoomOrigin(Portal portal, CallbackInfo ci){
        Vec3 newPos = portal.transformPoint(VRPlayer.get().roomOrigin);
        VRPlayer.get().setRoomOrigin(newPos.x, newPos.y, newPos.z, true);
    }
}
