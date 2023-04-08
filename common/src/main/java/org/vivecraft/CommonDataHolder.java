package org.vivecraft;

public class CommonDataHolder {

    private static CommonDataHolder INSTANCE;
    public boolean resourcePacksChanged;
    public final String minecriftVerString;

    public CommonDataHolder() {
        String mcVersion = "";
        String modVersion = "";
        if (Xplat.isModLoadedSuccess()) {
            String[] version = Xplat.getModVersion().split("-", 2);
            mcVersion = version[0];
            modVersion = version[1];
        }

        if (VRState.checkVR()) {
            minecriftVerString = "Vivecraft " + mcVersion + " VR-" + Xplat.getModloader() + "-" + modVersion;
        } else {
            minecriftVerString = "Vivecraft " + mcVersion + " NONVR-" + Xplat.getModloader() + "-" + modVersion;
        }
    }

    public static CommonDataHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommonDataHolder();
        }
        return INSTANCE;
    }
}
