package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiTeleportSettings extends org.vivecraft.client.gui.framework.GuiVROptionsBase {
    public static String vrTitle = "vivecraft.options.screen.teleport";

    public GuiTeleportSettings(Screen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void init() {
        super.clearWidgets();
        super.init(
            VrOptions.SIMULATE_FALLING,
            VrOptions.LIMIT_TELEPORT
        );

        if (ClientDataHolderVR.getInstance().vrSettings.vrLimitedSurvivalTeleport) {
            super.init(
                VrOptions.TELEPORT_UP_LIMIT,
                VrOptions.TELEPORT_DOWN_LIMIT,
                VrOptions.TELEPORT_HORIZ_LIMIT
            );
        }

        super.addDefaultButtons();
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            if (guivroption.getOption() == VrOptions.LIMIT_TELEPORT) {
                this.reinit = true;
            }
        }
    }
}
