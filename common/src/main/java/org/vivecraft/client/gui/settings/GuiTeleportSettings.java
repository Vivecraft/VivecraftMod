package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiTeleportSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] teleportSettings = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.SIMULATE_FALLING,
        VRSettings.VrOptions.LIMIT_TELEPORT
    };
    private static final VRSettings.VrOptions[] limitedTeleportSettings = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.TELEPORT_UP_LIMIT,
        VRSettings.VrOptions.TELEPORT_DOWN_LIMIT,
        VRSettings.VrOptions.TELEPORT_HORIZ_LIMIT
    };

    public GuiTeleportSettings(Screen guiScreen) {
        super(guiScreen);
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.teleport";
        super.init(teleportSettings, true);

        if (this.settings.vrLimitedSurvivalTeleport) {
            super.init(limitedTeleportSettings, false);
        }

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            if (guivroption.getId() == VRSettings.VrOptions.LIMIT_TELEPORT.ordinal()) {
                this.reinit = true;
            }
        }
    }
}
