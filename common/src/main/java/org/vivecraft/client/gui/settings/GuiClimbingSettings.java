package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiClimbingSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] CLIMBING_SETTINGS = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.REALISTIC_CLIMB,
        VRSettings.VrOptions.REALISTIC_CLIMB_AUTOGRAB,
        VRSettings.VrOptions.VANILLA_CLIMBING
    };

    public GuiClimbingSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.climbing";
        super.init(CLIMBING_SETTINGS, true);
        super.addDefaultButtons();
    }
}
