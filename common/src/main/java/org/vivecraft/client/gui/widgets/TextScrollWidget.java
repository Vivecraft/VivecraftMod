package org.vivecraft.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class TextScrollWidget extends AbstractWidget {

    private int maxLines = 0;
    private int currentLine = 0;
    private int scrollBarSize = 0;
    private int scrollBarOffset = 0;
    private int scrollSteps = 0;

    private boolean scrollDragActive;

    private final int scrollBarWidth = 5;
    private final int padding = 5;
    private final List<FormattedText> formattedText;

    public TextScrollWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height, Component.literal(""));

        formattedText = Minecraft.getInstance().font.getSplitter().splitLines(text, width - scrollBarWidth*2, Style.EMPTY);

        initScroll();
    }

    public TextScrollWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, Component.literal(""));

        formattedText = Minecraft.getInstance().font.getSplitter().splitLines(text, width - scrollBarWidth*2, Style.EMPTY);
        initScroll();
    }

    private void initScroll() {

        maxLines = (height - 2 - padding + 3) / 12;
        currentLine = 0;
        scrollSteps = formattedText.size() - maxLines;
        scrollSteps = Math.max(scrollSteps, 0);
        scrollBarSize = scrollSteps == 0 ? height - 2 : (int)(Math.max(formattedText.size(), maxLines) / (float)(scrollSteps) * 12);
        scrollBarOffset = height - scrollBarSize - 2;

    }

    @Override
    public void renderWidget(PoseStack poseStack, int i, int j, float f) {
        // draw box outline
        fill(poseStack,
                getX(),
                getY(),
                getX() + width,
                getY() + this.height,
                -6250336);
        // draw box inside
        fill(poseStack,
                getX() + 1,
                getY() + 1,
                getX() + width - 1,
                getY() + this.height - 1,
                -16777216);

        // draw text
        for (int line = 0; line + currentLine < formattedText.size() && line < maxLines; line++) {
            drawString(poseStack, Minecraft.getInstance().font, formattedText.get(line + currentLine).getString(), getX() + padding, getY() + padding + line * 12, 16777215);
        }

        float scrollbarStart = scrollSteps == 0 ? 0 : currentLine/(float)scrollSteps * scrollBarOffset;

        if (isFocused() || isHovered) {
            // draw scroll bar outline
            fill(poseStack,
                    getX() + width - scrollBarWidth - 2,
                    (int) (getY() + 1 + scrollbarStart),
                    getX() + width - 1,
                    (int) (getY() + 1 + scrollbarStart + scrollBarSize),
                    -1);
        }

        // draw scroll bar
        fill(poseStack,
                getX() + width - scrollBarWidth - (isFocused() || isHovered ? 1 : 2),
                (int)(getY() + (isFocused() || isHovered ? 2 : 1) + scrollbarStart),
                getX() + width - (isFocused() || isHovered ? 2 : 1),
                (int)(getY() + (isFocused() || isHovered ? 0 : 1) + scrollbarStart + scrollBarSize),
                -6250336);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double x, double y) {
        if (x >= getX() + width - scrollBarWidth && x <= getX() + width && y >= getY() && y <= getY() + height) {
            scrollDragActive = true;
            if (maxLines < formattedText.size()) {
                // update scroll position
                setCurrentLineFromYPos(y);
            }
        }
    }

    @Override
    public void onRelease(double x, double y) {
        scrollDragActive = false;
        super.onRelease(x, y);
    }

    @Override
    public void onDrag(double x, double y, double xRel, double yRel) {
        if (visible && active && scrollDragActive) {
            setCurrentLineFromYPos(y);
        }
    }

    private void setCurrentLineFromYPos(double y) {
        if (y < getY() + scrollBarSize * 0.5) {
            currentLine = 0;
        } else if (y > getY() + height - scrollBarSize * 0.5) {
            currentLine = scrollSteps;
        } else {
            currentLine = (int)((y - getY() - scrollBarSize * 0.5) / (height - scrollBarSize) * scrollSteps);
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmount) {
        if (scrollAmount < 0.0 && currentLine < scrollSteps) {
            currentLine++;
        } else if (scrollAmount > 0.0 && currentLine > 0) {
            currentLine--;
        } else {
            // scroll bar on limit, didn't consume the input
            return false;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN) {
            if (mouseScrolled(0, 0, key == GLFW.GLFW_KEY_UP ? 1 : -1)) {
                return true;
            }
        }
        return super.keyPressed(key, scancode, mods);
    }
}
