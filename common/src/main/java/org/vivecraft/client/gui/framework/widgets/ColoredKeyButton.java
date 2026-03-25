package org.vivecraft.client.gui.framework.widgets;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.InputWithModifiers;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gui.keyboard.KeyboardKeys;
import org.vivecraft.client_vr.gui.keyboard.KeyboardTheme;

/**
 * Button that has a color tint that his linked to the keyboard key
 */
public class ColoredKeyButton extends ColoredButton {

    private final KeyboardKeys.Key key;
    private final ClientDataHolderVR dh;

    private final KeyboardTheme keyboardTheme;

    public ColoredKeyButton(KeyboardKeys.Key key, int x, int y, int width, int height) {
        this(key, x, y, width, height, null, null);
    }

    public ColoredKeyButton(
        KeyboardKeys.Key key, int x, int y, int width, int height, OnPress onPress, KeyboardTheme keyboardTheme)
    {
        super(key.label(), x, y, width, height, onPress);
        this.key = key;
        this.dh = ClientDataHolderVR.getInstance();
        this.keyboardTheme = keyboardTheme;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (this.onPress != null) {
            super.onPress(input);
        } else {
            this.key.onPress().run();
            this.key.onRelease().run();
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        KeyboardTheme theme =
            this.keyboardTheme != null ? this.keyboardTheme : this.dh.vrSettings.physicalKeyboardTheme;
        theme.theme.updateColor(this.getColor(), this.key.id(), this.key.x(),
            this.key.y());
        super.extractContents(graphics, mouseX, mouseY, partialTick);
    }
}
