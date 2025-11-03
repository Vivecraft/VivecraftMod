package org.vivecraft.client_vr.gui.keyboard;

import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.utils.RGBAColor;

public class EasterEggTheme implements KeyboardTheme.PositionTheme {

    public static final EasterEggTheme INSTANCE = new EasterEggTheme();

    @Override
    public void updateColor(RGBAColor color, int keyX, int keyY) {
        // https://qimg.techjargaming.com/i/UkG1cWAh.png
        color.setRGB(RGBAColor.fromHSB(
            (ClientDataHolderVR.getInstance().tickCounter + ClientUtils.getCurrentPartialTick()) / 100.0F +
                keyX / (KeyboardKeys.COLUMNS * 3F),
            1.0F,
            1.0F));
    }
}
