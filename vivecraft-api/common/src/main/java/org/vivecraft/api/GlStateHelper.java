package org.vivecraft.api;

import com.mojang.blaze3d.platform.GlStateManager;
import org.lwjgl.opengl.GL11;

public class GlStateHelper {
	public static void clear(int i) {
		GlStateManager._clear(i, false);
	}
}
