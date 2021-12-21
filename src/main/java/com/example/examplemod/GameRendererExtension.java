package com.example.examplemod;

import net.minecraft.world.phys.Vec3;

public interface GameRendererExtension {

	boolean isInWater();

	boolean isInMenuRoom();

	Vec3 getControllerRenderPos(int i);

	Vec3 getCrossVec();

	void setMenuWorldFastTime(boolean b);

	void setupClipPlanes();

	float getMinClipDistance();

	float getClipDistance();

}
