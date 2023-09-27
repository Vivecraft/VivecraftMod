package org.vivecraft.client.gui.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.server.config.ConfigBuilder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class GuiListValueEditScreen extends GuiListScreen {

    protected final ConfigBuilder.ListValue<String> listValue;
    private List<String> elements;

    public GuiListValueEditScreen(Component title, Screen lastScreen, ConfigBuilder.ListValue<String> listValue) {
        super(title, lastScreen);
        this.listValue = listValue;
    }

    @Override
    protected void init() {
        clearWidgets();
        double scrollAmount = list != null ? list.getScrollAmount() : 0.0D;

        this.list = new SettingsList(this, minecraft, getEntries());
        list.setScrollAmount(scrollAmount);
        this.addWidget(this.list);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            listValue.set(getCurrentValues());
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 155, this.height - 27, 150, 20).build());

        this.addRenderableWidget(Button
            .builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen))
            .bounds(this.width / 2 + 5, this.height - 27, 150, 20)
            .build());
    }

    private List<String> getCurrentValues() {
        return list.children().stream().map(entry -> {
            if (entry instanceof ListValueEntry listValueEntry) {
                return listValueEntry.getString();
            } else {
                return "";
            }
        }).filter(string -> !string.isEmpty()).collect(Collectors.toList());
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        if (elements == null) {
            elements = new ArrayList<>(listValue.get());
        }
        int i = 0;
        for (String item : elements) {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, ListValueEntry.valueButtonWidth - 1, 20, Component.literal(item));
            box.setMaxLength(1000);
            box.setValue(item);
            int index = i++;
            entries.add(new ListValueEntry(Component.empty(), box, button -> {
                elements.remove(index);
                reinit = true;
            }));
        }
        entries.add(new SettingsList.WidgetEntry(Component.translatable("vivecraft.options.addnew"), Button.builder(Component.literal("+"), button -> {
            elements = getCurrentValues();
            elements.add("");
            reinit = true;
        }).size(20, 20).build()));
        return entries;
    }

    private static class ListValueEntry extends SettingsList.WidgetEntry {
        public static final int valueButtonWidth = 280;

        private final Button deleteButton;

        public ListValueEntry(Component name, EditBox valueWidget, Button.OnPress deleteAction) {
            super(name, valueWidget);

            this.deleteButton = Button
                .builder(Component.literal("-"), deleteAction)
                .tooltip(Tooltip.create(Component.translatable("selectWorld.delete")))
                .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.valueWidget.setX(k + -50);
            this.valueWidget.setY(j);
            this.valueWidget.render(guiGraphics, n, o, f);
            this.deleteButton.setX(k + 230);
            this.deleteButton.setY(j);
            this.deleteButton.render(guiGraphics, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget, this.deleteButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget, this.deleteButton);
        }

        public String getString() {
            return ((EditBox) valueWidget).getValue();
        }
    }
}
