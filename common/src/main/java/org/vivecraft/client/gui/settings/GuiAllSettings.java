package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiListScreen;
import org.vivecraft.client.gui.framework.widgets.SettingsList;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.server.config.ConfigBuilder;
import org.vivecraft.server.config.ServerConfig;

import java.util.LinkedList;
import java.util.List;

public class GuiAllSettings extends GuiListScreen {
    public GuiAllSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.settings"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        for (VRSettings.VrOptions option : VRSettings.VrOptions.values()) {
            if (option == VRSettings.VrOptions.CHAT_NOTIFICATION_SOUND) {
                entries.add(new SettingsList.ScreenEntry(
                    "vivecraft.options.CHAT_NOTIFICATION_SOUND", GuiChatNotificationSelection::new));
            } else if (option != VRSettings.VrOptions.DUMMY) {
                entries.add(SettingsList.vrOptionToEntry(option));
            }
        }
        // add special cases
        // quick commands
        entries.add(new SettingsList.ScreenEntry(
            "vivecraft.options.screen.quickcommands.button", GuiQuickCommandEditor::new));

        // radial menu
        entries.add(new SettingsList.ScreenEntry(
            "vivecraft.options.screen.radialmenu.button", GuiRadialConfiguration::new));

        // server blacklist
        entries.add(new SettingsList.ScreenEntry(
            "vivecraft.options.screen.blocklist", GuiBlacklistEditor::new));

        // keyboard layouts
        entries.add(new SettingsList.ScreenEntry(
            "vivecraft.options.screen.activekeyboardlayouts", GuiActiveKeyboardLayoutSelector::new));

        // custom keyboard layout editor
        entries.add(new SettingsList.ScreenEntry(
            "vivecraft.options.screen.customkeyboardeditor", GuiKeyboardLayoutEditor::new));

        // keyboard theme editor
        entries.add(new SettingsList.ScreenEntry(
            "vivecraft.options.screen.customkeyboardthemeeditor", GuiKeyboardThemeEditor::new));

        // server settings
        for (ConfigBuilder.ConfigValue<?> cv : ServerConfig.getConfigValues()) {
            entries.add(SettingsList.ConfigToEntry(cv));
        }

        return entries;
    }
}
