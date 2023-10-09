package org.vivecraft.client.gui.framework;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;

public abstract class TwoHandedScreen extends Screen {
    public float cursorX1;
    public float cursorY1;
    public float cursorX2;
    public float cursorY2;
    private AbstractWidget lastHoveredButtonId1 = null;
    private AbstractWidget lastHoveredButtonId2 = null;
    protected boolean reinit;

    protected TwoHandedScreen() {
        super(Component.literal(""));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY) {
        return super.mouseClicked(pMouseX, p_94738_, pMouseY);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        if (this.reinit) {
            this.init();
            this.reinit = false;
        }

        int i0 = (int) (this.cursorX1 * this.width / GuiHandler.guiWidth);
        int i1 = (int) (this.cursorY1 * this.height / GuiHandler.guiHeight);
        int i2 = (int) (this.cursorX2 * this.width / GuiHandler.guiWidth);
        int i3 = (int) (this.cursorY2 * this.height / GuiHandler.guiHeight);
        AbstractWidget abstractwidget = null;
        AbstractWidget abstractwidget1 = null;

        for (GuiEventListener child : this.children()) {
            AbstractWidget abstractwidget2 = (AbstractWidget) child;
            boolean flag = i0 >= abstractwidget2.getX() && i1 >= abstractwidget2.getY() &&
                i0 < (abstractwidget2.getX() + abstractwidget2.getWidth()) && i1 < (abstractwidget2.getY() + 20);
            boolean flag1 = i2 >= abstractwidget2.getX() && i3 >= abstractwidget2.getY() &&
                i2 < (abstractwidget2.getX() + abstractwidget2.getWidth()) && i3 < (abstractwidget2.getY() + 20);

            if (flag) {
                abstractwidget2.render(guiGraphics, i0, i1, pPartialTicks);
            } else {
                abstractwidget2.render(guiGraphics, i2, i3, pPartialTicks);
            }

            if (flag) {
                abstractwidget = abstractwidget2;
            }

            if (flag1) {
                abstractwidget1 = abstractwidget2;
            }
        }

        if (abstractwidget == null) {
            this.lastHoveredButtonId1 = null;
        } else if (abstractwidget instanceof Button && this.lastHoveredButtonId1 != abstractwidget) {
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(ControllerType.LEFT, 300);
            this.lastHoveredButtonId1 = abstractwidget;
        }

        if (abstractwidget1 == null) {
            this.lastHoveredButtonId2 = null;
        } else if (abstractwidget1 instanceof Button && this.lastHoveredButtonId2 != abstractwidget1) {
            ClientDataHolderVR.getInstance().vr.triggerHapticPulse(ControllerType.RIGHT, 300);
            this.lastHoveredButtonId2 = abstractwidget1;
        }

        ((GuiExtension) Minecraft.getInstance().gui).vivecraft$drawMouseMenuQuad(i0, i1);
        ((GuiExtension) Minecraft.getInstance().gui).vivecraft$drawMouseMenuQuad(i2, i3);
    }
}
