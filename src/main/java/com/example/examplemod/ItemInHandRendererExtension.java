package com.example.examplemod;

import net.minecraft.world.phys.Vec3;

public interface ItemInHandRendererExtension {

	Object getNearOpaqueBlock(Vec3 position, double minClipDistance);

}
