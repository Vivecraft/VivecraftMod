package org.vivecraft;

public class CommonDataHolder {

    private static CommonDataHolder INSTANCE;
    public boolean resourcePacksChanged;
    public final String minecriftVerString;

    //TODO add loader name
    public CommonDataHolder() {
        if (VRState.checkVR()) {
            minecriftVerString = "Vivecraft 1.18.2 jrbudda-VR-fabric-a1";
        } else {
            minecriftVerString = "Vivecraft 1.18.2 jrbudda-NONVR-fabric-a1";
        }
    }

    public static CommonDataHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommonDataHolder();
        }
        return INSTANCE;
    }
}
