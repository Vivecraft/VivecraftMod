package org.vivecraft.common;

import org.vivecraft.Services;

public class CommonDataHolder {

    private static CommonDataHolder INSTANCE = new CommonDataHolder();
    public final String versionIdentifier;

    public CommonDataHolder() {
        // to prevent race conditions
        INSTANCE = this;

        String mcVersion = "";
        String modVersion = "";
        if (Services.XLOADER.isModLoadedSuccess()) {
            String[] version = Services.XLOADER.getModVersion().split("-", 2);
            mcVersion = version[0];
            modVersion = version[1];
        }

        this.versionIdentifier =
            "Vivecraft-" + mcVersion + "-" + Services.XLOADER.getModloader().name + "-" + modVersion;
    }

    public static CommonDataHolder getInstance() {
        return INSTANCE;
    }
}
