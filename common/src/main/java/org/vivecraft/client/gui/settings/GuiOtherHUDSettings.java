package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiOtherHUDSettings extends GuiVROptionsBase
{
    static VRSettings.VrOptions[] hudOptions = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.CROSSHAIR_SCALE,
            VRSettings.VrOptions.RENDER_CROSSHAIR_MODE,
            VRSettings.VrOptions.RENDER_BLOCK_OUTLINE_MODE,
            VRSettings.VrOptions.MENU_CROSSHAIR_SCALE,
            VRSettings.VrOptions.CROSSHAIR_OCCLUSION,
            VRSettings.VrOptions.CROSSHAIR_SCALES_WITH_DISTANCE,
            VRSettings.VrOptions.CHAT_NOTIFICATIONS
    };
    static VRSettings.VrOptions[] chat = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.CHAT_NOTIFICATION_SOUND
    };

    static VRSettings.VrOptions[] messages = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.SHOW_UPDATES,
            VRSettings.VrOptions.SHOW_PLUGIN,
            VRSettings.VrOptions.SHOW_PLUGIN_MISSING
    };

    public GuiOtherHUDSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.guiother";
        super.init(hudOptions, true);

        if (this.dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND || this.dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH)
        {
            super.init(chat, false);
        } else {
            super.init(new VRSettings.VrOptions[]{VRSettings.VrOptions.DUMMY}, false);
        }
        super.init(messages, false);

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if (guivroption.getId() == VRSettings.VrOptions.CHAT_NOTIFICATIONS.ordinal())
            {
                this.reinit = true;
            }
        }
    }
}
