package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client_vr.VRState.dh;

public class GuiVRFingerDisplays extends GuiVROptionsBase {

    public static String vrTitle = "vivecraft.options.screen.controls.skeletal_input.finger_displays";

    public GuiVRFingerDisplays(final Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    @Override
    public void init()
    {
        super.clearWidgets();
        if (dh.vrSettings.reverseHands){
            super.init(
                VrOptions.MAIN_THUMB_DISPLAY,
                VrOptions.OFF_THUMB_DISPLAY,
                VrOptions.MAIN_INDEX_DISPLAY,
                VrOptions.OFF_INDEX_DISPLAY,
                VrOptions.MAIN_MIDDLE_DISPLAY,
                VrOptions.OFF_MIDDLE_DISPLAY,
                VrOptions.MAIN_RING_DISPLAY,
                VrOptions.OFF_RING_DISPLAY,
                VrOptions.MAIN_LITTLE_DISPLAY,
                VrOptions.OFF_LITTLE_DISPLAY
            );
        }
        else
        {
            super.init(
                VrOptions.OFF_THUMB_DISPLAY,
                VrOptions.MAIN_THUMB_DISPLAY,
                VrOptions.OFF_INDEX_DISPLAY,
                VrOptions.MAIN_INDEX_DISPLAY,
                VrOptions.OFF_MIDDLE_DISPLAY,
                VrOptions.MAIN_MIDDLE_DISPLAY,
                VrOptions.OFF_RING_DISPLAY,
                VrOptions.MAIN_RING_DISPLAY,
                VrOptions.OFF_LITTLE_DISPLAY,
                VrOptions.MAIN_LITTLE_DISPLAY
            );
        }
        super.addDefaultButtons();
    }
}
