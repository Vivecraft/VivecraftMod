package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiRenderingSettings extends GuiVROptionsBase {
    private final VROptionEntry[] hudOptions = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.DOUBLE_GUI_RESOLUTION),
        new VROptionEntry(VRSettings.VrOptions.GUI_MIPMAPS),
        new VROptionEntry(VRSettings.VrOptions.GUI_SCALE),
        new VROptionEntry(VRSettings.VrOptions.GUI_ANISOTROPIC_FILTERING),
        new VROptionEntry(VRSettings.VrOptions.HUD_MAX_GUI_SCALE)
    };

    public GuiRenderingSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.guirendering";
        super.init(this.hudOptions, true);
        super.addDefaultButtons();
    }
}
