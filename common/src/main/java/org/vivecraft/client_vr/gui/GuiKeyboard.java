package org.vivecraft.client_vr.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.framework.TwoHandedScreen;
import org.vivecraft.client_vr.provider.InputSimulator;

import net.minecraft.client.gui.components.Button;

public class GuiKeyboard extends TwoHandedScreen
{
    private boolean isShift = false;

    public void init()
    {
        String s = this.dataholder.vrSettings.keyboardKeys;
        String s1 = this.dataholder.vrSettings.keyboardKeysShift;
        this.clearWidgets();

        if (this.isShift)
        {
            s = s1;
        }

        int i = 13;
        int j = 4;
        int k = 32;
        int l = 2;
        int i1 = 25;
        double d0 = (double)s.length() / (double)i;

        if (Math.floor(d0) == d0)
        {
            j = (int)d0;
        }
        else
        {
            j = (int)(d0 + 1.0D);
        }

        for (int j1 = 0; j1 < j; ++j1)
        {
            for (int k1 = 0; k1 < i; ++k1)
            {
                int l1 = j1 * i + k1;
                char c0 = ' ';

                if (l1 < s.length())
                {
                    c0 = s.charAt(l1);
                }

                String s2 = String.valueOf(c0);
                Button button = new Button.Builder( Component.literal(s2),  (p) ->
                    {
                        InputSimulator.typeChars(s2);
                    })
                    .size( i1,  20)
                    .pos(k + k1 * (i1 + l),  k + j1 * (20 + l))
                    .build();
                this.addRenderableWidget(button);
            }
        }

        this.addRenderableWidget(new Button.Builder( Component.literal("Shift"),  (p) ->
            {
                this.setShift(!this.isShift);
            })
            .size( 30,  20)
            .pos(0,  k + 3 * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal(" "),  (p) ->
            {
                InputSimulator.typeChars(" ");
            })
            .size( 5 * (i1 + l),  20)
            .pos(k + 4 * (i1 + l),  k + j * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("BKSP"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_BACKSPACE);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_BACKSPACE);
            })
            .size( 35,  20)
            .pos(i * (i1 + l) + k,  k)
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("ENTER"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_ENTER);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_ENTER);
            })
            .size( 35,  20)
            .pos(i * (i1 + l) + k,  k + 2 * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("TAB"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_TAB);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_TAB);
            })
            .size( 30,  20)
            .pos(0,  k + 20 + l)
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("ESC"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_ESCAPE);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_ESCAPE);
            })
            .size( 30,  20)
            .pos(0,  k)
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("\u2191"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_UP);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_UP);
            })
            .size( i1,  20)
            .pos((i - 1) * (i1 + l) + k,  k + j * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("\u2193"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_DOWN);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_DOWN);
            })
            .size( i1,  20)
            .pos((i - 1) * (i1 + l) + k,  k + (j + 1) * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("\u2190"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT);
            })
            .size( i1,  20)
            .pos((i - 2) * (i1 + l) + k,  k + (j + 1) * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("\u2192"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_RIGHT);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_RIGHT);
            })
            .size( i1,  20)
            .pos(i * (i1 + l) + k,  k + (j + 1) * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("CUT"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_X);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            })
            .size( 35,  20)
            .pos(k,  k + -1 * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("COPY"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_C);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            })
            .size( 35,  20)
            .pos(35 + l + k,  k + -1 * (20 + l))
            .build());
        this.addRenderableWidget(new Button.Builder( Component.literal("PASTE"),  (p) ->
            {
                InputSimulator.pressKey(GLFW.GLFW_KEY_LEFT_CONTROL);
                InputSimulator.pressKey(GLFW.GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_V);
                InputSimulator.releaseKey(GLFW.GLFW_KEY_LEFT_CONTROL);
            })
            .size( 35,  20)
            .pos(2 * (35 + l) + k,  k + -1 * (20 + l))
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
