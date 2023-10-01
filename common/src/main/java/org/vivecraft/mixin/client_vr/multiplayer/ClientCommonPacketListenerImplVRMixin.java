package org.vivecraft.mixin.client_vr.multiplayer;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.common.VRServerPerms;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplVRMixin {


    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void vivecraft$disconnect(Component component, CallbackInfo ci) {
        VRServerPerms.INSTANCE.setTeleportSupported(false);
        if (VRState.vrInitialized) {
            ClientDataHolderVR.getInstance().vrPlayer.setTeleportOverride(false);
        }
        ClientDataHolderVR.getInstance().vrSettings.overrides.resetAll();
    }
}
