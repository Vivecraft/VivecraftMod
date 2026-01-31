package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiListScreen;
import org.vivecraft.client.gui.framework.widgets.SettingsList;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.LinkedList;
import java.util.List;

public class GuiBackpackSwitchingSettings extends GuiListScreen {
    private static final VRSettings.VrOptions[] BACKPACK_SETTINGS = new VRSettings.VrOptions[]{
        VRSettings.VrOptions.BACKPACK_SWITCH,
        VRSettings.VrOptions.BACKPACK_SWITCH_MAIN_HAND,
        VRSettings.VrOptions.BACKPACK_SWITCH_OFFHAND
    };

    public GuiBackpackSwitchingSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.backpackswitching"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        for (VRSettings.VrOptions option : BACKPACK_SETTINGS) {
            entries.add(SettingsList.vrOptionToEntry(option));
        }
        return entries;
    }
}
