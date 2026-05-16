package org.vivecraft.common;

import org.vivecraft.Xloader;

public class CommonDataHolder {

    private static CommonDataHolder INSTANCE = new CommonDataHolder();
    public final String versionIdentifier;

    public CommonDataHolder() {
        // to prevent race conditions
        INSTANCE = this;

        String mcVersion = "";
        String modVersion = "";
        if (Xloader.isModLoadedSuccess()) {
            String[] version = Xloader.getModVersion().split("-", 2);
            mcVersion = version[0];
            modVersion = version[1];
        }

        this.versionIdentifier = "Vivecraft-" + mcVersion + "-" + Xloader.getModloader().name + "-" + modVersion;
    }

    public static CommonDataHolder getInstance() {
        return INSTANCE;
    }
}
