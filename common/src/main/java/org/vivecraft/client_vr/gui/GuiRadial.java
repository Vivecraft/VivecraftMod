package org.vivecraft.client_vr.gui;

import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.dh;

import static org.joml.Math.*;

public class GuiRadial extends org.vivecraft.client.gui.framework.TwoHandedScreen
{
    private boolean isShift = false;
    String[] arr;

    public void init()
    {
        this.arr = dh.vrSettings.vrRadialItems;
        String[] alt = dh.vrSettings.vrRadialItemsAlt;
        this.clearWidgets();

        int numButts = 8;
        int buttonWidthMin = 120;
        int degreesPerButt = 360 / numButts;
        int dist = 48;
        int centerx = this.width / 2;
        int centery = this.height / 2;

        if (this.isShift)
        {
            this.arr = alt;
        }

        for (int i = 0; i < numButts; ++i)
        {
            KeyMapping b = null;

            for (KeyMapping kb : this.minecraft.options.keyMappings)
            {
                if (kb.getName().equalsIgnoreCase(this.arr[i]))
                {
                    b = kb;
                }
            }

            String str = "?";

            if (b != null)
            {
                str = I18n.get(b.getName());
            }

            int buttonwidth = max(buttonWidthMin, this.font.width(str));
            int x = 0, y = 0;

            if (i == 0)
            {
                y = -dist;
            }
            else if (i == 1)
            {
                x = buttonwidth / 2 + 8;
                y = -dist / 2;
            }
            else if (i == 2)
            {
                x = buttonwidth / 2 + 32;
            }
            else if (i == 3)
            {
                x = buttonwidth / 2 + 8;
                y = dist / 2;
            }
            else if (i == 4)
            {
                y = dist;
            }
            else if (i == 5)
            {
                x = -buttonwidth / 2 - 8;
                y = dist / 2;
            }
            else if (i == 6)
            {
                x = -buttonwidth / 2 - 32;
            }
            else if (i == 7)
            {
                x = -buttonwidth / 2 - 8;
                y = -dist / 2;
            }

            final int idx = i;

            if (!"?".equals(str))
            {
                this.addRenderableWidget(new Builder(Component.translatable(str), (p) ->
                    {
                        if (idx < 200)
                        {
                            VRInputAction vb = dh.vr.getInputAction(this.arr[idx]);

                            if (vb != null)
                            {
                                vb.pressBinding();
                                vb.unpressBinding(2);
                            }
                        }
                        else if (idx == 201)
                        {
                            this.setShift(!this.isShift);
                        }
                    })
                    .size(buttonwidth, 20)
                    .pos(centerx + x - buttonwidth / 2, centery + y - 10)
                    .build()
                );
            }
        }
    }

    public void setShift(boolean shift)
    {
        if (shift != this.isShift)
        {
            this.isShift = shift;
            this.init();
        }
    }

    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, 0, 0, pPartialTicks);
    }
}
