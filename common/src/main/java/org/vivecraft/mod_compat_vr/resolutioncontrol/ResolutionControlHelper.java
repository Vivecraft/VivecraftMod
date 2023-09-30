package org.vivecraft.mod_compat_vr.resolutioncontrol;

import io.github.ultimateboomer.resolutioncontrol.util.Config;
import io.github.ultimateboomer.resolutioncontrol.util.DynamicResolutionHandler;
import org.vivecraft.client.Xplat;

public class ResolutionControlHelper {

    public static boolean isLoaded() {
        return Xplat.isModLoaded("resolutioncontrol");
    }

    public static float getCurrentScaleFactor() {
        return Config.getInstance().enableDynamicResolution ? (float) DynamicResolutionHandler.INSTANCE.getCurrentScale() : Config.getInstance().scaleFactor;
    }
}
