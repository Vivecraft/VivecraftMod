package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiKeyboardSettings extends GuiVROptionsBase {
    private static final VROptionEntry[] KEYBOARD_OPTIONS = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.PHYSICAL_KEYBOARD, (button, mousePos) -> {
            KeyboardHandler.setOverlayShowing(false);
            return false;
        }),
        new VROptionEntry(VRSettings.VrOptions.KEYBOARD_PRESS_BINDS),
        new VROptionEntry(VRSettings.VrOptions.AUTO_OPEN_KEYBOARD),
        new VROptionEntry(VRSettings.VrOptions.AUTO_CLOSE_KEYBOARD),
        new VROptionEntry(VRSettings.VrOptions.PHYSICAL_KEYBOARD_SCALE),
        new VROptionEntry(VRSettings.VrOptions.PHYSICAL_KEYBOARD_THEME)
    };

    public GuiKeyboardSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.keyboard";
        super.init(KEYBOARD_OPTIONS, true);
        super.addDefaultButtons();
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
    }
}
