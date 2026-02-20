package org.vivecraft.client.gui.framework.screens;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
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
        this.categorySupplier = categorySupplier != null ? categorySupplier : item -> "";
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

            SettingsList.GroupedEntry categoryEntry = null;

            if (!category.isEmpty()) {
                categoryEntry = new SettingsList.GroupedEntry(
                    Component.translatable(category));
                entries.add(categoryEntry);
            }

            for (T value : values) {
                SettingsList.BaseEntry entry = this.toEntry(value, index++);

                if (categoryEntry != null) {
                    categoryEntry.add(entry);
                } else {
                    entries.add(entry);
                }
            }

            if (!this.fixedEntryCount && categoryEntry != null) {
                categoryEntry.add(createAddButton(category));
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

    protected static class RemovableEntry<T> extends ValueEntry<T> {
        protected final T value;

        private final int index;
        private final Button editButton;
        private final Button removeButton;

        public RemovableEntry(Component name, T value, int index) {
            super(name, null);
            this.value = value;
            this.index = index;

            this.editButton = Button.builder(Component.translatable("vivecraft.options.edit"),
                    button -> {})
                .bounds(0, 0, 100, 20).build();
            this.removeButton = Button.builder(Component.literal("-"),
                    button -> {})
                .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void renderContent(
            GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick)
        {
            super.renderContent(guiGraphics, mouseX, mouseY, hovering, partialTick);

            int textY = this.getY() + this.getHeight() / 2 - Minecraft.getInstance().font.lineHeight / 2 + 2;
            guiGraphics.drawString(Minecraft.getInstance().font, this.name, this.getContentX(), textY,
                this.textColor());
            this.removeButton.active = this.isActive();
            this.editButton.active = this.isActive();

            this.editButton.setX(this.getContentRight() - this.editButton.getWidth() - 20);
            this.editButton.setY(this.getY());
            this.editButton.render(guiGraphics, mouseX, mouseY, partialTick);
            this.removeButton.setX(this.getContentRight() - 20);
            this.removeButton.setY(this.getY());
            this.removeButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.editButton, this.removeButton);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editButton, this.removeButton);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.removeButton.active = active;
            this.editButton.active = active;
        }

        @Override
        public T getValue() {
            return this.value;
        }
    }

    protected static abstract class ValueEntry<T> extends SettingsList.BaseEntry {
        public ValueEntry(Component name, Supplier<String> tooltipSupplier) {
            super(name, tooltipSupplier);
        }

        public abstract T getValue();
    }
}
