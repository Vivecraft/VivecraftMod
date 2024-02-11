package org.vivecraft.mod_compat_vr.immersiveportals.mixin;

import net.minecraft.client.Camera;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import qouteall.imm_ptl.core.render.CrossPortalViewRendering;
import qouteall.imm_ptl.core.teleportation.ClientTeleportationManager;

@Mixin(CrossPortalViewRendering.class)
public class CrossPortalViewRenderingMixin {

    @Redirect(method = "renderCrossPortalView", at = @At(value = "INVOKE", target = "Lqouteall/imm_ptl/core/teleportation/ClientTeleportationManager;getPlayerEyePos(F)Lnet/minecraft/world/phys/Vec3;"), remap = false)
    private static Vec3 getPlayerEyePos(float partialTick) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance() != null && VRPlayer.get() != null) {
            return VRPlayer.get().vrdata_world_render.getEye(ClientDataHolderVR.getInstance().currentPass).getPosition();
        }
        return ClientTeleportationManager.getPlayerEyePos(partialTick);
    }

    @Redirect(method = "renderCrossPortalView", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getPosition()Lnet/minecraft/world/phys/Vec3;"), remap = false)
    private static Vec3 getPlayerCameraPos(Camera camera) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance() != null && VRPlayer.get() != null) {
            return VRPlayer.get().vrdata_world_render.getEye(ClientDataHolderVR.getInstance().currentPass).getPosition();
        }
        return camera.getPosition();
    }
}
