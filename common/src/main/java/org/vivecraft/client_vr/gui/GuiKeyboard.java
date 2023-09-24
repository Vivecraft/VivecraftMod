package org.vivecraft.client_vr.gui;

import org.vivecraft.client.gui.framework.TwoHandedScreen;
import org.vivecraft.client_vr.provider.InputSimulator;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.dh;

import static org.joml.Math.*;
import static org.lwjgl.glfw.GLFW.*;

public class GuiKeyboard extends TwoHandedScreen
{
    private boolean isShift = false;

    public void init()
    {
        String arr = dh.vrSettings.keyboardKeys;
        String alt = dh.vrSettings.keyboardKeysShift;
        this.clearWidgets();

        if (this.isShift)
        {
            arr = alt;
        }

        int cols = 13;
        int rows = 4;
        int margin = 32;
        int spacing = 2;
        int bwidth = 25;
        double tmp = (double)arr.length() / cols;

        if (floor(tmp) == tmp)
        {
            rows = (int)tmp;
        }
        else
        {
            rows = (int)(tmp + 1.0D);
        }

        for (int r = 0; r < rows; ++r)
        {
            for (int i = 0; i < cols; ++i)
            {
                int c = (r * cols) + i;
                char x = ' ';

                if (c < arr.length())
                {
                    x = arr.charAt(c);
                }

                final String c1 = String.valueOf(x);
                Button button = new Builder(Component.literal(c1), (p) -> InputSimulator.typeChars(c1))
                    .size(bwidth, 20)
                    .pos(margin + i * (bwidth + spacing), margin + r * (20 + spacing))
                    .build();
                this.addRenderableWidget(button);
            }
        }

        this.addRenderableWidget(new Builder(Component.literal("Shift"), (p) -> this.setShift(!this.isShift))
            .size(30, 20)
            .pos(0, margin + 3 * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal(" "),  (p) -> InputSimulator.typeChars(" "))
            .size(5 * (bwidth + spacing),  20)
            .pos(margin + 4 * (bwidth + spacing), margin + rows * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("BKSP"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_BACKSPACE);
                InputSimulator.releaseKey(GLFW_KEY_BACKSPACE);
            })
            .size(35, 20)
            .pos(cols * (bwidth + spacing) + margin,  margin)
            .build());
        this.addRenderableWidget(new Builder(Component.literal("ENTER"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_ENTER);
                InputSimulator.releaseKey(GLFW_KEY_ENTER);
            })
            .size(35, 20)
            .pos(cols * (bwidth + spacing) + margin, margin + 2 * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("TAB"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_TAB);
                InputSimulator.releaseKey(GLFW_KEY_TAB);
            })
            .size(30, 20)
            .pos(0,  margin + 20 + spacing)
            .build());
        this.addRenderableWidget(new Builder(Component.literal("ESC"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_ESCAPE);
                InputSimulator.releaseKey(GLFW_KEY_ESCAPE);
            })
            .size(30, 20)
            .pos(0,  margin)
            .build());
        this.addRenderableWidget(new Builder(Component.literal("↑"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_UP);
                InputSimulator.releaseKey(GLFW_KEY_UP);
            })
            .size(bwidth, 20)
            .pos((cols - 1) * (bwidth + spacing) + margin, margin + rows * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("↓"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_DOWN);
                InputSimulator.releaseKey(GLFW_KEY_DOWN);
            })
            .size(bwidth, 20)
            .pos((cols - 1) * (bwidth + spacing) + margin, margin + (rows + 1) * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("←"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT);
                InputSimulator.releaseKey(GLFW_KEY_LEFT);
            })
            .size(bwidth, 20)
            .pos((cols - 2) * (bwidth + spacing) + margin, margin + (rows + 1) * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("→"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_RIGHT);
                InputSimulator.releaseKey(GLFW_KEY_RIGHT);
            })
            .size(bwidth, 20)
            .pos(cols * (bwidth + spacing) + margin, margin + (rows + 1) * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("CUT"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW_KEY_LEFT_CONTROL);
            })
            .size(35, 20)
            .pos(margin, margin + -1 * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("COPY"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW_KEY_LEFT_CONTROL);
            })
            .size(35, 20)
            .pos(35 + spacing + margin, margin + -1 * (20 + spacing))
            .build());
        this.addRenderableWidget(new Builder(Component.literal("PASTE"), (p) ->
            {
                InputSimulator.pressKey(GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW_KEY_LEFT_CONTROL);
            })
            .size(35, 20)
            .pos(2 * (35 + spacing) + margin, margin + -1 * (20 + spacing))
            .build());
    }

    public void setShift(boolean shift)
    {
        if (shift != this.isShift)
        {
            this.isShift = shift;
            this.reinit = true;
        }
    }

    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, "Keyboard", this.width / 2, 2, 16777215);
        super.render(guiGraphics, 0, 0, pPartialTicks);
    }
}
