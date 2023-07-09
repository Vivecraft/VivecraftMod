package org.vivecraft.client_vr;

import org.lwjgl.glfw.GLFW;
import org.vivecraft.client_vr.provider.InputSimulator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;

public abstract class MethodHolder {
	
	public static boolean isKeyDown(int i) {
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), i) == 1 || InputSimulator.isKeyDown(i);
	}
	
	public static void notifyMirror(String text, boolean clear, int lengthMs)
	{
		ClientDataHolderVR clientDataHolderVR = ClientDataHolderVR.getInstance();
		clientDataHolderVR.mirroNotifyStart = System.currentTimeMillis();
		clientDataHolderVR.mirroNotifyLen = (long)lengthMs;
		clientDataHolderVR.mirrorNotifyText = text;
		clientDataHolderVR.mirrorNotifyClear = clear;
	}
	
	public static void rotateDeg(PoseStack pose, float angle, float x, float y, float z) {
		Vector3f vec = new Vector3f(x, y, z);
		Quaternion quat = vec.rotationDegrees(angle);
		pose.mulPose(quat);
	}

	public static void rotateDegXp(PoseStack matrix, int i) {
		matrix.mulPose(Vector3f.XP.rotationDegrees(i));
	}
}
