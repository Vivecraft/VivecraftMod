package org.vivecraft.common;

import org.vivecraft.client_vr.ClientDataHolderVR;

/**
 * holds permissions of the client
 */
public class VRServerPerms {

    public static VRServerPerms INSTANCE = new VRServerPerms();

    public boolean noTeleportClient = true;

    public void setTeleportSupported(boolean supported) {
        this.noTeleportClient = !supported;

        if (ClientDataHolderVR.getInstance().vrPlayer != null) {
            ClientDataHolderVR.getInstance().vrPlayer.updateTeleportKeys();
        }
    }
}
