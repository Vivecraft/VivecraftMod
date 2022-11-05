package org.vivecraft.modCompat.immersivePortals;

import qouteall.imm_ptl.core.render.context_management.PortalRendering;

public class ImmersivePortalsHelper {
    public static boolean isRenderingPortal(){
        return PortalRendering.isRendering();
    }
}
