package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.server.config.ConfigBuilder;
import org.vivecraft.server.config.ServerConfig;

import java.util.LinkedList;
import java.util.List;

public class GuiServerSettings extends GuiListScreen {

    public GuiServerSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.server"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        String lastCategory = null;
        for (ConfigBuilder.ConfigValue cv : ServerConfig.getConfigValues()) {
            String path = cv.getPath();
            String category = path.substring(0, path.lastIndexOf("."));
            String name = path.substring(path.lastIndexOf(".") + 1);
            if (!category.equals(lastCategory)) {
                lastCategory = category;
                entries.add(new SettingsList.CategoryEntry(Component.literal(category)));
            }
            entries.add(SettingsList.ConfigToEntry(cv, Component.literal(name)));
        }
        return entries;
    }
}
