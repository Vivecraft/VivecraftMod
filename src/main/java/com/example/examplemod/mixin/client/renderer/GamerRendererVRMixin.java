package com.example.examplemod.mixin.client.renderer;

import java.nio.FloatBuffer;
import java.util.Locale;

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
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
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

	@Shadow
	private LightTexture lightTexture;

	private boolean guiLoadingVisible;

	@Override
	public boolean isInWater() {
		return inwater;
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
//			this.polyblendsrca = GlStateManager.BLEND.srcAlpha; TODO
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

	@Override
	public void drawScreen(float f, Screen screen, PoseStack poseStack) {
		PoseStack posestack = RenderSystem.getModelViewStack();
		posestack.pushPose();
		posestack.setIdentity();
		posestack.translate(0.0D, 0.0D, -2000.0D);
		RenderSystem.applyModelViewMatrix();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ONE);
		screen.render(poseStack, 0, 0, f);
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ONE);
		posestack.popPose();
		RenderSystem.applyModelViewMatrix();

		this.minecraft.getMainRenderTarget().bindRead();
		// this.minecraft.getMainRenderTarget().genMipMaps();
		this.minecraft.getMainRenderTarget().unbindRead();
	}

	@Override
	public boolean wasInWater() {
		return wasinwater;
	}

	@Override
	public void setWasInWater(boolean b) {
		this.wasinwater = b;
	}

	@Override
	public boolean isInPortal() {
		return this.inportal;
	}

	@Override
	public Matrix4f getThirdPassProjectionMatrix() {
		return thirdPassProjectionMatrix;
	}

	@Override
	public void drawFramebufferNEW(float partialTicks, boolean renderWorldIn, PoseStack matrixstack) {
		if (!this.minecraft.noRender) {
			Window window = this.minecraft.getWindow();
			Matrix4f matrix4f = Matrix4f.orthographic(0.0F, (float) ((double) window.getWidth() / window.getGuiScale()),
					0.0F, (float) ((double) window.getHeight() / window.getGuiScale()), 1000.0F, 3000.0F);
			RenderSystem.setProjectionMatrix(matrix4f);
			PoseStack posestack = RenderSystem.getModelViewStack();
			posestack.pushPose();
			posestack.setIdentity();
			posestack.translate(0.0D, 0.0D, -2000.0D);
			RenderSystem.applyModelViewMatrix();
			Lighting.setupFor3DItems();
			PoseStack posestack1 = new PoseStack();

			int i = (int) (this.minecraft.mouseHandler.xpos() * (double) this.minecraft.getWindow().getGuiScaledWidth()
					/ (double) this.minecraft.getWindow().getScreenWidth());
			int j = (int) (this.minecraft.mouseHandler.ypos() * (double) this.minecraft.getWindow().getGuiScaledHeight()
					/ (double) this.minecraft.getWindow().getScreenHeight());

			// Window window = this.minecraft.getWindow();
			// RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
			// RenderSystem.clear(16640, Minecraft.ON_OSX);
			// GL43.glMatrixMode(5889);
			// GL43.glPushMatrix();
			// GL43.glLoadIdentity();
			// GL43.glOrtho(0.0D, (double)window.getScreenWidth() / window.getGuiScale(),
			// (double)window.getScreenHeight() / window.getGuiScale(), 0.0D, 1000.0D,
			// 3000.0D);
			// GL43.glMatrixMode(5888);
			// GL43.glPushMatrix();
			// GL43.glLoadIdentity();
			// GL43.glTranslatef(0.0F, 0.0F, -2000.0F);
			// Lighting.setupFor3DItems();
			// RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			// GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
			// GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			
//			GlStateManager.alphaFunc(516, 0.01F);

//			if (this.lightTexture.isCustom()) { TODO
//				this.lightTexture.setAllowed(false);
//			}

			// this.lightTexture.turnOffLightLayer();
			DataHolder.getInstance().pumpkineffect = 0.0F;

			if (renderWorldIn && this.minecraft.level != null
					&& (!this.minecraft.options.hideGui || this.minecraft.screen != null)) {
				this.minecraft.getProfiler().popPush("gui");

//				if (Reflector.ForgeIngameGui.exists()) {
//					// RenderSystem.defaultAlphaFunc();
//					Reflector.ForgeIngameGui_renderVignette.setValue(false);
//					Reflector.ForgeIngameGui_renderPortal.setValue(false);
//					Reflector.ForgeIngameGui_renderCrosshairs.setValue(false);
//				}

				// no thanks.
				// if (this.minecraft.player != null)
				// {
				// float f = Mth.lerp(pPartialTicks, this.minecraft.player.oPortalTime,
				// this.minecraft.player.portalTime);
				//
				// if (f > 0.0F && this.minecraft.player.hasEffect(MobEffects.CONFUSION) &&
				// this.minecraft.options.screenEffectScale < 1.0F)
				// {
				// this.renderConfusionOverlay(f * (1.0F -
				// this.minecraft.options.screenEffectScale));
				// }
				// }

				if (!DataHolder.viewonly) {
					this.minecraft.gui.render(posestack1, partialTicks);
				}

//				if (this.minecraft.options.ofShowFps && !this.minecraft.options.renderDebug) { TODO
//					Config.drawFps(matrixstack);
//				} 

//				if (this.minecraft.options.renderDebug) { TODO
//					Lagometer.showLagometer(matrixstack, (int) this.minecraft.getWindow().getGuiScale());
//				}

				this.minecraft.getProfiler().pop();
				RenderSystem.clear(256, Minecraft.ON_OSX);
			}

			if (this.guiLoadingVisible != (this.minecraft.getOverlay() != null)) {
				if (this.minecraft.getOverlay() != null) {
					LoadingOverlay.registerTextures(this.minecraft);

					if (this.minecraft.getOverlay() instanceof LoadingOverlay) {
						LoadingOverlay loadingoverlay = (LoadingOverlay) this.minecraft.getOverlay();
						//loadingoverlay.update();
					}
				}

				this.guiLoadingVisible = this.minecraft.getOverlay() != null;
			}

			if (this.minecraft.getOverlay() != null) {
				try {
					this.minecraft.getOverlay().render(posestack1, i, j, this.minecraft.getDeltaFrameTime());
				} catch (Throwable throwable1) {
					CrashReport crashreport2 = CrashReport.forThrowable(throwable1, "Rendering overlay");
					CrashReportCategory crashreportcategory2 = crashreport2.addCategory("Overlay render details");
					crashreportcategory2.setDetail("Overlay name", () -> {
						return this.minecraft.getOverlay().getClass().getCanonicalName();
					});
					throw new ReportedException(crashreport2);
				}
			} else if (this.minecraft.screen != null) {
				try {
//					if (Config.isCustomEntityModels()) { TODO
//						CustomEntityModels.onRenderScreen(this.minecraft.screen);
//					}

//					if (Reflector.ForgeHooksClient_drawScreen.exists()) { TODO
//						Reflector.callVoid(Reflector.ForgeHooksClient_drawScreen, this.minecraft.screen, posestack1, i,
//								j, this.minecraft.getDeltaFrameTime());
//					} else {
						this.minecraft.screen.render(posestack1, i, j, this.minecraft.getDeltaFrameTime());
//					}
					// Vivecraft
//					this.minecraft.gui.drawMouseMenuQuad(i, j);
				} catch (Throwable throwable2) {
					CrashReport crashreport = CrashReport.forThrowable(throwable2, "Rendering screen");
					CrashReportCategory crashreportcategory = crashreport.addCategory("Screen render details");
					crashreportcategory.setDetail("Screen name", () -> {
						return this.minecraft.screen.getClass().getCanonicalName();
					});
					crashreportcategory.setDetail("Mouse location", () -> {
						return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%f, %f)", i, j,
								this.minecraft.mouseHandler.xpos(), this.minecraft.mouseHandler.ypos());
					});
					crashreportcategory.setDetail("Screen size", () -> {
						return String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f",
								this.minecraft.getWindow().getGuiScaledWidth(),
								this.minecraft.getWindow().getGuiScaledHeight(), this.minecraft.getWindow().getWidth(),
								this.minecraft.getWindow().getHeight(), this.minecraft.getWindow().getGuiScale());
					});
					throw new ReportedException(crashreport);
				}

				try {
					if (this.minecraft.screen != null) {
						this.minecraft.screen.handleDelayedNarration();
					}
				} catch (Throwable throwable1) {
					CrashReport crashreport1 = CrashReport.forThrowable(throwable1, "Narrating screen");
					CrashReportCategory crashreportcategory1 = crashreport1.addCategory("Screen details");
					crashreportcategory1.setDetail("Screen name", () -> {
						return this.minecraft.screen.getClass().getCanonicalName();
					});
					throw new ReportedException(crashreport1);
				}
			}

			//this.lightTexture.setAllowed(true);
			posestack.popPose();
			RenderSystem.applyModelViewMatrix();
		}

		if (this.minecraft.options.renderDebugCharts && !this.minecraft.options.hideGui) {
			//this.minecraft.drawProfiler();
		}

//		this.frameFinish();
//		this.waitForServerThread();
//		MemoryMonitor.update(); TODO
//		Lagometer.updateLagometer();

//		if (this.minecraft.options.ofProfiler) { TODO
//			this.minecraft.options.renderDebugCharts = true;
//		}

		// TODO: does this do anything?
		this.minecraft.getMainRenderTarget().bindRead();
		//this.minecraft.getMainRenderTarget().genMipMaps();
		this.minecraft.getMainRenderTarget().unbindRead();

	}

}
