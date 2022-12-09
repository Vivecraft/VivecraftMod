package org.vivecraft.gui.framework;

import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.GuiExtension;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.provider.MCVR;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class TwoHandedScreen extends Screen
{
	protected ClientDataHolder dataholder = ClientDataHolder.getInstance();
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

    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
    {
        if (super.mouseClicked(pMouseX, p_94738_, pMouseY))
        {
            double d0 = (double)Math.min(Math.max((int)this.cursorX2, 0), this.minecraft.getWindow().getScreenWidth()) * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        if (this.reinit)
        {
            this.init();
            this.reinit = false;
        }

        double d0 = (double)(this.cursorX1 * (float)this.width / (float)this.minecraft.getWindow().getGuiScaledWidth()) * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double d1 = (double)(this.cursorY1 * (float)this.height / (float)this.minecraft.getWindow().getGuiScaledHeight()) * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double d2 = (double)(this.cursorX2 * (float)this.width / (float)this.minecraft.getWindow().getGuiScaledWidth()) * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double d3 = (double)(this.cursorY2 * (float)this.height / (float)this.minecraft.getWindow().getGuiScaledHeight()) * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        AbstractWidget abstractwidget = null;
        AbstractWidget abstractwidget1 = null;

        for (int i = 0; i < this.renderables.size(); ++i)
        {
            AbstractWidget abstractwidget2 = (AbstractWidget) this.renderables.get(i);
            boolean flag = d0 >= (double)abstractwidget2.getX() && d1 >= (double)abstractwidget2.getY() && d0 < (double)(abstractwidget2.getX() + abstractwidget2.getWidth()) && d1 < (double)(abstractwidget2.getY() + 20);
            boolean flag1 = d2 >= (double)abstractwidget2.getX() && d3 >= (double)abstractwidget2.getY() && d2 < (double)(abstractwidget2.getX() + abstractwidget2.getWidth()) && d3 < (double)(abstractwidget2.getY() + 20);

            if (flag)
            {
                abstractwidget2.render(pMatrixStack, (int)d0, (int)d1, pPartialTicks);
            }
            else
            {
                abstractwidget2.render(pMatrixStack, (int)d2, (int)d3, pPartialTicks);
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
            MCVR.get().triggerHapticPulse(ControllerType.LEFT, 300);
            this.lastHoveredButtonId1 = abstractwidget;
        }

        if (abstractwidget1 == null)
        {
            this.lastHoveredButtonId2 = null;
        }
        else if (abstractwidget1 instanceof Button && this.lastHoveredButtonId2 != abstractwidget1)
        {
            MCVR.get().triggerHapticPulse(ControllerType.RIGHT, 300);
            this.lastHoveredButtonId2 = abstractwidget1;
        }

        ((GuiExtension) this.minecraft.gui).drawMouseMenuQuad((int)d0, (int)d1);
        ((GuiExtension) this.minecraft.gui).drawMouseMenuQuad((int)d2, (int)d3);
    }
}
