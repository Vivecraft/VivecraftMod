package org.vivecraft.common;

import org.vivecraft.client.Xplat;

public class CommonDataHolder {

    private static CommonDataHolder INSTANCE;
    public final String versionIdentifier;

    public CommonDataHolder() {
        String mcVersion = "";
        String modVersion = "";
        if (Xplat.isModLoadedSuccess()) {
            String[] version = Xplat.getModVersion().split("-", 2);
            mcVersion = version[0];
            modVersion = version[1];
        }

        versionIdentifier = "Vivecraft-" + mcVersion + "-" + Xplat.getModloader().name + "-" + modVersion;
    }

    public static CommonDataHolder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CommonDataHolder();
        }
        return INSTANCE;
    }
}
