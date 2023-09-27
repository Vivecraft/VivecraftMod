package org.vivecraft.mod_compat_vr.immersiveportals;

import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

public class ImmersivePortalsHelper {
    public static boolean isRenderingPortal() {
        return PortalRendering.isRendering();
    }

    public static boolean shouldRenderSelf() {
        return IPGlobal.renderYourselfInPortal && isRenderingPortal();
    }
}
