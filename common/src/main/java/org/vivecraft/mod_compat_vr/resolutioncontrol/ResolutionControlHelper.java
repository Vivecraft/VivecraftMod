package org.vivecraft.mod_compat_vr.resolutioncontrol;

import org.vivecraft.client.Xplat;

import io.github.ultimateboomer.resolutioncontrol.util.Config;
import io.github.ultimateboomer.resolutioncontrol.util.DynamicResolutionHandler;

public class ResolutionControlHelper {

    public static boolean isLoaded() {
        return Xplat.isModLoaded("resolutioncontrol");
    }

    public static float getCurrentScaleFactor() {
        return Config.getInstance().enableDynamicResolution ? (float)DynamicResolutionHandler.INSTANCE.getCurrentScale() : Config.getInstance().scaleFactor;
    }

}
