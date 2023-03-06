package org.vivecraft;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

public class GlStateHelper {
	public static void clear(int i) {
		RenderSystem.clear(i, false);
	}
}
