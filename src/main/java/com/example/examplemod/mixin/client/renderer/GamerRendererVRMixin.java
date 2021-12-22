package com.example.examplemod.mixin.client.renderer;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.gameplay.trackers.TelescopeTracker;
import org.vivecraft.render.RenderPass;
import org.vivecraft.render.VRCamera;

import com.example.examplemod.DataHolder;
import com.example.examplemod.GameRendererExtension;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

@Mixin(GameRenderer.class)
public class GamerRendererVRMixin implements GameRendererExtension {

	@Unique
	private final Camera mainCamera = new VRCamera();

	@Unique
	private float clipDistance = 128.0F;

	public float minClipDistance = 0.02F;
	public Vec3 crossVec;
	private FloatBuffer matrixBuffer = MemoryTracker.create(16).asFloatBuffer();
	public Matrix4f thirdPassProjectionMatrix = new Matrix4f();
	public boolean menuWorldFastTime;
	public boolean inwater;
	public boolean wasinwater;
	public boolean inportal;
	public boolean onfire;
	public float inBlock = 0.0F;
	private boolean always_true = true;
	public double rveX;
	public double rveY;
	public double rveZ;
	public double rvelastX;
	public double rvelastY;
	public double rvelastZ;
	public double rveprevX;
	public double rveprevY;
	public double rveprevZ;
	public float rveyaw;
	public float rvepitch;
	private float rvelastyaw;
	private float rvelastpitch;
	private float rveHeight;
	private boolean cached;
	private int polyblendsrca;
	private int polyblenddsta;
	private int polyblendsrcrgb;
	private int polyblenddstrgb;
	// private net.optifine.shaders.Program prog;
	private boolean polyblend;
	private boolean polytex;
	private boolean polylight;
	private boolean polycull;
	Vec3i tpUnlimitedColor = new Vec3i(-83, -40, -26);
	Vec3i tpLimitedColor = new Vec3i(-51, -87, -51);
	Vec3i tpInvalidColor = new Vec3i(83, 83, 83);

	@Shadow
	private Minecraft minecraft;

	@Shadow
	private float renderDistance;

	@Override
	public boolean isInWater() {
		return wasinwater;
	}

	@Override
	public boolean isInMenuRoom() {
		return false;
	}

	@Override
	public Vec3 getControllerRenderPos(int c) {
		DataHolder dataholder = DataHolder.getInstance();
		if (!dataholder.vrSettings.seated) {
			return dataholder.vrPlayer.vrdata_world_render.getController(c).getPosition();
		} else {
			Vec3 vec3;

			if (this.minecraft.getCameraEntity() != null && this.minecraft.level != null) {
				Vec3 vec32 = dataholder.vrPlayer.vrdata_world_render.hmd.getDirection();
				vec32 = vec32.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
				vec32 = new Vec3(vec32.x, 0.0D, vec32.z);
				vec32 = vec32.normalize();
				RenderPass renderpass = RenderPass.CENTER;
				vec3 = dataholder.vrPlayer.vrdata_world_render.getEye(renderpass).getPosition().add(
						vec32.x * 0.3D * (double) dataholder.vrPlayer.vrdata_world_render.worldScale,
						-0.4D * (double) dataholder.vrPlayer.vrdata_world_render.worldScale,
						vec32.z * 0.3D * (double) dataholder.vrPlayer.vrdata_world_render.worldScale);

				if (TelescopeTracker.isTelescope(minecraft.player.getUseItem())) {
					if (c == 0 && minecraft.player.getUsedItemHand() == InteractionHand.MAIN_HAND)
						vec3 = dataholder.vrPlayer.vrdata_world_render.eye0.getPosition()
								.add(dataholder.vrPlayer.vrdata_world_render.hmd.getDirection()
										.scale(0.2 * dataholder.vrPlayer.vrdata_world_render.worldScale));
					if (c == 1 && minecraft.player.getUsedItemHand() == InteractionHand.OFF_HAND)
						vec3 = dataholder.vrPlayer.vrdata_world_render.eye1.getPosition()
								.add(dataholder.vrPlayer.vrdata_world_render.hmd.getDirection()
										.scale(0.2 * dataholder.vrPlayer.vrdata_world_render.worldScale));
				}

			} else {
				Vec3 vec31 = dataholder.vrPlayer.vrdata_world_render.hmd.getDirection();
				vec31 = vec31.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
				vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
				vec31 = vec31.normalize();
				vec3 = dataholder.vrPlayer.vrdata_world_render.hmd.getPosition().add(vec31.x * 0.3D, -0.4D,
						vec31.z * 0.3D);
			}

			return vec3;
		}
	}

	@Override
	public Vec3 getCrossVec() {
		return crossVec;
	}

