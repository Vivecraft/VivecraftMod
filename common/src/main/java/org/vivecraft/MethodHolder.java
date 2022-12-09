package org.vivecraft;

import com.mojang.math.Axis;
import org.joml.AxisAngle4f;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.provider.InputSimulator;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;

import net.minecraft.client.Minecraft;

public abstract class MethodHolder {
	
	public static boolean isKeyDown(int i) {
		return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().getWindow(), i) == 1 || InputSimulator.isKeyDown(i);
	}
	
	public static void notifyMirror(String text, boolean clear, int lengthMs)
	{
		ClientDataHolder clientDataHolder = ClientDataHolder.getInstance();
		clientDataHolder.mirroNotifyStart = System.currentTimeMillis();
		clientDataHolder.mirroNotifyLen = (long)lengthMs;
		clientDataHolder.mirrorNotifyText = text;
		clientDataHolder.mirrorNotifyClear = clear;
	}
	
	public static void rotateDeg(PoseStack pose, float angle, float x, float y, float z) {
		pose.mulPose(new Quaternionf(new AxisAngle4f(angle * 0.017453292F, x, y, z)));
	}

	public static void rotateDegXp(PoseStack matrix, int i) {
		matrix.mulPose(Axis.XP.rotationDegrees(i));
	}
}
