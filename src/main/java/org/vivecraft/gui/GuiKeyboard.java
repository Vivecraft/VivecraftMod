package org.vivecraft.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import org.vivecraft.gui.framework.TwoHandedScreen;
import org.vivecraft.provider.InputSimulator;

public class GuiKeyboard extends TwoHandedScreen
{
    private boolean isShift = false;

    public void init()
    {
        String s = this.minecraft.vrSettings.keyboardKeys;
        String s1 = this.minecraft.vrSettings.keyboardKeysShift;
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
                Button button = new Button(k + k1 * (i1 + l), k + j1 * (20 + l), i1, 20, s2, (p) ->
                {
                    InputSimulator.typeChars(s2);
                });
                this.addRenderableWidget(button);
            }
        }

        this.addRenderableWidget(new Button(0, k + 3 * (20 + l), 30, 20, "Shift", (p) ->
        {
            this.setShift(!this.isShift);
        }));
        this.addRenderableWidget(new Button(k + 4 * (i1 + l), k + j * (20 + l), 5 * (i1 + l), 20, " ", (p) ->
        {
            InputSimulator.typeChars(" ");
        }));
        this.addRenderableWidget(new Button(i * (i1 + l) + k, k, 35, 20, "BKSP", (p) ->
        {
            InputSimulator.pressKey(259);
            InputSimulator.releaseKey(259);
        }));
        this.addRenderableWidget(new Button(i * (i1 + l) + k, k + 2 * (20 + l), 35, 20, "ENTER", (p) ->
        {
            InputSimulator.pressKey(257);
            InputSimulator.releaseKey(257);
        }));
        this.addRenderableWidget(new Button(0, k + 20 + l, 30, 20, "TAB", (p) ->
        {
            InputSimulator.pressKey(258);
            InputSimulator.releaseKey(258);
        }));
        this.addRenderableWidget(new Button(0, k, 30, 20, "ESC", (p) ->
        {
            InputSimulator.pressKey(256);
            InputSimulator.releaseKey(256);
        }));
        this.addRenderableWidget(new Button((i - 1) * (i1 + l) + k, k + j * (20 + l), i1, 20, "\u2191", (p) ->
        {
            InputSimulator.pressKey(265);
            InputSimulator.releaseKey(265);
        }));
        this.addRenderableWidget(new Button((i - 1) * (i1 + l) + k, k + (j + 1) * (20 + l), i1, 20, "\u2193", (p) ->
        {
            InputSimulator.pressKey(264);
            InputSimulator.releaseKey(264);
        }));
        this.addRenderableWidget(new Button((i - 2) * (i1 + l) + k, k + (j + 1) * (20 + l), i1, 20, "\u2190", (p) ->
        {
            InputSimulator.pressKey(263);
            InputSimulator.releaseKey(263);
        }));
        this.addRenderableWidget(new Button(i * (i1 + l) + k, k + (j + 1) * (20 + l), i1, 20, "\u2192", (p) ->
        {
            InputSimulator.pressKey(262);
            InputSimulator.releaseKey(262);
        }));
        this.addRenderableWidget(new Button(k, k + -1 * (20 + l), 35, 20, "CUT", (p) ->
        {
            InputSimulator.pressKey(341);
            InputSimulator.pressKey(88);
            InputSimulator.releaseKey(88);
            InputSimulator.releaseKey(341);
        }));
        this.addRenderableWidget(new Button(35 + l + k, k + -1 * (20 + l), 35, 20, "COPY", (p) ->
        {
            InputSimulator.pressKey(341);
            InputSimulator.pressKey(67);
            InputSimulator.releaseKey(67);
            InputSimulator.releaseKey(341);
        }));
        this.addRenderableWidget(new Button(2 * (35 + l) + k, k + -1 * (20 + l), 35, 20, "PASTE", (p) ->
        {
            InputSimulator.pressKey(341);
            InputSimulator.pressKey(86);
            InputSimulator.releaseKey(86);
            InputSimulator.releaseKey(341);
        }));
    }

    public void setShift(boolean shift)
    {
        if (shift != this.isShift)
        {
            this.isShift = shift;
            this.reinit = true;
        }
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, "Keyboard", this.width / 2, 2, 16777215);
        super.render(pMatrixStack, 0, 0, pPartialTicks);
    }
}
