package org.vivecraft.client.gui.framework.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import org.vivecraft.client_vr.render.helpers.GuiHelper;
import org.vivecraft.client_vr.utils.RGBAColor;

public class ColorPicker extends AbstractWidget {

    private static final int HUE_WIDTH = 10;

    private float hue;
    private float saturation = 0F;
    private float brightness = 1F;

    private boolean clickedHue;

    public ColorPicker(int x, int y, int width, int height) {
        super(x, y, width, height, new TextComponent("Color Picker"));
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        // black background
        GuiComponent.fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height,
            0xFF000000);

        for (int i = 0; i < this.height - 2; i++) {
            RGBAColor color = RGBAColor.fromHSB(i / (float) (this.height - 2), 1F, 1F);
            GuiComponent.fill(poseStack, this.x + 1, this.y + i + 1, this.x + HUE_WIDTH - 1,
                this.y + i + 2, color.toIntEncodingARGB());
        }
        for (int x = HUE_WIDTH; x < this.width - 2; x++) {
            for (int y = 0; y < this.height - 2; y++) {
                RGBAColor color = RGBAColor.fromHSB(this.hue, (x - HUE_WIDTH) / (float) (this.width - HUE_WIDTH - 2),
                    1F - y / (float) (this.height - 3));
                int xPos = this.x + x + 1;
                int yPos = this.y + y + 1;

                GuiComponent.fill(poseStack, xPos, yPos, xPos + 1, yPos + 1, color.toIntEncodingARGB());
            }
        }

        int satX = (int) (this.x + HUE_WIDTH + 1 + this.saturation * (this.width - HUE_WIDTH - 3));
        int satY = (int) (this.y + 1 + (1F - this.brightness) * (this.height - 3));
        GuiHelper.renderOutline(poseStack, satX - 2, satY - 2, 5, 5, 0xFFFFFFFF);

        int hueY = (int) (this.y + 1 + this.hue * (this.height - 3));
        GuiHelper.renderOutline(poseStack, this.x, hueY - 2, HUE_WIDTH, 5, 0xFFFFFFFF);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.clickedHue = mouseX < this.x + HUE_WIDTH;
        this.setColor(mouseX, mouseY);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        this.setColor(mouseX, mouseY);
    }

    private void setColor(double mouseX, double mouseY) {
        if (this.clickedHue) {
            this.hue = (float) Mth.clamp((mouseY - (this.y + 1)) / (this.height - 2), 0.0, 1.0);
        } else {
            this.brightness = 1F - (float) Mth.clamp((mouseY - this.y - 1) / (this.height - 2), 0.0, 1.0);
            this.saturation = (float) Mth.clamp(
                (mouseX - this.x - HUE_WIDTH - 1) / (this.getWidth() - HUE_WIDTH - 2),
                0.0, 1.0);
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
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
