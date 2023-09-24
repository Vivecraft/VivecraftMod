package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client.gui.framework.VROptionPosition.POS_CENTER;

public class GuiSeatedOptions extends GuiVROptionsBase
{
    public static String vrTitle = "vivecraft.options.screen.seated";
    public GuiSeatedOptions(Screen guiScreen)
    {
        super(guiScreen);
    }

    @Override
    public void init()
    {
        super.clearWidgets();
        super.init(
            VrOptions.X_SENSITIVITY,
            VrOptions.Y_SENSITIVITY,
            VrOptions.KEYHOLE,
            VrOptions.SEATED_HUD_XHAIR,
            VrOptions.WALK_UP_BLOCKS,
            VrOptions.WORLD_ROTATION_INCREMENT,
            VrOptions.VEHICLE_ROTATION,
            VrOptions.DUMMY
        );
        super.init(VrOptions.SEATED_FREE_MOVE, POS_CENTER);
        super.init(VrOptions.RIGHT_CLICK_DELAY);
        super.init(
            GuiTeleportSettings.class,
            GuiFreeMoveSettings.class
        );
        super.addDefaultButtons();
    }
}
