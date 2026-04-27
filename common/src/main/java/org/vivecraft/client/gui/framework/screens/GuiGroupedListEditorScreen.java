package org.vivecraft.client.gui.framework.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.widgets.SettingsList;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class GuiGroupedListEditorScreen<T> extends GuiListScreen {
    private final Supplier<List<T>> valuesSupplier;
    private final Function<T, String> categorySupplier;
    private final Runnable loadDefaults;
    private final Consumer<List<T>> save;

    protected final boolean fixedEntryCount;

    protected List<T> elements;
    protected abstract T createNewValue(String category);

    public GuiGroupedListEditorScreen(
        Component title, Screen lastScreen, boolean fixedEntryCount, Supplier<List<T>> valuesSupplier,
        Runnable loadDefaults, Consumer<List<T>> save, @Nullable Function<T, String> categorySupplier)
    {
        super(title, lastScreen);
        this.fixedEntryCount = fixedEntryCount;
        this.valuesSupplier = valuesSupplier;
        this.loadDefaults = loadDefaults;
        this.save = save;
        this.categorySupplier = categorySupplier != null ? categorySupplier : item -> "Empty";
    }

    @Override
    protected void init() {
        super.init();
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
            if (entry instanceof GuiListEditorScreen.ValueEntry<?> valueEntry) {
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

        Map<String, List<T>> grouped = new LinkedHashMap<>();

        for (T element : this.elements) {
            String category = this.categorySupplier.apply(element);
            grouped.computeIfAbsent(category, k -> new LinkedList<>())
                .add(element);
        }

        int index = 0;
        for (Map.Entry<String, List<T>> group : grouped.entrySet()) {

            String category = group.getKey();
            List<T> values = group.getValue();

            if (!category.isEmpty()) {
                entries.add(new SettingsList.GroupedEntry(
                    Component.translatable(category)
                ));
            }

            for (T value : values) {
                entries.add(this.toEntry(value, index++));
            }

            if (!this.fixedEntryCount && !category.isEmpty()) {
                entries.add(createAddButton(category));
            }
        }

        return entries;
    }

    private SettingsList.BaseEntry createAddButton(String category) {
        return new SettingsList.WidgetEntry(
            Component.literal(""),
            Button.builder(Component.translatable("vivecraft.options.add"), button -> {
                this.addNewValue(category);
            }).size(50, 20).build()
        );
    }

    protected void addNewValue(String category) {
        this.elements = getCurrentValues();
        this.elements.add(this.createNewValue(category));
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
