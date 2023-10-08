package org.vivecraft.client_vr.provider.openvr_lwjgl.control;

import net.minecraft.client.KeyMapping;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

import static org.joml.Math.abs;
import static org.vivecraft.client_vr.VRState.dh;

public class VivecraftMovementInput {
    public static float getMovementAxisValue(KeyMapping keyBinding) {
        VRInputAction vrinputaction = dh.vr.getInputAction(keyBinding);
        return abs(vrinputaction.getAxis1DUseTracked());
    }
}
