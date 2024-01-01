package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.common.ConfigBuilder;

import java.util.LinkedList;
import java.util.List;

public class GuiConfigListScreen extends GuiListScreen{
    private final ConfigBuilder.ConfigValue<?>[] config;

    public GuiConfigListScreen(Component title, Screen lastScreen, ConfigBuilder.ConfigValue<?> ... config) {
        super(title, lastScreen);
        this.config = config;
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        for (ConfigBuilder.ConfigValue<?> value : config) {
            String path = value.getPath();
            String name = path.substring(path.lastIndexOf(".") + 1);
            entries.add(SettingsList.ConfigToEntry(value, Component.literal(name)));
        }
        return entries;
    }
}
