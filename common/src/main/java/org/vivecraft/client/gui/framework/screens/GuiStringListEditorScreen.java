package org.vivecraft.client.gui.framework.screens;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.framework.widgets.SettingsList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GuiStringListEditorScreen extends GuiListScreen {

    private final Supplier<List<String>> valuesSupplier;
    private final Runnable loadDefaults;
    private final Consumer<List<String>> save;
    private final boolean fixedEntryCount;

    private List<String> elements;

    public GuiStringListEditorScreen(
        Component title, Screen lastScreen, boolean fixedEntryCount, Supplier<List<String>> valuesSupplier,
        Runnable loadDefaults, Consumer<List<String>> save)
    {
        super(title, lastScreen);
        this.fixedEntryCount = fixedEntryCount;
        this.valuesSupplier = valuesSupplier;
        this.loadDefaults = loadDefaults;
        this.save = save;

        // can't search text boxes
        this.searchable = false;
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

    private List<String> getCurrentValues() {
        return this.list.children().stream().map(entry -> {
            if (entry instanceof StringValueEntry listValueEntry) {
                return listValueEntry.getString();
            } else {
                return "";
            }
        }).filter(string -> !string.isEmpty()).collect(Collectors.toList());
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> entries = new LinkedList<>();
        if (this.elements == null) {
            this.elements = new ArrayList<>(this.valuesSupplier.get());
        }
        int i = 0;
        for (String item : this.elements) {
            EditBox box = new EditBox(this.minecraft.font, 0, 0, 350, 20, Component.literal(item));
            box.setMaxLength(1000);
            box.setValue(item);
            int index = i++;
            box.setResponder(s -> this.elements.set(index, s));
            entries.add(new StringValueEntry(Component.empty(), box, button -> {
                this.elements.remove(index);
                this.reinit = true;
            }, !this.fixedEntryCount));
        }

        if (!this.fixedEntryCount) {
            entries.add(new SettingsList.WidgetEntry(Component.literal(""),
                Button.builder(Component.translatable("vivecraft.options.addnew"), button -> {
                    this.elements = getCurrentValues();
                    this.elements.add("");
                    this.reinit = true;
                }).size(SettingsList.WidgetEntry.VALUE_BUTTON_WIDTH, 20).build()));
        }
        return entries;
    }

    private static class StringValueEntry extends SettingsList.BaseEntry {

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
        public void render(
            GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY,
            boolean hovering, float partialTick)
        {
            this.editBox.setX(left);
            this.editBox.setY(top);
            this.editBox.setWidth(width - 20);
            this.editBox.render(guiGraphics, mouseX, mouseY, partialTick);
            this.deleteButton.setX(left + width - 20);
            this.deleteButton.setY(top);
            this.deleteButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            // allow to navigate off the edit box
            if (this.getFocused() == this.editBox && keyCode == GLFW.GLFW_KEY_RIGHT &&
                this.editBox.getValue().length() == this.editBox.getCursorPosition())
            {
                return false;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
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

        public String getString() {
            return this.editBox.getValue();
        }
    }
}
