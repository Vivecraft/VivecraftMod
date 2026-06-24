package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiRadialSettings extends GuiVROptionsBase {
    private static final VROptionEntry[] SETTINGS = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.RADIAL_MODE_HOLD),
        new VROptionEntry(VRSettings.VrOptions.RADIAL_REPEAT)
    };

    public GuiRadialSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.radialsettings";
        super.init(SETTINGS, true);
        super.addDefaultButtons();
    }
}
