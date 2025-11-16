package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiKeyboardSettings extends GuiVROptionsBase {
    private final VROptionEntry[] keyboardOptions = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.PHYSICAL_KEYBOARD),
        new VROptionEntry(VRSettings.VrOptions.KEYBOARD_PRESS_BINDS),
        new VROptionEntry(VRSettings.VrOptions.AUTO_OPEN_KEYBOARD),
        new VROptionEntry(VRSettings.VrOptions.AUTO_CLOSE_KEYBOARD),
        new VROptionEntry(VRSettings.VrOptions.PHYSICAL_KEYBOARD_SCALE),
        new VROptionEntry(VRSettings.VrOptions.PHYSICAL_KEYBOARD_THEME),
        new VROptionEntry(
            "vivecraft.options.screen.customkeyboardeditor.button", (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiKeyboardLayoutEditor(this));
            return true;
        }),
        new VROptionEntry(
            "vivecraft.options.screen.customkeyboardthemeeditor.button", (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiKeyboardThemeEditor(this));
            return true;
        }),
        new VROptionEntry(
            "vivecraft.options.screen.activekeyboardlayouts.button", (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiActiveKeyboardLayoutSelector(this));
            return true;
        })
    };

    public GuiKeyboardSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.keyboard";
        super.init(this.keyboardOptions, true);
        super.addDefaultButtons();
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
    }
}
