package org.vivecraft.client.gui.framework.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
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

        this.formattedChars = Minecraft.getInstance().font.split(Component.literal(text),
            width - this.scrollBarWidth * 2);

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
        this.scrollBarSize = this.scrollSteps == 0 ? this.height - 2 :
            (int) (Math.max(this.formattedChars.size(), this.maxLines) / (float) (this.scrollSteps) * 12);
        this.scrollBarOffset = this.height - this.scrollBarSize - 2;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // draw box outline
        graphics.fill(
            getX(),
            getY(),
            getX() + this.width,
            getY() + this.height,
            0xFFA0A0A0);
        // draw box inside
        graphics.fill(
            getX() + 1,
            getY() + 1,
            getX() + this.width - 1,
            getY() + this.height - 1,
            0xFF000000);

        ActiveTextCollector textRenderer = graphics.textRenderer(
            GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);

        // draw text
        for (int line = 0; line + this.currentLine < this.formattedChars.size() && line < this.maxLines; line++) {
            textRenderer.accept(getX() + this.padding, getY() + this.padding + line * 12,
                this.formattedChars.get(line + this.currentLine));
        }

        float scrollbarStart =
            this.scrollSteps == 0 ? 0 : this.currentLine / (float) this.scrollSteps * this.scrollBarOffset;

        if (isFocused() || this.isHovered) {
            // draw scroll bar outline
            graphics.fill(
                getX() + this.width - this.scrollBarWidth - 2,
                (int) (getY() + 1 + scrollbarStart),
                getX() + this.width - 1,
                (int) (getY() + 1 + scrollbarStart + this.scrollBarSize),
                -1);
        }

        // draw scroll bar
        graphics.fill(
            getX() + this.width - this.scrollBarWidth - (isFocused() || this.isHovered ? 1 : 2),
            (int) (getY() + (isFocused() || this.isHovered ? 2 : 1) + scrollbarStart),
            getX() + this.width - (isFocused() || this.isHovered ? 2 : 1),
            (int) (getY() + (isFocused() || this.isHovered ? 0 : 1) + scrollbarStart + this.scrollBarSize),
            0xFFA0A0A0);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

    @Override
    public void onClick(MouseButtonEvent mouseEvent, boolean doubleClick) {
        if (mouseEvent.x() >= getX() + this.width - this.scrollBarWidth && mouseEvent.x() <= getX() + this.width &&
            mouseEvent.y() >= getY() && mouseEvent.y() <= getY() + this.height)
        {
            this.scrollDragActive = true;
            if (this.maxLines < this.formattedChars.size()) {
                // update scroll position
                setCurrentLineFromYPos(mouseEvent.y());
            }
        } else if (this.isMouseOver(mouseEvent.x(), mouseEvent.y())) {
            Style style = getMouseoverStyle(mouseEvent.x(), mouseEvent.y());
            if (style != null && style.getClickEvent() != null) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null) {
                    Screen.defaultHandleGameClickEvent(style.getClickEvent(), mc, mc.screen);
                } else {
                    // skip the ingame click events and directly call the general ones
                    Screen.defaultHandleClickEvent(style.getClickEvent(), mc, mc.screen);
                }
            }
        }
    }

    @Override
    public void onRelease(MouseButtonEvent mouseEvent) {
        this.scrollDragActive = false;
        super.onRelease(mouseEvent);
    }

    @Override
    public void onDrag(MouseButtonEvent mouseEvent, double dragX, double dragY) {
        if (this.visible && this.active && this.scrollDragActive) {
            setCurrentLineFromYPos(mouseEvent.y());
        }
    }

    private void setCurrentLineFromYPos(double mouseY) {
        if (mouseY < getY() + this.scrollBarSize * 0.5) {
            this.currentLine = 0;
        } else if (mouseY > getY() + this.height - this.scrollBarSize * 0.5) {
            this.currentLine = this.scrollSteps;
        } else {
            this.currentLine = (int) (
                (mouseY - getY() - this.scrollBarSize * 0.5) / (this.height - this.scrollBarSize) * this.scrollSteps
            );
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
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == GLFW.GLFW_KEY_UP || keyEvent.key() == GLFW.GLFW_KEY_DOWN) {
            if (mouseScrolled(0, 0, 0, keyEvent.key() == GLFW.GLFW_KEY_UP ? 1 : -1)) {
                return true;
            }
        }
        return super.keyPressed(keyEvent);
    }

    public Style getMouseoverStyle(double mouseX, double mouseY) {
        int lineIndex = this.getLineIndex(mouseX, mouseY);
        if (lineIndex >= 0 && lineIndex < this.formattedChars.size()) {
            ActiveTextCollector.ClickableStyleFinder finder = new ActiveTextCollector.ClickableStyleFinder(
                Minecraft.getInstance().font, (int) mouseX, (int) mouseY);
            finder.accept(getX() + this.padding, getY() + this.padding + lineIndex * 12,
                this.formattedChars.get(lineIndex));
            return finder.result();
        }
        return null;
    }

    private int getLineIndex(double mouseX, double mouseY) {
        if (!this.isMouseOver(mouseX, mouseY)) {
            return -1;
        } else {
            return (int) ((mouseY - this.getY() - this.padding * 0.5) / 12.0);
        }
    }
}
