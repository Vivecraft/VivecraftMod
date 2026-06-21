package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gui.keyboard.KeyboardKeys;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiKeyboardLayoutEditor extends Screen {

    private final Screen parent;
    private boolean isShift = false;
    private boolean reinit = false;

    protected GuiKeyboardLayoutEditor(Screen parent) {
        super(Component.translatable("vivecraft.options.screen.customkeyboardeditor"));
        this.parent = parent;
    }

    @Override
    public void init() {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        this.clearWidgets();

        KeyboardKeys.Layout layout = KeyboardKeys.getRegularKeys(
            dh.vrSettings.keyboardLayouts.get("custom"), this.isShift, () -> {});

        int spacing = 2;
        int buttonWidth = (int) (25 * Math.min(this.width / ((25F + spacing) * (layout.columns() + 5F)), 1F));

        int yMargin = 32 + this.height / 2 - (int) ((layout.rows() + 3F) / 2F * (20 + spacing));
        int xMargin = this.width / 2 - ((buttonWidth + spacing) * (layout.columns() + 5)) / 2;

        for (KeyboardKeys.Key key : layout.keys()) {
            int y = key.y() < 0 ? layout.rows() - key.y() : key.y();
            EditBox box = new EditBox(this.font,
                xMargin + key.x() * (buttonWidth + spacing), yMargin + (y - 1) * (20 + spacing),
                buttonWidth, 20, Component.empty())
            {
                @Override
                public boolean charTyped(CharacterEvent event) {
                    this.setValue("");
                    return super.charTyped(event);
                }
            };
            box.setValue(key.label().getString());
            box.setMaxLength(1);
            box.setCentered(true);
            box.setResponder(s -> {
                char newChar = s.isEmpty() ? '\u0000' : s.charAt(0);
                if (this.isShift) {
                    char[] array = dh.vrSettings.keyboardKeysShift.toCharArray();
                    array[key.id() - 500] = newChar;
                    dh.vrSettings.keyboardKeysShift = new String(array);
                } else {
                    char[] array = dh.vrSettings.keyboardKeys.toCharArray();
                    array[key.id()] = newChar;
                    dh.vrSettings.keyboardKeys = new String(array);
                }
                dh.vrSettings.saveOptions();
                KeyboardHandler.reinitKeyboard();
            });

            this.addRenderableWidget(box);
        }

        for (KeyboardKeys.Key key : KeyboardKeys.getSpecialKeys()) {
            if (key.id() == KeyboardKeys.SHIFT_2.id()) {
                continue;
            }
            int y = key.y() < 0 ? layout.rows() - key.y() : key.y();
            this.addRenderableWidget(new Button.Builder(key.label(), p -> {
                if (key.isShift()) {
                    this.setShift(!this.isShift);
                }
            })
                .size(key.width() * buttonWidth + (key.width() - 1) * spacing, 20)
                .pos(xMargin + key.x() * (buttonWidth + spacing), yMargin + (y - 1) * (20 + spacing))
                .build()).active = key.id() == KeyboardKeys.SHIFT_1.id();
        }

        this.addRenderableWidget(new Button.Builder(Component.literal("+"), p -> {
            if (this.isShift) {
                dh.vrSettings.keyboardKeysShift += "\u0000".repeat(layout.columns());
            } else {
                dh.vrSettings.keyboardKeys += "\u0000".repeat(layout.columns());
            }
            dh.vrSettings.saveOptions();
            this.reinit = true;
            KeyboardHandler.reinitKeyboard();
        })
            .size(buttonWidth, 20)
            .pos((layout.columns() + 2) * (buttonWidth + spacing) + xMargin, yMargin + 3 * (20 + spacing))
            .tooltip(Tooltip.create(Component.translatable("vivecraft.options.screen.addkeyboardrow.tooltip")))
            .build()).active = layout.rows() < KeyboardKeys.MAX_ROWS;

        this.addRenderableWidget(new Button.Builder(Component.literal("-"), p -> {
            if (this.isShift) {
                dh.vrSettings.keyboardKeysShift = dh.vrSettings.keyboardKeysShift.substring(0,
                    dh.vrSettings.keyboardKeysShift.length() - layout.columns());
            } else {
                dh.vrSettings.keyboardKeys = dh.vrSettings.keyboardKeys.substring(0,
                    dh.vrSettings.keyboardKeys.length() - layout.columns());
            }
            dh.vrSettings.saveOptions();
            this.reinit = true;
            KeyboardHandler.reinitKeyboard();
        })
            .size(buttonWidth, 20)
            .pos((layout.columns() + 3) * (buttonWidth + spacing) + xMargin, yMargin + 3 * (20 + spacing))
            .tooltip(Tooltip.create(Component.translatable("vivecraft.options.screen.removekeyboardrow.tooltip")))
            .build()).active = layout.rows() > KeyboardKeys.ROWS;

        this.addRenderableWidget(new Button.Builder(
            Component.translatable("vivecraft.options.screen.loadkeyboardlayout.button"), button ->
            this.minecraft.setScreen(GuiActiveKeyboardLayoutSelector.getSelectionScreen(this,
                keyboard -> !keyboard.id().equals("custom"),
                keyboard -> {
                    VRSettings.KeyboardLayout newLayout =
                        keyboard == null ? dh.vrSettings.keyboardLayouts.get("en_us") : keyboard;
                    dh.vrSettings.keyboardKeys = newLayout.regular().get();
                    dh.vrSettings.keyboardKeysShift = newLayout.shift().get();
                    dh.vrSettings.saveOptions();
                    this.reinit = true;
                    this.isShift = false;
                    KeyboardHandler.reinitKeyboard();
                }, true)))
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
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
