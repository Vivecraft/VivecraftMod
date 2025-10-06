package org.vivecraft.client.gui.framework.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Button that just renders the text
 */
public class TextOnlyButton extends Button {

    public TextOnlyButton(Component message, OnPress onPress) {
        super(0, 0, 120, 20, message, onPress, Button.DEFAULT_NARRATION);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int textWidth = Minecraft.getInstance().font.width(this.getMessage());
        int textY = this.getY() + this.getHeight() / 2 - Minecraft.getInstance().font.lineHeight / 2 + 2;

        int color = !this.active ? 0xFFA0A0A0 : (this.isHoveredOrFocused() ? 0xFF55FF55 : 0xFFFFFFFF);

        if (textWidth < this.getWidth()) {
            guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), this.getX(), textY, color);
        } else {
            AbstractWidget.renderScrollingString(guiGraphics, Minecraft.getInstance().font, this.getMessage(),
                this.getX(), textY, this.getX() + this.getWidth() - 5,
                textY + Minecraft.getInstance().font.lineHeight - 1, color);
        }
    }
}
