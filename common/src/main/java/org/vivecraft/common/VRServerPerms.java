package org.vivecraft.common;

import static org.vivecraft.client_vr.VRState.dh;

public class VRServerPerms {

    public static boolean noTeleportClient = true;

    public static void setTeleportSupported(boolean supported)
    {
        noTeleportClient = !supported;

        if (dh.vrPlayer != null) {
            dh.vrPlayer.updateTeleportKeys();
        }
    }
}
