package org.vivecraft;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class VRMixinConfigPopup {

    public static void askVR(Properties properties, Path file, boolean showConfirmation) throws IOException {
        VRState.isVR = TinyFileDialogs.tinyfd_messageBox("enable VR?", "Would you like to use VR?", "yesno", "info", false);
        VRMixinConfig.asked = true;

        properties.setProperty("vrStatus", String.valueOf(VRState.isVR));

        boolean askAgain = !showConfirmation;
        if (showConfirmation) {
            askAgain = TinyFileDialogs.tinyfd_messageBox("Ask again?", "would you like to be asked on every launch, if you want to use VR?", "yesno", "info", false);
        }

        properties.setProperty("askEveryStartup", String.valueOf(askAgain));
        properties.store(Files.newOutputStream(file), "This file stores if VR should be enabled.");

        if (showConfirmation) {
            TinyFileDialogs.tinyfd_messageBox("VR choice saved", "Your choice has been saved. We will" + (askAgain ? "" : " not") + " ask again on the next startup. To edit it, please go to config/vivecraft-config.properties.", "ok", "info", false);
        }
    }
}