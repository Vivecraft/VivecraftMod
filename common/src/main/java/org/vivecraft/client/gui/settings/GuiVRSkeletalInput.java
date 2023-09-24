package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client.gui.framework.VROptionPosition.POS_CENTER;
import static org.vivecraft.client_vr.VRState.dh;

public class GuiVRSkeletalInput extends GuiVROptionsBase {

    public static String vrTitle = "vivecraft.options.screen.controls.skeletal_input";

    public GuiVRSkeletalInput(final Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        super.clearWidgets();
        if (dh.vrSettings.reverseHands){
            super.init(
                VrOptions.MAIN_THUMB_THRESHOLD,
                VrOptions.OFF_THUMB_THRESHOLD,
                VrOptions.MAIN_INDEX_THRESHOLD,
                VrOptions.OFF_INDEX_THRESHOLD,
                VrOptions.MAIN_MIDDLE_THRESHOLD,
                VrOptions.OFF_MIDDLE_THRESHOLD,
                VrOptions.MAIN_RING_THRESHOLD,
                VrOptions.OFF_RING_THRESHOLD,
                VrOptions.MAIN_LITTLE_THRESHOLD,
                VrOptions.OFF_LITTLE_THRESHOLD
            );
        } else {
            super.init(
                VrOptions.OFF_THUMB_THRESHOLD,
                VrOptions.MAIN_THUMB_THRESHOLD,
                VrOptions.OFF_INDEX_THRESHOLD,
                VrOptions.MAIN_INDEX_THRESHOLD,
                VrOptions.OFF_MIDDLE_THRESHOLD,
                VrOptions.MAIN_MIDDLE_THRESHOLD,
                VrOptions.OFF_RING_THRESHOLD,
                VrOptions.MAIN_RING_THRESHOLD,
                VrOptions.OFF_LITTLE_THRESHOLD,
                VrOptions.MAIN_LITTLE_THRESHOLD
            );
        }
        super.init(VrOptions.FINGER_COUNT);
        super.init(GuiVRFingerDisplays.class);
        super.init(VrOptions.FINGER_VIEW);
        super.init(VrOptions.SKELETAL_INPUT, POS_CENTER);
        super.addDefaultButtons();
    }
}
