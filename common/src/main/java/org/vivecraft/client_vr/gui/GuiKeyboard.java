package org.vivecraft.client_vr.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.vivecraft.client.gui.framework.screens.TwoHandedScreen;
import org.vivecraft.client.gui.framework.widgets.ColoredKeyButton;
import org.vivecraft.client_vr.gui.keyboard.KeyboardKeys;

public class GuiKeyboard extends TwoHandedScreen {
    private boolean isShift = false;

    @Override
    public void init() {
        this.clearWidgets();

        KeyboardKeys.Layout layout = KeyboardKeys.getRegularKeys(this.isShift, () -> {});

        int margin = 32;
        int spacing = 2;
        int buttonWidth = 25;
        int specialWidth = 30;

        int offset = specialWidth - (buttonWidth * 2 + spacing);

        for (KeyboardKeys.Key key : layout.keys()) {
            int y = key.y() < 0 ? layout.rows() - key.y() : key.y();
            this.addRenderableWidget(new ColoredKeyButton(key,
                key.x() * (buttonWidth + spacing) + offset,
                margin + (y - 1) * (20 + spacing), buttonWidth, 20));
        }

        for (KeyboardKeys.Key key : KeyboardKeys.getSpecialKeys(() -> this.setShift(!this.isShift))) {
            if (key.width() == 1) {
                // the arrow keys are on a different spot in the gui keyboard
                key = new KeyboardKeys.Key(key.id(), key.x() - 2, key.y() + layout.rows() - KeyboardKeys.ROWS,
                    key.width(), key.height(), key.label(), key.onPress(), key.onRelease());
            }
            int y = key.y() < 0 ? layout.rows() - key.y() : key.y();
            int xPos = (key.x() > 0 ? offset : 0) + key.x() * (buttonWidth + spacing);
            int width = key.x() == 0 ? specialWidth : key.width() * buttonWidth + (key.width() - 1) * spacing;
            // don't let buttons go offscreen
            if (xPos + width > this.width) {
                width -= xPos + width - this.width;
            }
            this.addRenderableWidget(new ColoredKeyButton(key,
                xPos, margin + (y - 1) * (20 + spacing),
                width, 20));
        }
        this.dh.vrSettings.physicalKeyboardTheme.theme.reload();
    }

    public void setShift(boolean shift) {
        if (shift != this.isShift) {
            this.isShift = shift;
            this.reinit = true;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(this.font, "Keyboard", this.width / 2, 2, 0xFFFFFFFF);
        super.extractRenderState(graphics, 0, 0, partialTick);
    }
}
