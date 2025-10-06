package org.vivecraft.mod_compat_vr.immersiveportals;

import org.vivecraft.Xloader;
import qouteall.imm_ptl.core.IPGlobal;
import qouteall.imm_ptl.core.render.context_management.PortalRendering;

public class ImmersivePortalsHelper {

    public static boolean isLoaded() {
        return Xloader.isModLoaded("immersive_portals");
    }

    /**
     * @return if the renderpass is for a portal
     */
    public static boolean isRenderingPortal() {
        return PortalRendering.isRendering();
    }

    /**
     * @return if the player should be rendered in a portal
     */
    public static boolean shouldRenderSelf() {
        return IPGlobal.renderYourselfInPortal && isRenderingPortal();
    }
}
