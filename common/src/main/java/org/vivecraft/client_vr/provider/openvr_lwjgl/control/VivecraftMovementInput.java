package org.vivecraft.client_vr.provider.openvr_lwjgl.control;

import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

import net.minecraft.client.KeyMapping;

import static org.vivecraft.client_vr.VRState.dh;

import static org.joml.Math.*;

public class VivecraftMovementInput
{
    public static float getMovementAxisValue(KeyMapping keyBinding)
    {
        VRInputAction vrinputaction = dh.vr.getInputAction(keyBinding);
        return abs(vrinputaction.getAxis1DUseTracked());
    }
}
