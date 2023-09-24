package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client.gui.framework.VROptionPosition.POS_CENTER;

public class GuiStandingSettings extends GuiVROptionsBase
{
    public static String vrTitle = "vivecraft.options.screen.standing";
    public GuiStandingSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    @Override
    public void init()
    {
        super.clearWidgets();
        super.init(
            VrOptions.WALK_UP_BLOCKS,
            VrOptions.VEHICLE_ROTATION,
            VrOptions.WALK_MULTIPLIER,
            VrOptions.WORLD_ROTATION_INCREMENT,
            VrOptions.BCB_ON,
            VrOptions.ALLOW_STANDING_ORIGIN_OFFSET
        );
        super.init(VrOptions.FORCE_STANDING_FREE_MOVE, POS_CENTER);
        super.init(VrOptions.DUMMY, POS_CENTER);
        super.init(
            GuiTeleportSettings.class,
            GuiFreeMoveSettings.class
        );
        super.addDefaultButtons();
    }
}
