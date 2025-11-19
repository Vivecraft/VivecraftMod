package org.vivecraft.client.gui.settings;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiListScreen;
import org.vivecraft.client.gui.framework.widgets.SettingsList;
import org.vivecraft.client_vr.provider.control.ActionType;
import org.vivecraft.client_vr.provider.control.VRInputAction;
import org.vivecraft.client_vr.provider.openxr.MCOpenXR;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class GuiBindings extends GuiListScreen {


    public GuiBindings(Screen lastScreen) {
        super(Component.translatable("vivecraft.options.screen.bindings"), lastScreen);
    }

    @Override
    protected List<SettingsList.BaseEntry> getEntries() {
        List<SettingsList.BaseEntry> list = new LinkedList<>();
        for (var entry : MCOpenXR.get().getBinds().entrySet()) {
            var input = MCOpenXR.get().getInputActionByName(entry.getKey());
            if (input == null) continue;
            //list.add(new ResettableEntry(Component.translatable(input.name), input));
        }
        return list;
    }

    public static class ResettableEntry extends SettingsList.WidgetEntry {
        public static final int VALUE_BUTTON_WIDTH = 125;

        private final Button resetButton;
        private final BooleanSupplier canReset;

        public ResettableEntry(Component name, VRInputAction action) {
            super(name, getBaseWidget(action, VALUE_BUTTON_WIDTH, 20).get());

            this.canReset = () -> action.type != ActionType.BOOLEAN;
            this.resetButton = Button.builder(Component.literal("X"), button -> {
                    action.setType(ActionType.BOOLEAN);
                    this.valueWidget = getBaseWidget(action, valueWidget.getWidth(), valueWidget.getHeight()).get();
                })
                .tooltip(Tooltip.create(Component.translatable("controls.reset")))
                .bounds(0, 0, 20, 20).build();
        }

        @Override
        public void renderContent(
            GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovering, float partialTick)
        {
            super.renderContent(guiGraphics, mouseX, mouseY, hovering, partialTick);
            this.resetButton.setX(this.getContentRight() - 20);
            this.resetButton.setY(this.getContentY());
            this.resetButton.active = this.canReset.getAsBoolean();
            this.resetButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of(this.valueWidget, this.resetButton);
        }

        @Override
        public void setActive(boolean active) {
            super.setActive(active);
            this.resetButton.active = active;
        }
    }

    public static Supplier<AbstractWidget> getBaseWidget(VRInputAction action, int width, int height) {
        return () -> Button
            .builder(Component.literal("" + action.type), button -> {})
            .bounds(0, 0, width, height)
            .tooltip(Tooltip.create(Component.literal(action.name)))
            .build();
    }
}
