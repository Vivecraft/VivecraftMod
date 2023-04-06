package org.vivecraft.client;

import org.vivecraft.common.NonVRMixinConfig;

public class VRState {
    public static boolean isVR;

    public static boolean checkVR() {
        NonVRMixinConfig.classLoad();
        return isVR;
    }
}
