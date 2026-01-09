package org.vivecraft.client.gui.framework.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.vivecraft.client_vr.utils.RGBAColor;

/**
 * Button that has a color tint
 */
public class ColoredButton extends Button {

    private final RGBAColor color = new RGBAColor();

    public ColoredButton(Component message, int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
    }

    public RGBAColor getColor() {
        return this.color;
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(this.color.r, this.color.g, this.color.b, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        blitNineSliced(poseStack, this.getX(), this.getY(), this.getWidth(), this.getHeight(),
            TEXTURE_BORDER_X, TEXTURE_BORDER_Y, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, this.getTextureY());

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int color = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.renderString(guiGraphics, Minecraft.getInstance().font, color | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    // copied from AbstractButton
    private int getTextureY() {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (this.isHoveredOrFocused()) {
            i = 2;
        }

        return TEXTURE_Y_OFFSET + i * TEXTURE_HEIGHT;
    }
}
