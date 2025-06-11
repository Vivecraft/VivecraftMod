package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.widgets.GuiVROption;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiTeleportSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] TELEPORT_SETTINGS = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.SIMULATE_FALLING,
        VRSettings.VrOptions.LIMIT_TELEPORT
    };
    private static final VRSettings.VrOptions[] LIMITED_TELEPORT_SETTINGS = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.TELEPORT_UP_LIMIT,
        VRSettings.VrOptions.TELEPORT_DOWN_LIMIT,
        VRSettings.VrOptions.TELEPORT_HORIZ_LIMIT
    };

    public GuiTeleportSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.teleport";
        super.init(TELEPORT_SETTINGS, true);

        if (this.vrSettings.vrLimitedSurvivalTeleport) {
            super.init(LIMITED_TELEPORT_SETTINGS, false);
        }

        super.addDefaultButtons();
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            if (guivroption.getId() == VRSettings.VrOptions.LIMIT_TELEPORT.ordinal()) {
                this.reinit = true;
            }
        }
    }
}
