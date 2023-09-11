package org.vivecraft.client.gui.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.LinkedList;
import java.util.List;

public class VivecraftMainSettings extends GuiListScreen {
    public VivecraftMainSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.settings"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();

        entries.add(new SettingsList.WidgetEntry(
            Component.translatable("vivecraft.options.screen.main"),
            Button.builder(Component.translatable("vivecraft.options.screen.main"), button -> this.minecraft.setScreen(new GuiMainVRSettings(this))).size(SettingsList.WidgetEntry.valueButtonWidth, 20).build()));

        entries.add(new SettingsList.WidgetEntry(
            Component.translatable("vivecraft.options.screen.server"),
            Button.builder(Component.translatable("vivecraft.options.screen.server"), button -> this.minecraft.setScreen(new GuiServerSettings(this))).size(SettingsList.WidgetEntry.valueButtonWidth, 20).build()));

        entries.add(new SettingsList.WidgetEntry(
                Component.translatable("vivecraft.options.screen.client"),
                Button.builder(Component.translatable("vivecraft.options.screen.client"), button -> this.minecraft.setScreen(new GuiClientSettings(this))).size(SettingsList.WidgetEntry.valueButtonWidth, 20).build()));

        entries.add(new SettingsList.CategoryEntry(Component.literal("Vivecraft Buttons")));

        entries.add(new SettingsList.WidgetEntry(
            Component.translatable("vivecraft.options.VR_TOGGLE_BUTTON_VISIBLE"),
            CycleButton.onOffBuilder(ClientDataHolderVR.getInstance().vrSettings.vrToggleButtonEnabled).displayOnlyValue().create(0,0, SettingsList.WidgetEntry.valueButtonWidth,20, Component.empty(), (cycleButton, object) -> {
                ClientDataHolderVR.getInstance().vrSettings.vrToggleButtonEnabled = !ClientDataHolderVR.getInstance().vrSettings.vrToggleButtonEnabled;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            })));
        entries.add(new SettingsList.WidgetEntry(
            Component.translatable("vivecraft.options.VR_SETTINGS_BUTTON_VISIBLE"),
            CycleButton.onOffBuilder(ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled).displayOnlyValue().create(0,0, SettingsList.WidgetEntry.valueButtonWidth,20, Component.empty(), (cycleButton, object) -> {
                ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled = !ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            })));
        entries.add(new SettingsList.WidgetEntry(
            Component.translatable("vivecraft.options.VR_SETTINGS_BUTTON_POSITION"),
            new CycleButton.Builder<Boolean>(bool -> bool ? Component.translatable("vivecraft.options.left") : Component.translatable("vivecraft.options.right")).withValues(ImmutableList.of(Boolean.TRUE, Boolean.FALSE)).withInitialValue(ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft).displayOnlyValue().create(0,0, SettingsList.WidgetEntry.valueButtonWidth,20, Component.empty(), (cycleButton, object) -> {
                ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft = !ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            })));
        return entries;
    }
}
