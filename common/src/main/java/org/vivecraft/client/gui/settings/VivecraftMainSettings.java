package org.vivecraft.client.gui.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

import java.util.LinkedList;
import java.util.List;

public class VivecraftMainSettings extends GuiListScreen {
    public VivecraftMainSettings(Screen lastScreen) {
        super(new TranslatableComponent("vivecraft.options.screen.settings"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();

        AbstractWidget vrButton = CycleButton.onOffBuilder(VRState.vrEnabled)
            .displayOnlyValue()
            .withTooltip(value -> font.split(new TranslatableComponent("vivecraft.options.VR_MODE.tooltip"), 200))
            .create(0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20, TextComponent.EMPTY, (cycleButton, object) -> {
                VRState.vrEnabled = !VRState.vrEnabled;
                ClientDataHolderVR.getInstance().vrSettings.vrEnabled = VRState.vrEnabled;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            });
        vrButton.active = ClientNetworking.serverAllowsVrSwitching || minecraft.player == null;

        entries.add(new SettingsList.WidgetEntry(
            new TranslatableComponent("vivecraft.gui.vr", TextComponent.EMPTY),
            vrButton
        ));

        entries.add(new SettingsList.WidgetEntry(
            new TranslatableComponent("vivecraft.options.screen.main"),
            new Button(
                0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20,
                new TranslatableComponent("vivecraft.options.screen.main"),
                button -> this.minecraft.setScreen(new GuiMainVRSettings(this)))));

        entries.add(new SettingsList.WidgetEntry(
            new TranslatableComponent("vivecraft.options.screen.server"),
            new Button(
                0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20,
                new TranslatableComponent("vivecraft.options.screen.server"),
                button -> this.minecraft.setScreen(new GuiServerSettings(this)))));

        entries.add(new SettingsList.CategoryEntry(new TextComponent("Vivecraft Buttons")));

        entries.add(new SettingsList.WidgetEntry(
            new TranslatableComponent("vivecraft.options.VR_TOGGLE_BUTTON_VISIBLE"),
            CycleButton.onOffBuilder(ClientDataHolderVR.getInstance().vrSettings.vrToggleButtonEnabled).displayOnlyValue().create(0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20, TextComponent.EMPTY, (cycleButton, object) -> {
                ClientDataHolderVR.getInstance().vrSettings.vrToggleButtonEnabled = !ClientDataHolderVR.getInstance().vrSettings.vrToggleButtonEnabled;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            })));
        entries.add(new SettingsList.WidgetEntry(
            new TranslatableComponent("vivecraft.options.VR_SETTINGS_BUTTON_VISIBLE"),
            CycleButton.onOffBuilder(ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled).displayOnlyValue().create(0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20, TextComponent.EMPTY, (cycleButton, object) -> {
                ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled = !ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonEnabled;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            })));
        entries.add(new SettingsList.WidgetEntry(
            new TranslatableComponent("vivecraft.options.VR_SETTINGS_BUTTON_POSITION"),
            new CycleButton.Builder<Boolean>(bool -> bool ? new TranslatableComponent("vivecraft.options.left") : new TranslatableComponent("vivecraft.options.right")).withValues(ImmutableList.of(Boolean.TRUE, Boolean.FALSE)).withInitialValue(ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft).displayOnlyValue().create(0, 0, SettingsList.WidgetEntry.valueButtonWidth, 20, TextComponent.EMPTY, (cycleButton, object) -> {
                ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft = !ClientDataHolderVR.getInstance().vrSettings.vrSettingsButtonPositionLeft;
                ClientDataHolderVR.getInstance().vrSettings.saveOptions();
            })));
        return entries;
    }
}
