package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiHUDSettings extends org.vivecraft.client.gui.framework.GuiVROptionsBase {
    public static String vrTitle = "vivecraft.options.screen.gui";

    public GuiHUDSettings(Screen guiScreen) {
        super(guiScreen);
    }

    @Override
    public void init() {
        super.clearWidgets();
        super.init(
            VrOptions.HUD_HIDE,
            VrOptions.HUD_LOCK_TO,
            VrOptions.HUD_SCALE,
            VrOptions.HUD_DISTANCE,
            VrOptions.HUD_OCCLUSION,
            VrOptions.HUD_OPACITY,
            VrOptions.RENDER_MENU_BACKGROUND,
            VrOptions.DUMMY,
            VrOptions.DUMMY,
            VrOptions.MENU_ALWAYS_FOLLOW_FACE,
            VrOptions.DUMMY,
            VrOptions.GUI_APPEAR_OVER_BLOCK,
            VrOptions.DUMMY
        );
        super.init(GuiMenuWorldSettings.class);
        super.init(
            VrOptions.DUMMY,
            VrOptions.SHADER_GUI_RENDER
        );
        super.addDefaultButtons();
    }

    @Override
    protected void loadDefaults() {
        super.loadDefaults();
        Minecraft.getInstance().options.hideGui = false;
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            switch (guivroption.getOption()) {
                case PHYSICAL_KEYBOARD_THEME -> {
                    KeyboardHandler.physicalKeyboard.init();
                }
                case MENU_ALWAYS_FOLLOW_FACE -> {
                    GuiHandler.onScreenChanged(
                        Minecraft.getInstance().screen,
                        Minecraft.getInstance().screen,
                        false
                    );
                }
            }
        }
    }
}
