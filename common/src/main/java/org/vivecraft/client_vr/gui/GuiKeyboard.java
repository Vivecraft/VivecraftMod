package org.vivecraft.client_vr.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.TextComponent;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.framework.screens.TwoHandedScreen;
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
                this.addRenderableWidget(new Button(
                    margin + column * (buttonWidth + spacing), margin + row * (20 + spacing), buttonWidth, 20,
                    new TextComponent(label),
                    (p) -> {
                        InputSimulator.pressKeyForBind(code);
                        InputSimulator.releaseKeyForBind(code);
                        InputSimulator.typeChars(label);
                    }));
            }
        }

        this.addRenderableWidget(new Button(0, margin + 3 * (20 + spacing), 30, 20,
            new TextComponent("Shift"),
            (p) -> this.setShift(!this.isShift)));

        this.addRenderableWidget(new Button(
            margin + 4 * (buttonWidth + spacing), margin + rows * (20 + spacing), 5 * (buttonWidth + spacing), 20,
            new TextComponent(" "),
            (p) -> {
                InputSimulator.pressKeyForBind(GLFW.GLFW_KEY_SPACE);
                InputSimulator.releaseKeyForBind(GLFW.GLFW_KEY_SPACE);
                InputSimulator.typeChars(" ");
            }));

        this.addRenderableWidget(new Button(
            columns * (buttonWidth + spacing) + margin, margin, 35, 20,
            new TextComponent("BKSP"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_BACKSPACE);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_BACKSPACE);
            }));

        this.addRenderableWidget(new Button(
            columns * (buttonWidth + spacing) + margin, margin + 2 * (20 + spacing), 35, 20,
            new TextComponent("ENTER"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_ENTER);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_ENTER);
            }));

        this.addRenderableWidget(new Button(0, margin + 20 + spacing, 30, 20,
            new TextComponent("TAB"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_TAB);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_TAB);
            }));

        this.addRenderableWidget(new Button(0, margin, 30, 20,
            new TextComponent("ESC"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_ESCAPE);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_ESCAPE);
            }));

        this.addRenderableWidget(new Button(
            (columns - 1) * (buttonWidth + spacing) + margin, margin + rows * (20 + spacing), buttonWidth, 20,
            new TextComponent("\u2191"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_UP);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_UP);
            }));

        this.addRenderableWidget(new Button(
            (columns - 1) * (buttonWidth + spacing) + margin, margin + (rows + 1) * (20 + spacing), buttonWidth, 20,
            new TextComponent("\u2193"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_DOWN);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_DOWN);
            }));

        this.addRenderableWidget(new Button(
            (columns - 2) * (buttonWidth + spacing) + margin, margin + (rows + 1) * (20 + spacing), buttonWidth, 20,
            new TextComponent("\u2190"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT);
            }));

        this.addRenderableWidget(new Button(
            columns * (buttonWidth + spacing) + margin, margin + (rows + 1) * (20 + spacing), buttonWidth, 20,
            new TextComponent("\u2192"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_RIGHT);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_RIGHT);
            }));

        this.addRenderableWidget(new Button(
            margin, margin + -1 * (20 + spacing), 35, 20,
            new TextComponent("CUT"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            }));

        this.addRenderableWidget(new Button(
            35 + spacing + margin, margin + -1 * (20 + spacing), 35, 20,
            new TextComponent("COPY"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            }));

        this.addRenderableWidget(new Button(
            2 * (35 + spacing) + margin, margin + -1 * (20 + spacing), 35, 20,
            new TextComponent("PASTE"),
            (p) -> {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            }));
    }

    public void setShift(boolean shift) {
        if (shift != this.isShift) {
            this.isShift = shift;
            this.reinit = true;
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(poseStack);
        drawCenteredString(poseStack, this.font, "Keyboard", this.width / 2, 2, 0xFFFFFFFF);
        super.render(poseStack, 0, 0, partialTick);
    }
}
