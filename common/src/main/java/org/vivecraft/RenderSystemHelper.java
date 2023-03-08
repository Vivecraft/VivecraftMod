package org.vivecraft;

import com.mojang.blaze3d.systems.RenderSystem;

public class RenderSystemHelper {
	public static void clear(int i) {
		RenderSystem.clear(i, false);
	}
}
