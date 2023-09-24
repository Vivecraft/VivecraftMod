package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.settings.VRSettings.ChatNotifications;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

public class GuiOtherHUDSettings extends org.vivecraft.client.gui.framework.GuiVROptionsBase
{
    public static String vrTitle = "vivecraft.options.screen.guiother";
    public GuiOtherHUDSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    @Override
    public void init()
    {
        super.clearWidgets();
        super.init(
            VrOptions.CROSSHAIR_SCALE,
            VrOptions.RENDER_CROSSHAIR_MODE,
            VrOptions.RENDER_BLOCK_OUTLINE_MODE,
            VrOptions.MENU_CROSSHAIR_SCALE,
            VrOptions.CROSSHAIR_OCCLUSION,
            VrOptions.CROSSHAIR_SCALES_WITH_DISTANCE,
            VrOptions.CHAT_NOTIFICATIONS,
            dh.vrSettings.chatNotifications == ChatNotifications.SOUND || dh.vrSettings.chatNotifications == ChatNotifications.BOTH ? VrOptions.CHAT_NOTIFICATION_SOUND : VrOptions.DUMMY,
            VrOptions.SHOW_UPDATES,
            VrOptions.SHOW_PLUGIN,
            VrOptions.SHOW_PLUGIN_MISSING,
            VrOptions.AUTO_OPEN_KEYBOARD
        );
        super.init(VrOptions.PHYSICAL_KEYBOARD, (button, mousePos) -> {
            KeyboardHandler.setOverlayShowing(false);
            return false;
        });
        super.init(
            VrOptions.PHYSICAL_KEYBOARD_SCALE,
            VrOptions.PHYSICAL_KEYBOARD_THEME
        );

        super.addDefaultButtons();
    }

    @Override
    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            switch(guivroption.getOption()){
                case CHAT_NOTIFICATIONS -> { this.reinit = true; }
                case PHYSICAL_KEYBOARD_THEME -> { KeyboardHandler.physicalKeyboard.init(); }
                case MENU_ALWAYS_FOLLOW_FACE ->
                {
                    GuiHandler.onScreenChanged(
                        mc.screen,
                        mc.screen,
                        false
                    );
                }
            }
        }
    }
}
