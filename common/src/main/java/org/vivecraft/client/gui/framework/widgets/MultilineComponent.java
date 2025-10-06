package org.vivecraft.client.gui.framework.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Widget that just shows text, splits lines into on new lines and when a line is longer than width
 */
public class MultilineComponent extends AbstractWidget {

    private final Font font;
    private final boolean centered;
    private final List<FormattedCharSequence> text;

    private boolean renderBackground = false;

    /**
     * @param x        x position, when {@code centered} is active, this is the center, else it's the left edge
     * @param y        y position, top edge of the text
     * @param width    width the text should be wrapped to
     * @param message  Text to show
     * @param centered centered text orr left aligned
     * @param font     Font to use
     */
    public MultilineComponent(int x, int y, int width, Component message, boolean centered, Font font) {
        super(centered ? x - width / 2 : x, y, width, 0, message);

        this.text = Minecraft.getInstance().font.split(message, width);
        this.height = this.text.size() * font.lineHeight;
        this.centered = centered;
        this.font = font;
    }

    public MultilineComponent withBackground() {
        this.renderBackground = true;
        return this;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

        if (this.renderBackground) {
            GuiComponent.fill(poseStack,
                this.x - 5, this.y - 5,
                this.x + this.getWidth() + 5, this.y + this.getHeight() + 5,
                0x80000000);
        }
        int yPos = 0;
        for (FormattedCharSequence text : this.text) {
            if (this.centered) {
                drawCenteredString(poseStack, this.font, text, this.x + this.width / 2, this.y + yPos, 0xFFFFFFFF);
            } else {
                drawString(poseStack, this.font, text, this.x, this.y + yPos, 0xFFFFFFFF);
            }
            yPos += this.font.lineHeight;
        }
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }
}
