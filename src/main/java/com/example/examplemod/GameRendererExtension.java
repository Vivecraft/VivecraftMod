package com.example.examplemod;

import org.vivecraft.render.RenderPass;

import com.mojang.blaze3d.vertex.PoseStack;

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

	void applyVRModelView(RenderPass currentPass, PoseStack poseStack);

	void renderDebugAxes(int i, int j, int k, float f);

}
