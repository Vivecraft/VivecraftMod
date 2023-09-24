package org.vivecraft.client_vr.settings;

import org.vivecraft.client.utils.LangHelper;

import org.joml.Vector3d;

import net.minecraft.network.chat.Component;

import static org.vivecraft.client.utils.Utils.message;
import static org.vivecraft.client_vr.VRState.dh;

import static org.joml.Math.*;

public class AutoCalibration
{
    public static final float defaultHeight = 1.52F;

    public static void calibrateManual()
    {

        dh.vrSettings.manualCalibration = (float)dh.vr.hmdPivotHistory.averagePosition(0.5D, new Vector3d()).y;
        message(Component.literal(LangHelper.get("vivecraft.messages.heightset", round(100.0F * getPlayerHeight() / defaultHeight))));
        dh.vrSettings.saveOptions();
    }

    public static float getPlayerHeight()
    {
        return !dh.vrSettings.seated && dh.vrSettings.manualCalibration != -1.0F ?
            dh.vrSettings.manualCalibration : defaultHeight;
    }
}
