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

        this.formattedChars = Minecraft.getInstance().font.split(Component.literal(text), width - this.scrollBarWidth * 2);

        initScroll();
    }

    public TextScrollWidget(int x, int y, int width, int height, Component text) {
        super(x, y, width, height, Component.literal(""));

        this.formattedChars = Minecraft.getInstance().font.split(text, width - this.scrollBarWidth * 2);
        initScroll();
    }

    private void initScroll() {

        this.maxLines = (this.height - 2 - this.padding + 3) / 12;
        this.currentLine = 0;
        this.scrollSteps = this.formattedChars.size() - this.maxLines;
        this.scrollSteps = Math.max(this.scrollSteps, 0);
        this.scrollBarSize = this.scrollSteps == 0 ? this.height - 2 : (int) (Math.max(this.formattedChars.size(), this.maxLines) / (float) (this.scrollSteps) * 12);
        this.scrollBarOffset = this.height - this.scrollBarSize - 2;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // draw box outline
        guiGraphics.fill(
            getX(),
            getY(),
            getX() + this.width,
            getY() + this.height,
            0xFFA0A0A0);
        // draw box inside
        guiGraphics.fill(
            getX() + 1,
            getY() + 1,
            getX() + this.width - 1,
            getY() + this.height - 1,
            0xFF000000);

        // draw text
        for (int line = 0; line + this.currentLine < this.formattedChars.size() && line < this.maxLines; line++) {
            guiGraphics.drawString(Minecraft.getInstance().font, this.formattedChars.get(line + this.currentLine), getX() + this.padding, getY() + this.padding + line * 12, 0xFFFFFF);
        }

        float scrollbarStart = this.scrollSteps == 0 ? 0 : this.currentLine / (float) this.scrollSteps * this.scrollBarOffset;

        if (isFocused() || this.isHovered) {
            // draw scroll bar outline
            guiGraphics.fill(
                getX() + this.width - this.scrollBarWidth - 2,
                (int) (getY() + 1 + scrollbarStart),
                getX() + this.width - 1,
                (int) (getY() + 1 + scrollbarStart + this.scrollBarSize),
                -1);
        }

        // draw scroll bar
        guiGraphics.fill(
            getX() + this.width - this.scrollBarWidth - (isFocused() || this.isHovered ? 1 : 2),
            (int) (getY() + (isFocused() || this.isHovered ? 2 : 1) + scrollbarStart),
            getX() + this.width - (isFocused() || this.isHovered ? 2 : 1),
            (int) (getY() + (isFocused() || this.isHovered ? 0 : 1) + scrollbarStart + this.scrollBarSize),
            0xFFA0A0A0);

        renderMouseover(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (mouseX >= getX() + this.width - this.scrollBarWidth && mouseX <= getX() + this.width &&
            mouseY >= getY() && mouseY <= getY() + this.height)
        {
            this.scrollDragActive = true;
            if (this.maxLines < this.formattedChars.size()) {
                // update scroll position
                setCurrentLineFromYPos(mouseY);
            }
        } else if (this.clicked(mouseX, mouseY)) {
            Style style = getMouseoverStyle(mouseX, mouseY);
            if (style != null && style.getClickEvent() != null) {
                Minecraft.getInstance().screen.handleComponentClicked(style);
            }
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.scrollDragActive = false;
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (this.visible && this.active && this.scrollDragActive) {
            setCurrentLineFromYPos(mouseY);
        }
    }

    private void setCurrentLineFromYPos(double mouseY) {
        if (mouseY < getY() + this.scrollBarSize * 0.5) {
            this.currentLine = 0;
        } else if (mouseY > getY() + this.height - this.scrollBarSize * 0.5) {
            this.currentLine = this.scrollSteps;
        } else {
            this.currentLine = (int) ((mouseY - getY() - this.scrollBarSize * 0.5) / (this.height - this.scrollBarSize) * this.scrollSteps);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY < 0.0 && this.currentLine < this.scrollSteps) {
            this.currentLine++;
        } else if (scrollY > 0.0 && this.currentLine > 0) {
            this.currentLine--;
        } else {
            // scroll bar on limit, didn't consume the input
            return false;
        }
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_UP || keyCode == GLFW.GLFW_KEY_DOWN) {
            if (mouseScrolled(0, 0, 0, keyCode == GLFW.GLFW_KEY_UP ? 1 : -1)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public Style getMouseoverStyle(double mouseX, double mouseY) {
        int lineIndex = this.getLineIndex(mouseX, mouseY);
        if (lineIndex >= 0 && lineIndex < this.formattedChars.size()) {
            FormattedCharSequence line = this.formattedChars.get(lineIndex);
            return Minecraft.getInstance().font.getSplitter().componentStyleAtWidth(line, Mth.floor(mouseX - this.getX()));
        }
        return null;
    }

    private int getLineIndex(double mouseX, double mouseY) {
        if (!this.clicked(mouseX, mouseY)) {
            return -1;
        } else {
            return (int) ((mouseY - this.getY() - this.padding * 0.5) / 12.0);
        }
    }

    public void renderMouseover(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Style style = this.getMouseoverStyle(mouseX, mouseY);
        if (style != null && style.getHoverEvent() != null) {
            guiGraphics.renderComponentHoverEffect(Minecraft.getInstance().font, style, mouseX, mouseY);
        }
    }
}
