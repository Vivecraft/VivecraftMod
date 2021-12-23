package com.example.examplemod;

import com.mojang.blaze3d.platform.GlStateManager;

public class GlStateHelper {

	public static void enableAlphaTest() {
	}

	public static void clear(int i) {
		GlStateManager._clear(i, false);
	}

}
