package org.vivecraft.client_vr;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client_vr.provider.InputSimulator;

public abstract class MethodHolder {

    public static boolean isKeyDown(int i) {
        return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), i) == 1 || InputSimulator.isKeyDown(i);
    }

    public static void notifyMirror(String text, boolean clear, int lengthMs) {
        ClientDataHolderVR clientDataHolderVR = ClientDataHolderVR.getInstance();
        clientDataHolderVR.mirroNotifyStart = System.currentTimeMillis();
        clientDataHolderVR.mirroNotifyLen = lengthMs;
        clientDataHolderVR.mirrorNotifyText = text;
        clientDataHolderVR.mirrorNotifyClear = clear;
    }
}
