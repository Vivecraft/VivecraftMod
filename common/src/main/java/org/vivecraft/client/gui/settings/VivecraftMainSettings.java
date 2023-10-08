package org.vivecraft.client.gui.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.CycleButton.Builder;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList.BaseEntry;
import org.vivecraft.client.gui.widgets.SettingsList.CategoryEntry;
import org.vivecraft.client.gui.widgets.SettingsList.WidgetEntry;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRState;

import java.util.LinkedList;
import java.util.List;

import static org.vivecraft.client_vr.VRState.dh;

public class VivecraftMainSettings extends GuiListScreen {
    public VivecraftMainSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.settings"), lastScreen);
    }

    @Override
    protected List<BaseEntry> getEntries() {
        List<BaseEntry> entries = new LinkedList<>();

        AbstractWidget vrButton = CycleButton.onOffBuilder(VRState.vrEnabled)
            .displayOnlyValue()
            .withTooltip(value -> Tooltip.create(Component.translatable("vivecraft.options.VR_MODE.tooltip")))
            .create(0, 0, WidgetEntry.valueButtonWidth, 20, Component.empty(), (cycleButton, object) -> {
                VRState.vrEnabled = !VRState.vrEnabled;
                dh.vrSettings.vrEnabled = VRState.vrEnabled;
                dh.vrSettings.saveOptions();
            });
        vrButton.active = ClientNetworking.serverAllowsVrSwitching || minecraft.player == null;

        entries.add(new WidgetEntry(
            Component.translatable("vivecraft.gui.vr", Component.empty()),
            vrButton
        ));

        entries.add(new WidgetEntry(
            Component.translatable("vivecraft.options.screen.main"),
            Button.builder(Component.translatable("vivecraft.options.screen.main"), button -> this.minecraft.setScreen(new GuiMainVRSettings(this))).size(WidgetEntry.valueButtonWidth, 20).build()));

        entries.add(new WidgetEntry(
            Component.translatable("vivecraft.options.screen.server"),
            Button.builder(Component.translatable("vivecraft.options.screen.server"), button -> this.minecraft.setScreen(new GuiServerSettings(this))).size(WidgetEntry.valueButtonWidth, 20).build()));

        entries.add(new CategoryEntry(Component.literal("Vivecraft Buttons")));

        entries.add(new WidgetEntry(
            Component.translatable("vivecraft.options.VR_TOGGLE_BUTTON_VISIBLE"),
            CycleButton.onOffBuilder(dh.vrSettings.vrToggleButtonEnabled).displayOnlyValue().create(0, 0, WidgetEntry.valueButtonWidth, 20, Component.empty(), (cycleButton, object) -> {
                dh.vrSettings.vrToggleButtonEnabled = !dh.vrSettings.vrToggleButtonEnabled;
                dh.vrSettings.saveOptions();
            })));
        entries.add(new WidgetEntry(
            Component.translatable("vivecraft.options.VR_SETTINGS_BUTTON_VISIBLE"),
            CycleButton.onOffBuilder(dh.vrSettings.vrSettingsButtonEnabled).displayOnlyValue().create(0, 0, WidgetEntry.valueButtonWidth, 20, Component.empty(), (cycleButton, object) -> {
                dh.vrSettings.vrSettingsButtonEnabled = !dh.vrSettings.vrSettingsButtonEnabled;
                dh.vrSettings.saveOptions();
            })));
        entries.add(new WidgetEntry(
            Component.translatable("vivecraft.options.VR_SETTINGS_BUTTON_POSITION"),
            new Builder<Boolean>(bool -> bool ? Component.translatable("vivecraft.options.left") : Component.translatable("vivecraft.options.right")).withValues(ImmutableList.of(Boolean.TRUE, Boolean.FALSE)).withInitialValue(dh.vrSettings.vrSettingsButtonPositionLeft).displayOnlyValue().create(0, 0, WidgetEntry.valueButtonWidth, 20, Component.empty(), (cycleButton, object) -> {
                dh.vrSettings.vrSettingsButtonPositionLeft = !dh.vrSettings.vrSettingsButtonPositionLeft;
                dh.vrSettings.saveOptions();
            })));
        return entries;
    }
}
