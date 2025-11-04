package org.vivecraft.client.gui.framework.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.widgets.SettingsList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class GuiListEditorScreen<T> extends GuiListScreen {

    private final Supplier<List<T>> valuesSupplier;
    private final Runnable loadDefaults;
    private final Consumer<List<T>> save;

    protected final boolean fixedEntryCount;

    protected List<T> elements;

    public GuiListEditorScreen(
        Component title, Screen lastScreen, boolean fixedEntryCount, Supplier<List<T>> valuesSupplier,
        Runnable loadDefaults, Consumer<List<T>> save)
    {
        super(title, lastScreen);
        this.fixedEntryCount = fixedEntryCount;
        this.valuesSupplier = valuesSupplier;
        this.loadDefaults = loadDefaults;
        this.save = save;
    }

    @Override
    protected void addLowerButtons(int top) {
        this.addRenderableWidget(
            Button.builder(Component.translatable("vivecraft.gui.loaddefaults"), button -> {
                    this.loadDefaults.run();
                    this.elements = null;
                    this.reinit = true;
                })
                .bounds(this.width / 2 - 155, top, 150, 20)
                .build());

        this.addRenderableWidget(
            Button.builder(Component.translatable("gui.back"), button -> this.onClose())
                .bounds(this.width / 2 + 5, top, 150, 20)
                .build());
    }

    @Override
    public void onClose() {
        this.save.accept(this.elements);
        super.onClose();
    }

    @SuppressWarnings("unchecked")
    protected List<T> getCurrentValues() {
        return this.list.children().stream().map(entry -> {
            if (entry instanceof ValueEntry<?> valueEntry) {
                return (T) valueEntry.getValue();
            } else {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        if (this.elements == null) {
            this.elements = new ArrayList<>(this.valuesSupplier.get());
        }
        int index = 0;
        for (T item : this.elements) {
            entries.add(this.toEntry(item, index++));
        }

        if (!this.fixedEntryCount) {
            entries.add(new SettingsList.WidgetEntry(Component.literal(""),
                Button.builder(Component.translatable("vivecraft.options.addnew"), button -> {
                    this.addNewValue();
                }).size(SettingsList.WidgetEntry.VALUE_BUTTON_WIDTH, 20).build()));
        }
        return entries;
    }

    protected void addNewValue() {
        this.elements = getCurrentValues();
        this.reinit = true;
    }

    protected abstract ValueEntry<T> toEntry(T value, int index);

    protected static abstract class ValueEntry<T> extends SettingsList.BaseEntry {
        public ValueEntry(Component name, Supplier<String> tooltipSupplier) {
            super(name, tooltipSupplier);
        }

        public abstract T getValue();
    }
}
