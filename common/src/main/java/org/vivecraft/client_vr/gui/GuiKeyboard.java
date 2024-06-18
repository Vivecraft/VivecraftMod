package org.vivecraft.client_vr.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.framework.TwoHandedScreen;
import org.vivecraft.client_vr.provider.InputSimulator;

public class GuiKeyboard extends TwoHandedScreen {
    private boolean isShift = false;

    @Override
    public void init() {
        String keys = this.dh.vrSettings.keyboardKeys;
        String shiftKeys = this.dh.vrSettings.keyboardKeysShift;
        this.clearWidgets();

        if (this.isShift) {
            keys = shiftKeys;
        }

        int columns = 13;
        int rows;
        int margin = 32;
        int spacing = 2;
        int buttonWidth = 25;
        double rowsD = (double) keys.length() / (double) columns;

        if (Math.floor(rowsD) == rowsD) {
            rows = (int) rowsD;
        } else {
            rows = (int) (rowsD + 1.0D);
        }

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int index = row * columns + column;
                char buttonChar = ' ';

                if (index < keys.length()) {
                    buttonChar = keys.charAt(index);
                }

                String label = String.valueOf(buttonChar);
                final int code = index < this.dh.vrSettings.keyboardCodes.length ?
                    this.dh.vrSettings.keyboardCodes[index] : GLFW.GLFW_KEY_UNKNOWN;
                this.addRenderableWidget(new Button.Builder(Component.literal(label),
                    (p) -> {
                        InputSimulator.pressKeyForBind(code);
                        InputSimulator.releaseKeyForBind(code);
                        InputSimulator.typeChars(label);
                    })
                    .size(buttonWidth, 20)
                    .pos(margin + column * (buttonWidth + spacing), margin + row * (20 + spacing))
                    .build());
            }
        }

        this.addRenderableWidget(new Button.Builder(Component.literal("Shift"),
            (p) -> this.setShift(!this.isShift))
            .size(30, 20)
            .pos(0, margin + 3 * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal(" "),
            (p) -> {
                InputSimulator.pressKeyForBind(GLFW.GLFW_KEY_SPACE);
                InputSimulator.releaseKeyForBind(GLFW.GLFW_KEY_SPACE);
                InputSimulator.typeChars(" ");
            })
            .size(5 * (buttonWidth + spacing), 20)
            .pos(margin + 4 * (buttonWidth + spacing), margin + rows * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("BKSP"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_BACKSPACE);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_BACKSPACE);
            })
            .size(35, 20)
            .pos(columns * (buttonWidth + spacing) + margin, margin)
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("ENTER"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_ENTER);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_ENTER);
            })
            .size(35, 20)
            .pos(columns * (buttonWidth + spacing) + margin, margin + 2 * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("TAB"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_TAB);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_TAB);
            })
            .size(30, 20)
            .pos(0, margin + 20 + spacing)
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("ESC"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_ESCAPE);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_ESCAPE);
            })
            .size(30, 20)
            .pos(0, margin)
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("\u2191"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_UP);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_UP);
            })
            .size(buttonWidth, 20)
            .pos((columns - 1) * (buttonWidth + spacing) + margin, margin + rows * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("\u2193"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_DOWN);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_DOWN);
            })
            .size(buttonWidth, 20)
            .pos((columns - 1) * (buttonWidth + spacing) + margin, margin + (rows + 1) * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("\u2190"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT);
            })
            .size(buttonWidth, 20)
            .pos((columns - 2) * (buttonWidth + spacing) + margin, margin + (rows + 1) * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("\u2192"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_RIGHT);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_RIGHT);
            })
            .size(buttonWidth, 20)
            .pos(columns * (buttonWidth + spacing) + margin, margin + (rows + 1) * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("CUT"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            })
            .size(35, 20)
            .pos(margin, margin + -1 * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("COPY"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            })
            .size(35, 20)
            .pos(35 + spacing + margin, margin + -1 * (20 + spacing))
            .build());

        this.addRenderableWidget(new Button.Builder(Component.literal("PASTE"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            })
            .size(35, 20)
            .pos(2 * (35 + spacing) + margin, margin + -1 * (20 + spacing))
            .build());
    }

    public void setShift(boolean shift) {
        if (shift != this.isShift) {
            this.isShift = shift;
            this.reinit = true;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, "Keyboard", this.width / 2, 2, 0xFFFFFF);
        super.render(guiGraphics, 0, 0, partialTick);
    }
}
