package org.vivecraft.client.gui.framework;

import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_vr.provider.ControllerType;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import static org.joml.Math.*;

public abstract class TwoHandedScreen extends Screen
{
    public float cursorX1;
    public float cursorY1;
    public float cursorX2;
    public float cursorY2;
    private AbstractWidget lastHoveredButtonId1 = null;
    private AbstractWidget lastHoveredButtonId2 = null;
    protected boolean reinit;

    protected TwoHandedScreen()
    {
        super(Component.literal(""));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
    {
        if (super.mouseClicked(pMouseX, p_94738_, pMouseY))
        {
            double d0 = (double) min(max((int) this.cursorX2, 0), mc.getWindow().getScreenWidth()) *
                (double) mc.getWindow().getGuiScaledWidth() / (double) mc.getWindow().getScreenWidth();
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks)
    {
        if (this.reinit)
        {
            this.init();
            this.reinit = false;
        }

        int i0 = ((int) this.cursorX1 * this.width / mc.getWindow().getGuiScaledWidth())
            * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        int i1 = ((int) this.cursorY1 * this.height / mc.getWindow().getGuiScaledHeight())
            * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        int i2 = ((int) this.cursorX2 * this.width / mc.getWindow().getGuiScaledWidth())
            * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        int i3 = ((int) this.cursorY2 * this.height / mc.getWindow().getGuiScaledHeight())
            * mc.getWindow().getGuiScaledWidth() / mc.getWindow().getScreenWidth();
        AbstractWidget abstractwidget = null;
        AbstractWidget abstractwidget1 = null;

        for (GuiEventListener child : this.children())
        {
            AbstractWidget abstractwidget2 = (AbstractWidget) child;
            boolean flag = i0 >= abstractwidget2.getX() && i1 >= abstractwidget2.getY() &&
                i0 < (abstractwidget2.getX() + abstractwidget2.getWidth()) && i1 < (abstractwidget2.getY() + 20);
            boolean flag1 = i2 >= abstractwidget2.getX() && i3 >= abstractwidget2.getY() &&
                i2 < (abstractwidget2.getX() + abstractwidget2.getWidth()) && i3 < (abstractwidget2.getY() + 20);

            if (flag)
            {
                abstractwidget2.render(guiGraphics, i0, i1, pPartialTicks);
            }
            else
            {
                abstractwidget2.render(guiGraphics, i2, i3, pPartialTicks);
            }

            if (flag)
            {
                abstractwidget = abstractwidget2;
            }

            if (flag1)
            {
                abstractwidget1 = abstractwidget2;
            }
        }

        if (abstractwidget == null)
        {
            this.lastHoveredButtonId1 = null;
        }
        else if (abstractwidget instanceof Button && this.lastHoveredButtonId1 != abstractwidget)
        {
            dh.vr.triggerHapticPulse(ControllerType.LEFT, 300);
            this.lastHoveredButtonId1 = abstractwidget;
        }

        if (abstractwidget1 == null)
        {
            this.lastHoveredButtonId2 = null;
        }
        else if (abstractwidget1 instanceof Button && this.lastHoveredButtonId2 != abstractwidget1)
        {
            dh.vr.triggerHapticPulse(ControllerType.RIGHT, 300);
            this.lastHoveredButtonId2 = abstractwidget1;
        }

        ((GuiExtension) mc.gui).drawMouseMenuQuad(i0, i1);
        ((GuiExtension) mc.gui).drawMouseMenuQuad(i2, i3);
    }
}
