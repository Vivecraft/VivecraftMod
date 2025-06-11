package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiListScreen;
import org.vivecraft.client.gui.framework.widgets.SettingsList;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.LinkedList;
import java.util.List;

public class VivecraftMainSettings extends GuiListScreen {
    public VivecraftMainSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.settings"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();

        SettingsList.BaseEntry vrButton = SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_ENABLED);
        vrButton.setActive(
            vrButton.isActive() && (ClientNetworking.SERVER_ALLOWS_VR_SWITCHING || this.minecraft.player == null));
        entries.add(vrButton);

        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_REMEMBER_ENABLED));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_PLUGIN));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_CLOSE_WITH_RUNTIME));

        entries.add(new SettingsList.ScreenEntry("vivecraft.options.screen.main", GuiMainVRSettings::new));

        entries.add(new SettingsList.ScreenEntry("vivecraft.options.screen.server", GuiServerSettings::new));

        entries.add(new SettingsList.ScreenEntry("vivecraft.options.screen.blocklist", GuiBlacklistEditor::new));

        entries.add(new SettingsList.CategoryEntry(Component.literal("Vivecraft Buttons")));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_TOGGLE_BUTTON_VISIBLE));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_SETTINGS_BUTTON_VISIBLE));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_SETTINGS_BUTTON_POSITION));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.MODIFY_PAUSE_MENU));

        entries.add(new SettingsList.CategoryEntry(Component.literal("Debug")));

        entries.add(new SettingsList.ScreenEntry("vivecraft.options.screen.debug", GuiDebugRenderSettings::new));

        return entries;
    }
}
