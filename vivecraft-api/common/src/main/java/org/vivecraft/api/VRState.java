package org.vivecraft.api;

public class VRState {
    public static boolean isVR;

    public static boolean checkVR() {
        NonVRMixinConfig.classLoad();
        return isVR;
    }
}