	@Override
	public void setMenuWorldFastTime(boolean b) {
		this.menuWorldFastTime = b;
	}

	@Override
	public void setupClipPlanes() {
		this.renderDistance = (float) (this.minecraft.options.renderDistance * 16);

//		if (Config.isFogOn()) { TODO
//			this.renderDistance *= 0.95F;
//		}

		this.clipDistance = this.renderDistance + 1024.0F;

	}

	@Override
	public float getMinClipDistance() {
		return this.minClipDistance;
	}

	@Override
	public float getClipDistance() {
		return this.clipDistance;
	}

	@Override
	public void applyVRModelView(RenderPass currentPass, PoseStack poseStack) {
		poseStack.last().pose().multiply(DataHolder.getInstance().vrPlayer.vrdata_world_render.getEye(currentPass)
				.getMatrix().transposed().toMCMatrix());
	}

	@Override
	public void renderDebugAxes(int r, int g, int b, float radius) {
		this.setupPolyRendering(true);
		RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
		this.renderCircle(new Vec3(0.0D, 0.0D, 0.0D), radius, 32, r, g, b, 255, 0);
		this.renderCircle(new Vec3(0.0D, 0.01D, 0.0D), radius * 0.75F, 32, r, g, b, 255, 0);
		this.renderCircle(new Vec3(0.0D, 0.02D, 0.0D), radius * 0.25F, 32, r, g, b, 255, 0);
		this.renderCircle(new Vec3(0.0D, 0.0D, 0.15D), radius * 0.5F, 32, r, g, b, 255, 2);
		this.setupPolyRendering(false);
	}

	public void renderCircle(Vec3 pos, float radius, int edges, int r, int g, int b, int a, int side) {
		Tesselator tesselator = Tesselator.getInstance();
		tesselator.getBuilder().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		tesselator.getBuilder().vertex(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();

		for (int i = 0; i < edges + 1; ++i) {
			float f = (float) i / (float) edges * (float) Math.PI * 2.0F;

			if (side != 0 && side != 1) {
				if (side != 2 && side != 3) {
					if (side == 4 || side == 5) {
						float f5 = (float) pos.x;
						float f7 = (float) pos.y + (float) Math.cos((double) f) * radius;
						float f9 = (float) pos.z + (float) Math.sin((double) f) * radius;
						tesselator.getBuilder().vertex((double) f5, (double) f7, (double) f9).color(r, g, b, a)
								.endVertex();
					}
				} else {
					float f4 = (float) pos.x + (float) Math.cos((double) f) * radius;
					float f6 = (float) pos.y + (float) Math.sin((double) f) * radius;
					float f8 = (float) pos.z;
					tesselator.getBuilder().vertex((double) f4, (double) f6, (double) f8).color(r, g, b, a).endVertex();
				}
			} else {
				float f1 = (float) pos.x + (float) Math.cos((double) f) * radius;
				float f2 = (float) pos.y;
				float f3 = (float) pos.z + (float) Math.sin((double) f) * radius;
				tesselator.getBuilder().vertex((double) f1, (double) f2, (double) f3).color(r, g, b, a).endVertex();
			}
		}

		tesselator.end();
	}

	private void setupPolyRendering(boolean enable) {
//		boolean flag = Config.isShaders(); TODO
		boolean flag = false;

		if (enable) {
//			this.polyblendsrca = GlStateManager.BLEND.srcAlpha;
//			this.polyblenddsta = GlStateManager.BLEND.dstAlpha;
//			this.polyblendsrcrgb = GlStateManager.BLEND.srcRgb;
//			this.polyblenddstrgb = GlStateManager.BLEND.dstRgb;
			this.polyblend = GL11.glIsEnabled(GL11.GL_BLEND);
			this.polytex = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
			this.polylight = GL11.glIsEnabled(GL11.GL_LIGHTING);
			this.polycull = GL11.glIsEnabled(GL11.GL_CULL_FACE);
			GlStateManager._enableBlend();
			RenderSystem.defaultBlendFunc();
			GlStateManager._disableTexture();
			// GlStateManager._disableLighting();
			GlStateManager._disableCull();

			if (flag) {
//				this.prog = Shaders.activeProgram; TODO
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
			}
		} else {
			RenderSystem.blendFuncSeparate(this.polyblendsrcrgb, this.polyblenddstrgb, this.polyblendsrca,
					this.polyblenddsta);

			if (!this.polyblend) {
				GlStateManager._disableBlend();
			}

			if (this.polytex) {
				GlStateManager._enableTexture();
			}

			if (this.polylight) {
				// GlStateManager._enableLighting();
			}

			if (this.polycull) {
				GlStateManager._enableCull();
			}

//			if (flag && this.polytex) {
//				Shaders.useProgram(this.prog); TODO
//			}
		}
	}

}
