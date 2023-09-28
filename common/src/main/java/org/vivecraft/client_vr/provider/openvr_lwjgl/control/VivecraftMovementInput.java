package org.vivecraft.client_vr.provider.openvr_lwjgl.control;

import net.minecraft.client.KeyMapping;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

public class VivecraftMovementInput {
    public static float getMovementAxisValue(KeyMapping keyBinding) {
        VRInputAction vrinputaction = MCVR.get().getInputAction(keyBinding);
        return Math.abs(vrinputaction.getAxis1DUseTracked());
    }
}
