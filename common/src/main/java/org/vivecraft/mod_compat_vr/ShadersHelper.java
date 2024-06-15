package org.vivecraft.mod_compat_vr;

import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

public class ShadersHelper {

    public static int ShaderLight() {
        if (isShaderActive()) {
            return 8;
        }
        return 4;
    }

    public static boolean isShaderActive() {
        boolean irisActive = IrisHelper.isIrisLoaded() && IrisHelper.isShaderActive();
        boolean optifineActive = OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive();

        return irisActive || optifineActive;
    }

    public static boolean needsSameSizeBuffers() {
        return OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive();
    }

    public static boolean hasFixesSizeBuffers() {
        return false;
    }

    public static void maybeReloadShaders() {
        if (IrisHelper.isIrisLoaded()) {
            IrisHelper.reload();
        }
    }
}
