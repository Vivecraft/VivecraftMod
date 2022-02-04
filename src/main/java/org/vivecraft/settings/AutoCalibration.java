package org.vivecraft.settings;

import org.vivecraft.utils.LangHelper;

import com.example.examplemod.DataHolder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;

public class AutoCalibration
{
    public static final float defaultHeight = 1.52F;

    public static void calibrateManual()
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        dataHolder.vrSettings.manualCalibration = (float)dataHolder.vr.hmdPivotHistory.averagePosition(0.5D).y;
        int i = (int)((float)((double)Math.round(100.0D * (double)getPlayerHeight() / (double)1.52F)));
        minecraft.gui.getChat().addMessage(new TextComponent(LangHelper.get("vivecraft.messages.heightset", i)));
        dataHolder.vrSettings.saveOptions();
    }

    public static float getPlayerHeight()
    {
        Minecraft minecraft = Minecraft.getInstance();
        DataHolder dataHolder = DataHolder.getInstance();
        float f = 1.52F;

        if (dataHolder.vrSettings.seated)
        {
            return f;
        }
        else
        {
            if (dataHolder.vrSettings.manualCalibration != -1.0F)
            {
                f = dataHolder.vrSettings.manualCalibration;
            }

            return f;
        }
    }
}
