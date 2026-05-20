package org.vivecraft.client.gui.framework.screens;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GuiStringListEditorScreen extends GuiListEditorScreen<String> {

    public GuiStringListEditorScreen(
        Component title, Screen lastScreen, boolean fixedEntryCount, Supplier<List<String>> valuesSupplier,
        Runnable loadDefaults, Consumer<List<String>> save)
    {
        super(title, lastScreen, fixedEntryCount, valuesSupplier, loadDefaults, save);

        // can't search text boxes
        this.searchable = false;
    }

    @Override
    protected void addNewValue() {
        super.addNewValue();
        this.elements.add("");
    }

    @Override
    protected ValueEntry<String> toEntry(String value, int index) {
        EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, 350, 20, Component.literal(value));
        box.setMaxLength(1000);
        box.setValue(value);
        box.setResponder(s -> this.elements.set(index, s));
        return new StringValueEntry(Component.empty(), box, button -> {
            this.elements.remove(index);
            this.reinit = true;
        }, !this.fixedEntryCount);
    }

    private static class StringValueEntry extends ValueEntry<String> {

        private final EditBox editBox;
        private final Button deleteButton;

        public StringValueEntry(Component name, EditBox editBox, Button.OnPress deleteAction, boolean deletable) {
            super(name, null);
            this.editBox = editBox;
            this.deleteButton = Button
                .builder(Component.literal(deletable ? "-" : "X"),
                    deletable ? deleteAction : b -> this.editBox.setValue(""))
                .tooltip(Tooltip.create(Component.translatable("selectWorld.delete")))
                .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void extractContent(
            GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick)
        {
            this.editBox.setX(this.getContentX());
            this.editBox.setY(this.getY());
            this.editBox.setWidth(this.getContentWidth() - 20);
            this.editBox.extractRenderState(graphics, mouseX, mouseY, partialTick);
            this.deleteButton.setX(this.getContentRight() - 20);
            this.deleteButton.setY(this.getY());
            this.deleteButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean keyPressed(KeyEvent keyEvent) {
            // allow to navigate off the edit box
            if (this.getFocused() == this.editBox && keyEvent.key() == GLFW.GLFW_KEY_RIGHT &&
                this.editBox.getValue().length() == this.editBox.getCursorPosition())
            {
                return false;
            }
            return super.keyPressed(keyEvent);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.editBox, this.deleteButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.editBox, this.deleteButton);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.editBox.active = active;
            this.deleteButton.active = active;
        }

        public String getValue() {
            return this.editBox.getValue();
        }
    }
}
