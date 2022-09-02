package org.vivecraft;

public class CommonDataHolder {

    private static CommonDataHolder INSTANCE;
    public boolean resourcePacksChanged;
    public final String minecriftVerString;

    public CommonDataHolder() {
        if (VRState.checkVR()) {
            minecriftVerString = "Vivecraft 1.18.2 jrbudda-VR-" + Xplat.getModloader() + "-a1";
        } else {
            minecriftVerString = "Vivecraft 1.18.2 jrbudda-NONVR-" + Xplat.getModloader() + "-a1";
        }
    }

    public static CommonDataHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommonDataHolder();
        }
        return INSTANCE;
    }
}
