package org.vivecraft;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class VRMixinConfigPopup {

    public static void askVR(Properties properties, Path file) throws IOException {
        VRState.isVR = TinyFileDialogs.tinyfd_messageBox("VR", "Would you like to use VR?", "yesno", "info", false);
        VRMixinConfig.asked = true;

        properties.setProperty("vrStatus", String.valueOf(VRState.isVR));
        properties.store(Files.newOutputStream(file), "This file stores if VR should be enabled.");
        TinyFileDialogs.tinyfd_messageBox("VR", "Your choice has been saved. To edit it, please go to config/vivecraft-config.properties.", "ok", "info", false);
    }
}
