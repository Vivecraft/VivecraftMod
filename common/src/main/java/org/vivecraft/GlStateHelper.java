package org.vivecraft;

import com.mojang.blaze3d.platform.GlStateManager;
import net.vulkanmod.vulkan.VRenderSystem;
import org.lwjgl.opengl.GL11;

public class GlStateHelper {
	public static void clear(int i) {
		VRenderSystem.clear(i);
	}
}
