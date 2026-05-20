package org.vivecraft.client.gui.framework.screens;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class GuiOrderedListEditorScreen<T> extends GuiListEditorScreen<T> {

    public GuiOrderedListEditorScreen(
        Component title, Screen lastScreen, boolean fixedEntryCount, Supplier<List<T>> valuesSupplier,
        Runnable loadDefaults, Consumer<List<T>> save)
    {
        super(title, lastScreen, fixedEntryCount, valuesSupplier, loadDefaults, save);

        // the underlying list is ordered, so can't filter it
        this.searchable = false;
    }

    private void moveEntry(int from, int offset, boolean remove) {
        T value = this.elements.get(from);
        this.elements.remove(from);
        if (!remove) {
            this.elements.add(Math.clamp(from + offset, 0, this.elements.size()), value);
        }
        this.reinit = true;
    }

    protected class OrderedEntry<T> extends ValueEntry<T> {

        protected final T value;

        private final int index;
        private final Button upButton;
        private final Button downButton;
        private final Button removeButton;

        public OrderedEntry(Component name, T value, int index) {
            super(name, null);
            this.value = value;
            this.index = index;
            this.upButton = Button.builder(Component.literal("\u2191"),
                    button -> moveEntry(index, -1, false))
                .bounds(0, 0, 20, 20).build();
            this.downButton = Button.builder(Component.literal("\u2193"),
                    button -> moveEntry(index, 1, false))
                .bounds(0, 0, 20, 20).build();
            this.removeButton = Button.builder(Component.literal("-"),
                    button -> moveEntry(index, 0, true))
                .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void extractContent(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick)
        {
            super.extractContent(graphics, mouseX, mouseY, hovering, partialTick);

            int textY = this.getY() + this.getHeight() / 2 - Minecraft.getInstance().font.lineHeight / 2 + 2;
            graphics.text(Minecraft.getInstance().font, this.getMessage(), this.getContentX(), textY,
                0xFFFFFFFF);

            this.upButton.active = this.isActive() && this.index != 0;
            this.downButton.active =
                this.isActive() && this.index != GuiOrderedListEditorScreen.this.elements.size() - 1;
            this.removeButton.active = this.isActive();

            int i = 0;
            for (GuiEventListener child : this.children()) {
                Button b = (Button) child;
                b.setX(this.getContentRight() - (2 - i) * 22 - 20);
                b.setY(this.getContentY());
                b.extractRenderState(graphics, mouseX, mouseY, partialTick);
                i++;
            }
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.upButton, this.downButton, this.removeButton);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.upButton, this.downButton, this.removeButton);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.upButton.active = active;
            this.downButton.active = active;
            this.removeButton.active = active;
        }

        @Override
        public T getValue() {
            return this.value;
        }
    }
}
