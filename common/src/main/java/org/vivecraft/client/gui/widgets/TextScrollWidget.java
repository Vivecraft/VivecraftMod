package org.vivecraft.client.gui.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
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
    private final List<FormattedCharSequence> formattedChars;

    public TextScrollWidget(int x, int y, int width, int height, String text) {
        super(x, y, width, height, Component.literal(""));

        formattedChars = Minecraft.getInstance().font.split(Component.literal(text), width - scrollBarWidth * 2);

        initScroll();
    }

    public TextScrollWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, Component.literal(""));

        formattedChars = Minecraft.getInstance().font.split(text, width - scrollBarWidth * 2);
        initScroll();
    }

    private void initScroll() {

        maxLines = (height - 2 - padding + 3) / 12;
        currentLine = 0;
        scrollSteps = formattedChars.size() - maxLines;
        scrollSteps = Math.max(scrollSteps, 0);
        scrollBarSize = scrollSteps == 0 ? height - 2 : (int) (Math.max(formattedChars.size(), maxLines) / (float) (scrollSteps) * 12);
        scrollBarOffset = height - scrollBarSize - 2;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int x, int y, float f) {
        // draw box outline
        guiGraphics.fill(
            getX(),
            getY(),
            getX() + width,
            getY() + this.height,
            -6250336);
        // draw box inside
        guiGraphics.fill(
            getX() + 1,
            getY() + 1,
            getX() + width - 1,
            getY() + this.height - 1,
            -16777216);

        // draw text
        for (int line = 0; line + currentLine < formattedChars.size() && line < maxLines; line++) {
            guiGraphics.drawString(Minecraft.getInstance().font, formattedChars.get(line + currentLine), getX() + padding, getY() + padding + line * 12, 16777215);
        }

        float scrollbarStart = scrollSteps == 0 ? 0 : currentLine / (float) scrollSteps * scrollBarOffset;

        if (isFocused() || isHovered) {
            // draw scroll bar outline
            guiGraphics.fill(
                getX() + width - scrollBarWidth - 2,
                (int) (getY() + 1 + scrollbarStart),
                getX() + width - 1,
                (int) (getY() + 1 + scrollbarStart + scrollBarSize),
                -1);
        }

        // draw scroll bar
        guiGraphics.fill(
            getX() + width - scrollBarWidth - (isFocused() || isHovered ? 1 : 2),
            (int) (getY() + (isFocused() || isHovered ? 2 : 1) + scrollbarStart),
            getX() + width - (isFocused() || isHovered ? 2 : 1),
            (int) (getY() + (isFocused() || isHovered ? 0 : 1) + scrollbarStart + scrollBarSize),
            -6250336);

        renderMouseover(guiGraphics, x, y);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double x, double y) {
        if (x >= getX() + width - scrollBarWidth && x <= getX() + width && y >= getY() && y <= getY() + height) {
            scrollDragActive = true;
            if (maxLines < formattedChars.size()) {
                // update scroll position
                setCurrentLineFromYPos(y);
            }
        } else if (this.clicked(x, y)) {
            Style style = getMouseoverStyle(x, y);
            if (style != null && style.getClickEvent() != null) {
                Minecraft.getInstance().screen.handleComponentClicked(style);
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
            currentLine = (int) ((y - getY() - scrollBarSize * 0.5) / (height - scrollBarSize) * scrollSteps);
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollAmountX, double scrollAmountY) {
        if (scrollAmountY < 0.0 && currentLine < scrollSteps) {
            currentLine++;
        } else if (scrollAmountY > 0.0 && currentLine > 0) {
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
            if (mouseScrolled(0, 0, 0, key == GLFW.GLFW_KEY_UP ? 1 : -1)) {
                return true;
            }
        }
        return super.keyPressed(key, scancode, mods);
    }

    public Style getMouseoverStyle(double x, double y) {
        int lineIndex = this.getLineIndex(x, y);
        if (lineIndex >= 0 && lineIndex < this.formattedChars.size()) {
            FormattedCharSequence line = this.formattedChars.get(lineIndex);
            return Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(line, Mth.floor(x - this.getX()));
        }
        return null;
    }

    private int getLineIndex(double x, double y) {
        if (!this.clicked(x, y)) {
            return -1;
        } else {
            return (int) ((y - this.getY() - padding * 0.5) / 12.0);
        }
    }

    public void renderMouseover(GuiGraphics guiGraphics, int x, int y) {
        Style style = this.getMouseoverStyle(x, y);
        if (style != null && style.getHoverEvent() != null) {
            guiGraphics.renderComponentHoverEffect(Minecraft.getInstance().font, style, x, y);
        }
    }
}
