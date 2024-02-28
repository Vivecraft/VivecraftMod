package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList;
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
        vrButton.setActive(vrButton.isActive() && (ClientNetworking.serverAllowsVrSwitching || minecraft.player == null));
        entries.add(vrButton);

        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_REMEMBER_ENABLED));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_PLUGIN));

        entries.add(new SettingsList.WidgetEntry(
            Component.translatable("vivecraft.options.screen.main"),
            new Button(
                0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20,
                Component.translatable("vivecraft.options.screen.main"),
                button -> this.minecraft.setScreen(new GuiMainVRSettings(this)))
        ));

        entries.add(new SettingsList.WidgetEntry(
            Component.translatable("vivecraft.options.screen.server"),
            new Button(
                0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20,
                Component.translatable("vivecraft.options.screen.server"),
                button -> this.minecraft.setScreen(new GuiServerSettings(this)))
        ));

        entries.add(new SettingsList.CategoryEntry(Component.literal("Vivecraft Buttons")));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_TOGGLE_BUTTON_VISIBLE));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_SETTINGS_BUTTON_VISIBLE));
        entries.add(SettingsList.vrOptionToEntry(VRSettings.VrOptions.VR_SETTINGS_BUTTON_POSITION));

        return entries;
    }
}
