package org.vivecraft.client.gui.framework.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
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
        super(key.icon() != null ? Component.empty() : key.label(), x, y, width, height, onPress);
        this.key = key;
        this.dh = ClientDataHolderVR.getInstance();
        this.keyboardTheme = keyboardTheme;
    }

    @Override
    public void onPress() {
        if (this.onPress != null) {
            super.onPress();
        } else {
            this.key.onPress().run();
            this.key.onRelease().run();
        }
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        KeyboardTheme theme =
            this.keyboardTheme != null ? this.keyboardTheme : this.dh.vrSettings.physicalKeyboardTheme;
        theme.theme.updateColor(this.getColor(), this.key.id(), this.key.x(),
            this.key.y());
        super.renderWidget(poseStack, mouseX, mouseY, partialTick);

        if (this.key.icon() != null) {
            RenderSystem.setShaderTexture(0, this.key.icon().location());
            blit(poseStack,
                this.getX() + this.getWidth() / 2 - this.key.icon().width() / 2,
                this.getY() + this.getHeight() / 2 - this.key.icon().height() / 2,
                this.key.icon().u(), this.key.icon().v(),
                this.key.icon().width(), this.key.icon().height(),
                this.key.icon().texWidth(), this.key.icon().texHeight());
        }
    }
}
