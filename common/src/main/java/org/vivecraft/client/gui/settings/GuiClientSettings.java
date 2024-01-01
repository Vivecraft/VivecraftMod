package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.ClientConfig;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.common.ConfigBuilder;

import java.util.LinkedList;
import java.util.List;

public class GuiClientSettings extends GuiListScreen {
    public GuiClientSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.client"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        String lastCategory = null;
        for (ConfigBuilder.ConfigValue cv : ClientConfig.getConfigValues()) {
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
