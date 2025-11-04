package org.vivecraft.client.gui.framework.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.vivecraft.client_vr.utils.RGBAColor;

/**
 * Button that has a color tint
 */
public class ColoredButton extends Button {

    // copied over from AbstractButton
    private static final WidgetSprites SPRITES = new WidgetSprites(
        ResourceLocation.withDefaultNamespace("widget/button"),
        ResourceLocation.withDefaultNamespace("widget/button_disabled"),
        ResourceLocation.withDefaultNamespace("widget/button_highlighted"));

    private final RGBAColor color = new RGBAColor();

    public ColoredButton(Component message, int x, int y, int width, int height, OnPress onPress) {
        super(x, y, width, height, message, onPress, Button.DEFAULT_NARRATION);
    }

    public RGBAColor getColor() {
        return this.color;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.setColor(this.color.r, this.color.g, this.color.b, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        guiGraphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(),
            this.getWidth(), this.getHeight());
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.active ? 0xFFFFFF : 0xA0A0A0;
        this.renderString(guiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
