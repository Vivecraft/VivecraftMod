package org.vivecraft.mod_compat_vr.resolutioncontrol;

import io.github.ultimateboomer.resolutioncontrol.ResolutionControlMod;
import org.vivecraft.client.Xplat;

public class ResolutionControlHelper {

    public static boolean isLoaded() {
        return Xplat.isModLoaded("resolutioncontrol");
    }

    /**
     * @return current render scale
     */
    public static float getCurrentScaleFactor() {
        return (float) ResolutionControlMod.getInstance().getCurrentScaleFactor();
    }
}
