package org.vivecraft.client.gui.framework.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.vivecraft.client_vr.render.helpers.GuiHelper;

/**
 * Button that just renders the text
 */
public class TextOnlyButton extends Button {

    public TextOnlyButton(Component message, OnPress onPress) {
        super(0, 0, 120, 20, message, onPress);
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        int textWidth = Minecraft.getInstance().font.width(this.getMessage());
        int textY = this.y + this.getHeight() / 2 - Minecraft.getInstance().font.lineHeight / 2 + 2;

        int color = !this.active ? 0xFFA0A0A0 : (this.isHoveredOrFocused() ? 0xFF55FF55 : 0xFFFFFFFF);

        if (textWidth < this.getWidth()) {
            drawString(poseStack, Minecraft.getInstance().font, this.getMessage(), this.x, textY, color);
        } else {
            GuiHelper.renderScrollingString(poseStack, Minecraft.getInstance().font, this.getMessage(),
                this.x, textY, this.x + this.getWidth() - 5,
                textY + Minecraft.getInstance().font.lineHeight - 1, color);
        }
    }
}
