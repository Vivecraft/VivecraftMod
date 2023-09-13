package org.vivecraft.client_vr.extensions;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.vivecraft.client_vr.render.RenderPass;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.phys.Vec3;

public interface GameRendererExtension {

	boolean vivecraft$wasInWater();
	
	void vivecraft$setWasInWater(boolean b);

	boolean vivecraft$isInWater();

	boolean vivecraft$isInPortal();

	float vivecraft$inBlock();

	void vivecraft$setupRVE();

	void vivecraft$cacheRVEPos(LivingEntity e);

	void vivecraft$restoreRVEPos(LivingEntity e);

	double vivecraft$getRveY();

	boolean vivecraft$isInMenuRoom();

	boolean vivecraft$willBeInMenuRoom(Screen newScreen);

	Vec3 vivecraft$getControllerRenderPos(int i);

	Vec3 vivecraft$getCrossVec();

	Matrix4f vivecraft$getThirdPassProjectionMatrix();

	void vivecraft$setupClipPlanes();

	float vivecraft$getMinClipDistance();

	float vivecraft$getClipDistance();

	void vivecraft$applyVRModelView(RenderPass currentPass, PoseStack poseStack);

	void vivecraft$renderDebugAxes(int i, int j, int k, float f);

	void vivecraft$drawScreen(float f, Screen screen, GuiGraphics guiGraphics);

	void vivecraft$DrawScopeFB(PoseStack matrixStackIn, int i);

	void vivecraft$drawEyeStencil(boolean flag1);

	void vivecraft$renderVrFast(float partialTicks, boolean secondpass, boolean menuhandright, boolean menuHandleft, PoseStack poseStack);

	void vivecraft$renderVRFabulous(float f, LevelRenderer levelRenderer, boolean menuhandright, boolean menuHandleft, PoseStack poseStack);

	void vivecraft$setShouldDrawScreen(boolean shouldDrawScreen);
	void vivecraft$setShouldDrawGui(boolean shouldDrawGui);
}
