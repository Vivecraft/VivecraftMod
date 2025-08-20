package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiListScreen;
import org.vivecraft.client.gui.framework.widgets.MultilineComponent;
import org.vivecraft.client.gui.framework.widgets.SettingsList;
import org.vivecraft.server.config.ConfigBuilder;
import org.vivecraft.server.config.ServerConfig;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class GuiServerSettings extends GuiListScreen {

    public GuiServerSettings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.server"), lastScreen);
    }

    @Override
    public void init() {
        super.init();
        if (this.minecraft.level != null && !this.minecraft.isLocalServer()) {
            this.addRenderableWidget(new MultilineComponent(this.width / 2, this.height / 2, this.list.getRowWidth(),
                Component.translatable("vivecraft.messages.serversettingsnotavailable"), true,
                this.minecraft.font).withBackground());
            this.list.active = false;
            if (this.searchBox != null) {
                this.searchBox.active = false;
            }
        }
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        String lastGroup = null;
        Deque<SettingsList.GroupedEntry> groupStack = new ArrayDeque<>();
        Consumer<SettingsList.BaseEntry> consumer = (entry) -> {
            if (groupStack.isEmpty()) {
                // no group active, add globally
                entries.add(entry);
            } else {
                // add to active group
                groupStack.getFirst().add(entry);
            }
        };

        for (ConfigBuilder.ConfigValue<?> cv : ServerConfig.getConfigValues()) {
            String path = cv.getPath();
            String group = getParent(path);
            if (!group.equals(lastGroup)) {
                if (lastGroup != null && !group.startsWith(lastGroup)) {
                    // new non-child group, walk back till we find a common parent again
                    String parent = getParent(lastGroup);
                    while (!groupStack.isEmpty() && (parent.isEmpty() || !group.startsWith(parent))) {
                        groupStack.removeFirst();
                        parent = getParent(parent);
                    }
                }
                // create a new group
                SettingsList.GroupedEntry groupedEntry = new SettingsList.GroupedEntry(
                    Component.translatable("vivecraft.serverSettings." + group));
                // add category
                consumer.accept(groupedEntry);
                groupStack.addFirst(groupedEntry);
                lastGroup = group;
            }
            SettingsList.BaseEntry entry = SettingsList.ConfigToEntry(cv);
            // add entry
            consumer.accept(entry);
        }
        return entries;
    }

    private String getParent(String entry) {
        if (entry.contains(".")) {
            return entry.substring(0, entry.lastIndexOf("."));
        } else {
            return "";
        }
    }
}
