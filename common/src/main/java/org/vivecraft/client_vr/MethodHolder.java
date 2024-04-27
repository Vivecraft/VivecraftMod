package org.vivecraft.client_vr;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Unique;
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

    public static void rotateDeg(Matrix4f pose, float angle, float x, float y, float z) {
        pose.rotate(new Quaternionf(new AxisAngle4f(angle * 0.017453292F, x, y, z)));
    }

    public static void rotateDegXp(PoseStack matrix, int i) {
        matrix.mulPose(Axis.XP.rotationDegrees(i));
    }

    public static boolean isInMenuRoom() {
        return willBeInMenuRoom(Minecraft.getInstance().screen);
    }

    public static boolean willBeInMenuRoom(Screen newScreen) {
        return Minecraft.getInstance().level == null ||
            newScreen instanceof WinScreen ||
            newScreen instanceof ReceivingLevelScreen ||
            newScreen instanceof ProgressScreen ||
            newScreen instanceof GenericMessageScreen ||
            ClientDataHolderVR.getInstance().integratedServerLaunchInProgress ||
            Minecraft.getInstance().getOverlay() != null;
    }
}
