package org.vivecraft.client.gui.framework.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.vivecraft.client_vr.utils.RGBAColor;

/**
 * Button that has a color tint
 */
public class ColoredButton extends Button {

    private final RGBAColor color = new RGBAColor();

    public ColoredButton(Component message, int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, message, onPress);
    }

    public RGBAColor getColor() {
        return this.color;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(this.color.r, this.color.g, this.color.b, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(poseStack, this.x, this.y, 0, getTextureY(), this.width / 2, this.height);
        this.blit(poseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, getTextureY(), this.width / 2,
            this.height);
        this.renderBg(poseStack, minecraft, mouseX, mouseY);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? 0xFFFFFF : 0xA0A0A0;
        drawCenteredString(poseStack, minecraft.font, this.getMessage(), this.x + this.width / 2,
            this.y + (this.height - 8) / 2, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    // copied from AbstractButton
    private int getTextureY() {
        return 46 + this.getYImage(this.isHoveredOrFocused()) * 20;
    }
}
