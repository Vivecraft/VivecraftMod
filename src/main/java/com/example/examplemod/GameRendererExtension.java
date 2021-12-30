package com.example.examplemod;

import org.vivecraft.render.RenderPass;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;

public interface GameRendererExtension {

	boolean wasInWater();
	
	void setWasInWater(boolean b);
	
	boolean isInWater();

	boolean isInMenuRoom();
	
	boolean isInPortal();

	Vec3 getControllerRenderPos(int i);

	Vec3 getCrossVec();

	void setMenuWorldFastTime(boolean b);

	void setupClipPlanes();

	float getMinClipDistance();

	float getClipDistance();

	void applyVRModelView(RenderPass currentPass, PoseStack poseStack);

	void renderDebugAxes(int i, int j, int k, float f);

	void drawScreen(float f, Screen screen, PoseStack poseStack);

	Matrix4f getThirdPassProjectionMatrix();

	void drawFramebufferNEW(float f, boolean pRenderLevel, PoseStack poseStack);

	void drawEyeStencil(boolean flag1);

	float inBlock();

	double getRveY();

	void renderVrFast(float f, boolean b, boolean menuhandright, boolean menuHandleft, PoseStack poseStack);

}