package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.screens.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.widgets.GuiVROption;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiOtherHUDSettings extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] HUD_OPTIONS = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.CROSSHAIR_SCALE,
        VRSettings.VrOptions.RENDER_CROSSHAIR_MODE,
        VRSettings.VrOptions.RENDER_BLOCK_OUTLINE_MODE,
        VRSettings.VrOptions.MENU_CROSSHAIR_SCALE,
        VRSettings.VrOptions.CROSSHAIR_OCCLUSION,
        VRSettings.VrOptions.CROSSHAIR_SCALES_WITH_DISTANCE,
        VRSettings.VrOptions.CHAT_NOTIFICATIONS
    };
    private final VROptionEntry[] chat = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.CHAT_NOTIFICATION_SOUND, (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiChatNotificationSelection(this));
            return true;
        })};

    private static final VRSettings.VrOptions[] MESSAGES = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.SHOW_UPDATES,
        VRSettings.VrOptions.UPDATE_TYPE,
        VRSettings.VrOptions.SHOW_PLUGIN,
        VRSettings.VrOptions.SHOW_PLUGIN_MISSING,
        VRSettings.VrOptions.CHAT_MESSAGE_STENCIL,
        VRSettings.VrOptions.SHOW_SERVER_VR_CHANGES
    };

    public GuiOtherHUDSettings(Screen lastScreen) {
        super(lastScreen);
    }

    @Override
    public void init() {
        this.vrTitle = "vivecraft.options.screen.guiother";
        super.init(HUD_OPTIONS, true);

        if (this.dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND ||
            this.dataHolder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH)
        {
            super.init(this.chat, false);
        } else {
            super.init(new VRSettings.VrOptions[]{VRSettings.VrOptions.DUMMY}, false);
        }
        super.init(MESSAGES, false);

        super.addDefaultButtons();
    }

    @Override
    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption guivroption) {
            if (guivroption.getId() == VRSettings.VrOptions.CHAT_NOTIFICATIONS.ordinal()) {
                this.reinit = true;
            }
        }
    }
}
