package org.vivecraft.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.gui.framework.GuiVROptionButton;
import org.vivecraft.gui.framework.GuiVROptionsBase;
import org.vivecraft.settings.VRSettings;

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

    public GuiOtherHUDSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.guiother";
        super.init(hudOptions, true);

        if (this.minecraft.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND || this.minecraft.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH)
        {
            super.init(chat, false);
        }

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROptionButton)
        {
            GuiVROptionButton guivroptionbutton = (GuiVROptionButton)widget;

            if (guivroptionbutton.id == VRSettings.VrOptions.CHAT_NOTIFICATIONS.ordinal())
            {
                this.reinit = true;
            }
        }
    }
}
