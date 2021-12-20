package org.vivecraft.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import org.vivecraft.utils.LangHelper;

public class AutoCalibration
{
    public static final float defaultHeight = 1.52F;

    public static void calibrateManual()
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.vrSettings.manualCalibration = (float)minecraft.vr.hmdPivotHistory.averagePosition(0.5D).y;
        int i = (int)((float)((double)Math.round(100.0D * (double)getPlayerHeight() / (double)1.52F)));
        minecraft.gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.heightset", i)));
        minecraft.vrSettings.saveOptions();
    }

    public static float getPlayerHeight()
    {
        Minecraft minecraft = Minecraft.getInstance();
        float f = 1.52F;

        if (minecraft.vrSettings.seated)
        {
            return f;
        }
        else
        {
            if (minecraft.vrSettings.manualCalibration != -1.0F)
            {
                f = minecraft.vrSettings.manualCalibration;
            }

            return f;
        }
    }
}
