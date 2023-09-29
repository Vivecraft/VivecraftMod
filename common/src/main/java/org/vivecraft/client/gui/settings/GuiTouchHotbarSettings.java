package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import static org.vivecraft.client.gui.framework.VROptionPosition.POS_CENTER;
import static org.vivecraft.client_vr.VRState.dh;

public class GuiTouchHotbarSettings extends GuiVROptionsBase {

    public static String vrTitle = "vivecraft.options.screen.touch_hotbar";

    public GuiTouchHotbarSettings(final Screen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void init() {
        super.clearWidgets();
        super.init(VrOptions.TOUCH_HOTBAR, POS_CENTER);
        super.init(VrOptions.DUMMY, POS_CENTER);
        if (dh.vrSettings.vrTouchHotbar) {
            super.init(
                VrOptions.TOUCH_HOTBAR_COLOR_R,
                VrOptions.TOUCH_HOTBAR_COLOR_G,
                VrOptions.TOUCH_HOTBAR_COLOR_B,
                VrOptions.TOUCH_HOTBAR_COLOR_A
            );
            super.init(VrOptions.DUMMY, POS_CENTER);
            super.init(
                VrOptions.TOUCH_OFFBAR_COLOR_R,
                VrOptions.TOUCH_OFFBAR_COLOR_G,
                VrOptions.TOUCH_OFFBAR_COLOR_B,
                VrOptions.TOUCH_OFFBAR_COLOR_A
            );
        }
        super.addDefaultButtons();
    }

    @Override
    protected void actionPerformed(final AbstractWidget widget) {
        if (widget instanceof final GuiVROption guivroption) {
            if (guivroption.getOption() == VrOptions.TOUCH_HOTBAR) {
                this.reinit = true;
            }
        }
    }
}
