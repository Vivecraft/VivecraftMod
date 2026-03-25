package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.framework.screens.GuiSelectionListScreen;
import org.vivecraft.client.gui.framework.widgets.ColorPicker;
import org.vivecraft.client.gui.framework.widgets.ColoredButton;
import org.vivecraft.client.gui.framework.widgets.ColoredKeyButton;
import org.vivecraft.client_vr.gui.keyboard.CustomKeyboardTheme;
import org.vivecraft.client_vr.gui.keyboard.KeyboardKeys;
import org.vivecraft.client_vr.gui.keyboard.KeyboardTheme;

import java.util.Arrays;

public class GuiKeyboardThemeEditor extends Screen {

    private final Screen parent;
    private boolean isShift = false;
    private boolean reinit = false;

    private final CustomKeyboardTheme customTheme = (CustomKeyboardTheme) KeyboardTheme.CUSTOM.theme;

    private final ColorPicker colorPicker;

    protected GuiKeyboardThemeEditor(Screen parent) {
        super(Component.translatable("vivecraft.options.screen.customkeyboardthemeeditor"));
        this.parent = parent;
        this.colorPicker = new ColorPicker(0, 0, 50, 40);
    }

    @Override
    public void init() {
        this.clearWidgets();

        KeyboardKeys.Layout layout = KeyboardKeys.getRegularKeys(this.isShift, () -> {});
        this.customTheme.reload();

        int spacing = 2;
        int buttonWidth = (int) (25 * Math.min(this.width / ((25F + spacing) * (layout.columns() + 5F)), 1F));

        int yMargin = 32 + this.height / 2 - (int) ((layout.rows() + 3F) / 2F * (20 + spacing));
        int xMargin = this.width / 2 - ((buttonWidth + spacing) * (layout.columns() + 5)) / 2;

        this.colorPicker.setPosition(xMargin + (buttonWidth + spacing) * 15, yMargin - 42);
        this.addRenderableWidget(this.colorPicker);

        for (KeyboardKeys.Key key : layout.keys()) {
            int y = key.y() < 0 ? layout.rows() - key.y() : key.y();
            this.addRenderableWidget(new ColoredKeyButton(key,
                xMargin + key.x() * (buttonWidth + spacing), yMargin + (y - 1) * (20 + spacing),
                buttonWidth, 20, b -> {
                if (Minecraft.getInstance().hasShiftDown()) {
                    this.colorPicker.setColor(((ColoredButton) b).getColor());
                } else {
                    this.customTheme.setColor(key.id(), this.colorPicker.getColor());
                    this.customTheme.save();
                }
            }, KeyboardTheme.CUSTOM));
        }

        for (KeyboardKeys.Key key : KeyboardKeys.getSpecialKeys()) {
            int y = key.y() < 0 ? layout.rows() - key.y() : key.y();
            this.addRenderableWidget(new ColoredKeyButton(key,
                xMargin + key.x() * (buttonWidth + spacing), yMargin + (y - 1) * (20 + spacing),
                key.width() * buttonWidth + (key.width() - 1) * spacing, 20, b -> {
                if (Minecraft.getInstance().hasShiftDown()) {
                    this.colorPicker.setColor(((ColoredButton) b).getColor());
                } else {
                    this.customTheme.setColor(key.id(), this.colorPicker.getColor());
                    this.customTheme.save();
                }
            }, KeyboardTheme.CUSTOM));
        }

        this.addRenderableWidget(
            CycleButton.onOffBuilder(this.isShift)
                .create(xMargin + 12 * (buttonWidth + spacing), yMargin - (20 + spacing),
                    3 * buttonWidth + 2 * spacing, 20,
                    Component.translatable("vivecraft.keyboard.key.shift"),
                    (p, b) -> this.setShift(!this.isShift)));

        this.addRenderableWidget(
            new Button.Builder(
                Component.translatable("vivecraft.options.screen.loadkeyboardtheme.button"), button ->
                this.minecraft.setScreen(new GuiSelectionListScreen<>(
                    Component.translatable("vivecraft.options.screen.loadkeyboardtheme"), this,
                    () -> Arrays.stream(KeyboardTheme.values()).filter(t -> t != KeyboardTheme.CUSTOM).toList(),
                    theme -> Component.translatable(theme.getLangKey()),
                    null,
                    theme -> {
                        this.customTheme.load(theme == null ? KeyboardTheme.DEFAULT : theme);
                        this.customTheme.save();
                    }, true, true, null)))
                .bounds(this.width / 2 - 155, this.height - 30, 150, 20)
                .build());

        this.addRenderableWidget(
            new Button.Builder(Component.translatable("gui.back"), p -> onClose())
                .bounds(this.width / 2 + 5, this.height - 30, 150, 20)
                .build());
    }

    private void setShift(boolean shift) {
        if (shift != this.isShift) {
            this.isShift = shift;
            this.reinit = true;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.reinit) {
            this.init();
            this.reinit = false;
        }
        graphics.centeredText(this.font, this.getTitle(), this.width / 2, 15, 0xFFFFFFFF);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, Component.translatable("vivecraft.messages.shifttopickcolor"),
            this.width / 2, 30, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
