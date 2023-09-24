package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

public class GuiRoomscaleSettings extends GuiVROptionsBase
{
    public static String vrTitle = "vivecraft.options.screen.roomscale";
    public GuiRoomscaleSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    @Override
    public void init()
    {
        super.clearWidgets();
        super.init(
            VrOptions.WEAPON_COLLISION,
            VrOptions.REALISTIC_JUMP,
            VrOptions.REALISTIC_SNEAK,
            VrOptions.REALISTIC_CLIMB,
            VrOptions.REALISTIC_ROW,
            VrOptions.REALISTIC_SWIM,
            VrOptions.BOW_MODE,
            VrOptions.BACKPACK_SWITCH,
            VrOptions.ALLOW_CRAWLING
        );
        super.addDefaultButtons();
    }
}
