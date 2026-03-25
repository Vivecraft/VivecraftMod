package org.vivecraft.client.gui.framework.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.vivecraft.client_vr.utils.RGBAColor;

public class ColorPicker extends AbstractWidget {

    private static final int HUE_WIDTH = 10;

    private float hue;
    private float saturation = 0F;
    private float brightness = 1F;

    private boolean clickedHue;

    public ColorPicker(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("Color Picker"));
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // black background
        graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height,
            0xFF000000);

        for (int i = 0; i < this.height - 2; i++) {
            RGBAColor color = RGBAColor.fromHSB(i / (float) (this.height - 2), 1F, 1F);
            graphics.fill(this.getX() + 1, this.getY() + i + 1, this.getX() + HUE_WIDTH - 1,
                this.getY() + i + 2, ARGB.colorFromFloat(1F, color.r, color.g, color.b));
        }
        for (int x = HUE_WIDTH; x < this.width - 2; x++) {
            for (int y = 0; y < this.height - 2; y++) {
                RGBAColor color = RGBAColor.fromHSB(this.hue, (x - HUE_WIDTH) / (float) (this.width - HUE_WIDTH - 2),
                    1F - y / (float) (this.height - 3));
                int xPos = this.getX() + x + 1;
                int yPos = this.getY() + y + 1;

                graphics.fill(xPos, yPos, xPos + 1, yPos + 1, ARGB.colorFromFloat(1F, color.r, color.g, color.b));
            }
        }

        int satX = (int) (this.getX() + HUE_WIDTH + 1 + this.saturation * (this.width - HUE_WIDTH - 3));
        int satY = (int) (this.getY() + 1 + (1F - this.brightness) * (this.height - 3));
        graphics.outline(satX - 2, satY - 2, 5, 5, 0xFFFFFFFF);

        int hueY = (int) (this.getY() + 1 + this.hue * (this.height - 3));
        graphics.outline(this.getX(), hueY - 2, HUE_WIDTH, 5, 0xFFFFFFFF);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean isDoubleClick) {
        this.clickedHue = event.x() < this.getX() + HUE_WIDTH;
        this.setColor(event.x(), event.y());
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double mouseX, double mouseY) {
        this.setColor(event.x(), event.y());
    }

    private void setColor(double mouseX, double mouseY) {
        if (this.clickedHue) {
            this.hue = (float) Math.clamp((mouseY - (this.getY() + 1)) / (this.height - 2), 0.0, 1.0);
        } else {
            this.brightness = 1F - (float) Math.clamp((mouseY - this.getY() - 1) / (this.height - 2), 0.0, 1.0);
            this.saturation = (float) Math.clamp(
                (mouseX - this.getX() - HUE_WIDTH - 1) / (this.getWidth() - HUE_WIDTH - 2),
                0.0, 1.0);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    public RGBAColor getColor() {
        return RGBAColor.fromHSB(this.hue, this.saturation, this.brightness);
    }

    public void setColor(RGBAColor color) {
        float max = Math.max(color.r, Math.max(color.g, color.b));
        float min = Math.min(color.r, Math.min(color.g, color.b));
        float dif = max - min;

        this.brightness = max;
        this.saturation = max == 0 ? 0 : dif / max;
        if (dif == 0.0F) {
            this.hue = 0;
            return;
        }
        if (max == color.r) {
            this.hue = (color.g - color.b) / dif;
        } else if (max == color.g) {
            this.hue = 2 + (color.b - color.r) / dif;
        } else {
            this.hue = 4 + (color.r - color.g) / dif;
        }
        this.hue *= 60F;
        this.hue = ((this.hue + 360F) % 360F) / 360F;
    }
}
