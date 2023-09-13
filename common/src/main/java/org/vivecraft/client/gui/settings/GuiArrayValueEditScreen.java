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
import org.vivecraft.common.ConfigBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GuiArrayValueEditScreen<T> extends GuiListScreen{
    private final ConfigBuilder.ArrayValue<T> arrayValue;
    private final Function<String, T> fromString;
    private T[] elements;

    public GuiArrayValueEditScreen(Component title, Screen lastScreen, ConfigBuilder.ArrayValue<T> arrayValue, Function<String, T> fromString) {
        super(title, lastScreen);
        this.arrayValue = arrayValue;
        this.fromString = fromString;
    }

    @Override
    protected void init() {
        clearWidgets();
        double scrollAmount = list != null? list.getScrollAmount() : 0.0D;

        this.list = new SettingsList(this, minecraft, getEntries());
        list.setScrollAmount(scrollAmount);
        this.addWidget(this.list);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            arrayValue.set(getCurrentValues());
            this.minecraft.setScreen(this.lastScreen);
        }).bounds(this.width / 2 - 155, this.height - 27, 150, 20).build());

        this.addRenderableWidget(Button
                .builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width / 2 + 5, this.height - 27, 150, 20)
                .build());
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        if (elements == null) {
            elements = Arrays.copyOf(arrayValue.get(), arrayValue.get().length);
        }
        int i = 0;
        for (T item : elements) {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, ArrayResetEntry.valueButtonWidth - 1, 20, Component.literal(item + ""));
            box.setMaxLength(1000);
            box.setValue(item + "");
            int index = i++;
            entries.add(new ArrayResetEntry(Component.empty(), box, () -> !fromString.apply(box.getValue()).equals(arrayValue.getDefault()[index]), button -> {elements[index] = arrayValue.get()[index];reinit = true;}));
        }
        return entries;
    }

    private T[] getCurrentValues(){
        return (T[]) list.children().stream().map(entry -> {
            if (entry instanceof ArrayResetEntry arrayResetEntry) {
                return fromString.apply(arrayResetEntry.getString());
            } else {
                return fromString.apply("");
            }
        }).collect(Collectors.toList()).toArray();
    }

    private static class ArrayResetEntry extends SettingsList.WidgetEntry {
        public static final int valueButtonWidth = 280;
        private final BooleanSupplier canReset;
        private final Button resetButton;

        public ArrayResetEntry(Component name, EditBox valueWidget, BooleanSupplier canReset, Button.OnPress resetAction) {
            super(name, valueWidget);
            this.canReset = canReset;

            this.resetButton = Button.builder(Component.literal("X"), resetAction)
                    .tooltip(Tooltip.create(Component.translatable("controls.reset")))
                    .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
            this.valueWidget.setX(k + -50);
            this.valueWidget.setY(j);
            this.valueWidget.render(guiGraphics, n, o, f);
            this.resetButton.setX(k + 230);
            this.resetButton.setY(j);
            this.resetButton.active = canReset.getAsBoolean();
            this.resetButton.render(guiGraphics, n, o, f);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        public String getString() {
            return ((EditBox)valueWidget).getValue();
        }
    }
}
