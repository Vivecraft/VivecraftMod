package com.example.vivecraftfabric;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GlStateHelper {

	private static boolean alphaTest;
	private static float alphaTestRef;
	private static int alphaTestFunc;

	public static void enableAlphaTest() {
		alphaTest = true;
		GlStateHelper.applyAlphaTest();
	}

	public static void clear(int i) {
		GlStateManager._clear(i, false);
	}

	public static void alphaFunc(int func, float ref) {
		alphaTestFunc = func;
		alphaTestRef = ref;
	}

	private static void applyAlphaTest() {
	}

	public static void disableAlphaTest() {
		alphaTest = false;
	}

}
