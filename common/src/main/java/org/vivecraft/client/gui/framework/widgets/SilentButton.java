package org.vivecraft.client.gui.framework.widgets;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

/**
 * Button that doesn't play a click sound
 */
public class SilentButton extends Button {
    public SilentButton(Component message, OnPress onPress, int width, int height) {
        super(0, 0, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void playDownSound(SoundManager handler) {}
}
