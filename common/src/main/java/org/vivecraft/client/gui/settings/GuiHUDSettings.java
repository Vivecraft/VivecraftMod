package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiHUDSettings extends GuiVROptionsBase {
    private final VROptionEntry[] hudOptions = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.HUD_HIDE),
        new VROptionEntry(VRSettings.VrOptions.HUD_LOCK_TO),
        new VROptionEntry(VRSettings.VrOptions.HUD_SCALE),
        new VROptionEntry(VRSettings.VrOptions.HUD_DISTANCE),
        new VROptionEntry(VRSettings.VrOptions.HUD_OCCLUSION),
        new VROptionEntry(VRSettings.VrOptions.HUD_OPACITY),
        new VROptionEntry(VRSettings.VrOptions.RENDER_MENU_BACKGROUND),
        new VROptionEntry(VRSettings.VrOptions.TOUCH_HOTBAR),
        new VROptionEntry(VRSettings.VrOptions.MENU_ALWAYS_FOLLOW_FACE),
        new VROptionEntry(VRSettings.VrOptions.GUI_APPEAR_OVER_BLOCK),
        new VROptionEntry(VRSettings.VrOptions.HUD_WRIST_OFFSET),
        new VROptionEntry("vivecraft.options.screen.guirendering.button", (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiRenderingSettings(this));
            return true;
        }),
        new VROptionEntry("vivecraft.options.screen.keyboard.button", (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiKeyboardSettings(this));
            return true;
        }),
        new VROptionEntry("vivecraft.options.screen.menuworld.button", (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiMenuWorldSettings(this));
            return true;
        }),
    };

    public GuiHUDSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.gui";
        super.init(this.hudOptions, true);
        super.addDefaultButtons();
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
        this.minecraft.options.hideGui = false;
    }
}
