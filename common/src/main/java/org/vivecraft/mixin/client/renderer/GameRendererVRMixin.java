package org.vivecraft.mixin.client.renderer;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import me.jellysquid.mods.lithium.common.util.Pos;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.IrisHelper;
import org.vivecraft.MethodHolder;
import org.vivecraft.Xevents;
import org.vivecraft.Xplat;
import org.vivecraft.extensions.GameRendererExtension;
import org.vivecraft.extensions.GuiExtension;
import org.vivecraft.extensions.ItemInHandRendererExtension;
import org.vivecraft.extensions.LevelRendererExtension;
import org.vivecraft.extensions.MinecraftExtension;
import org.vivecraft.extensions.PlayerExtension;
import org.vivecraft.extensions.RenderTargetExtension;
import org.vivecraft.api.ClientNetworkHelper;
import org.vivecraft.api.VRData;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.gameplay.trackers.BowTracker;
import org.vivecraft.gameplay.trackers.TelescopeTracker;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.render.RenderPass;
import org.vivecraft.render.VRCamera;
import org.vivecraft.render.VRWidgetHelper;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.Utils;

import java.nio.FloatBuffer;
import java.util.Locale;

@Mixin(GameRenderer.class)
public abstract class GameRendererVRMixin
		implements ResourceManagerReloadListener, AutoCloseable, GameRendererExtension {

	private static final ClientDataHolder DATA_HOLDER = ClientDataHolder.getInstance();
	@Unique
	public float minClipDistance = 0.02F;
	@Unique
	public Vec3 crossVec;
	@Unique
	private final FloatBuffer matrixBuffer = MemoryTracker.create(16).asFloatBuffer();
	@Unique
	public Matrix4f thirdPassProjectionMatrix = new Matrix4f();
	@Unique
	public boolean menuWorldFastTime;
	@Unique
	public boolean inwater;
	@Unique
	public boolean wasinwater;
	@Unique
	public boolean inportal;
	@Unique
	public boolean onfire;
	@Unique
	public float inBlock = 0.0F;
	@Unique
	private boolean always_true = true;
	@Unique
	public double rveX;
	@Unique
	public double rveY;
	@Unique
	public double rveZ;
	@Unique
	public double rvelastX;
	@Unique
	public double rvelastY;
	@Unique
	public double rvelastZ;
	@Unique
	public double rveprevX;
	@Unique
	public double rveprevY;
	@Unique
	public double rveprevZ;
	@Unique
	public float rveyaw;
	@Unique
	public float rvepitch;
	@Unique
	private float rvelastyaw;
	@Unique
	private float rvelastpitch;
	@Unique
	private float rveHeight;
	@Unique
	private boolean cached;
	@Unique
	private int polyblendsrca;
	@Unique
	private int polyblenddsta;
	@Unique
	private int polyblendsrcrgb;
	@Unique
	private int polyblenddstrgb;
	// private net.optifine.shaders.Program prog;
	@Unique
	private boolean polyblend;
	@Unique
	private boolean polytex;
	@Unique
	private boolean polylight;
	@Unique
	private boolean polycull;
	@Unique
	private Vec3i tpUnlimitedColor = new Vec3i(-83, -40, -26);
	@Unique
	private Vec3i tpLimitedColor = new Vec3i(-51, -87, -51);
	@Unique
	private Vec3i tpInvalidColor = new Vec3i(83, 83, 83);

	@Shadow
	@Final
	@Mutable
	private final Camera mainCamera = new VRCamera();

	@Unique // TODO added by optifine...
	private float clipDistance = 128.0F;

	@Unique // TODO added by optifine...
	private boolean guiLoadingVisible;

	@Unique
	private PoseStack stack;

	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	private float renderDistance;

	@Shadow
	@Final
	private LightTexture lightTexture;
	@Shadow
	private float zoom;
	@Shadow
	private float zoomX;
	@Shadow
	private float zoomY;
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	@Shadow
	@Final
	public ItemInHandRenderer itemInHandRenderer;
	@Shadow
	private int tick;
	@Shadow
	private boolean renderHand;

	@Shadow
	public abstract Matrix4f getProjectionMatrix(double fov);

	@Shadow
	protected abstract double getFov(Camera mainCamera2, float partialTicks, boolean b);

	@Shadow
	public abstract void resetProjectionMatrix(Matrix4f projectionMatrix);

	@Shadow
	protected abstract void renderItemActivationAnimation(int i, int j, float par1);

	@Shadow
	public abstract void pick(float f);

	@Shadow
	private boolean effectActive;

	@Shadow
	private long lastActiveTime;

	@Override
	public double getRveY() {
		return rveY;
	}
	
	@Override
	public float inBlock() {
		return inBlock;
	}

	// TODO check
	public void init() {
		if (this.minecraft.gameRenderer != null) {
			System.out.println("**********NEW GAME RENDERER ***********");
			Thread.dumpStack();
		}
	}

	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"), method = "pick")
	public ClientLevel appendCheck(Minecraft instance) {
		return ClientDataHolder.getInstance().vrPlayer.vrdata_world_render == null ? null : instance.level;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;"), method = "pick(F)V")
	public Vec3 rayTrace(Entity e, float f) {
		this.minecraft.hitResult = GameRendererVRMixin.DATA_HOLDER.vrPlayer.rayTraceBlocksVR(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render, 0, this.minecraft.gameMode.getPickRange(), false);
		this.crossVec = GameRendererVRMixin.DATA_HOLDER.vrPlayer.AimedPointAtDistance(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render, 0, this.minecraft.gameMode.getPickRange());
		return GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getPosition();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"), method = "pick(F)V")
	public Vec3 vrVec31(Entity e, float f) {
		return GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getDirection();
	}

	//TODO Vivecraft add riding check in case your hand is somewhere inappropriate

	@Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)D", cancellable = true)
	public void fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Double> info) {
		if (this.minecraft.level == null) { // Vivecraft: using this on the main menu
			info.setReturnValue(this.minecraft.options.fov);
		}
	}

	@Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lcom/mojang/math/Matrix4f;", cancellable = true)
	public void projection(double d, CallbackInfoReturnable<Matrix4f> info) {
		PoseStack posestack = new PoseStack();
		setupClipPlanes();
		if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.LEFT) {
			posestack.mulPoseMatrix(GameRendererVRMixin.DATA_HOLDER.vrRenderer.eyeproj[0]);
		} else if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.RIGHT) {
			posestack.mulPoseMatrix(GameRendererVRMixin.DATA_HOLDER.vrRenderer.eyeproj[1]);
		} else if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.THIRD) {
			if (GameRendererVRMixin.DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
				posestack.mulPoseMatrix(
						Matrix4f.perspective((double) GameRendererVRMixin.DATA_HOLDER.vrSettings.mixedRealityFov,
								GameRendererVRMixin.DATA_HOLDER.vrSettings.mixedRealityAspectRatio, this.minClipDistance,
								this.clipDistance));
			} else {
				posestack.mulPoseMatrix(
						Matrix4f.perspective((double) GameRendererVRMixin.DATA_HOLDER.vrSettings.mixedRealityFov,
								(float) this.minecraft.getWindow().getScreenWidth()
										/ (float) this.minecraft.getWindow().getScreenHeight(),
								this.minClipDistance, this.clipDistance));
			}
			this.thirdPassProjectionMatrix = new Matrix4f(posestack.last().pose());
		} else if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.CAMERA) {
			posestack.mulPoseMatrix(Matrix4f.perspective((double) GameRendererVRMixin.DATA_HOLDER.vrSettings.handCameraFov,
					(float) GameRendererVRMixin.DATA_HOLDER.vrRenderer.cameraFramebuffer.viewWidth
							/ (float) GameRendererVRMixin.DATA_HOLDER.vrRenderer.cameraFramebuffer.viewHeight,
					this.minClipDistance, this.clipDistance));
		} else if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.SCOPEL
				|| GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.SCOPER) {
			posestack.mulPoseMatrix(Matrix4f.perspective(70f / 8f, 1.0F, 0.05F, this.clipDistance));

		} else {
			if (this.zoom != 1.0F) {
				posestack.translate((double) this.zoomX, (double) (-this.zoomY), 0.0D);
				posestack.scale(this.zoom, this.zoom, 1.0F);
			}
			posestack.mulPoseMatrix(Matrix4f.perspective(d, (float) this.minecraft.getWindow().getScreenWidth()
					/ (float) this.minecraft.getWindow().getScreenHeight(), 0.05F, this.clipDistance));
		}
		info.setReturnValue(posestack.last().pose());
	}

	// TODO optifine?
//	public void initFrame() {
//		if (this.minecraft.currentPass == RenderPass.LEFT) {
//			this.frameInit();
//
//			if (!this.always_true && !this.minecraft.isWindowActive() && this.minecraft.options.pauseOnLostFocus
//					&& (!this.minecraft.options.touchscreen || !this.minecraft.mouseHandler.isRightPressed())) {
//				if (Util.getMillis() - this.lastActiveTime > 500L) {
//					this.minecraft.pauseGame(false);
//				}
//			} else {
//				this.lastActiveTime = Util.getMillis();
//			}
//		}
//	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
	public boolean focus(Minecraft instance) {
		return true;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
	public void pause(Minecraft instance, boolean bl) {
		if (ClientDataHolder.getInstance().currentPass == RenderPass.LEFT ){
			instance.pauseGame(bl);
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
	public long active() {
		if (ClientDataHolder.getInstance().currentPass == RenderPass.LEFT) {
			return Util.getMillis();
		}
		return this.lastActiveTime;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
	public void matrix(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
		this.resetProjectionMatrix(this.getProjectionMatrix(minecraft.options.fov));
		RenderSystem.getModelViewStack().setIdentity();
		RenderSystem.applyModelViewMatrix();
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "render")
	public PoseStack newStack(PoseStack poseStack) {
		this.stack = poseStack;
		return poseStack;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
	public void renderoverlay(float f, long l, boolean bl, CallbackInfo ci) {
		if (GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.THIRD
				&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.CAMERA) {
			this.renderFaceOverlay(f, this.stack);
		}
	}

	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
	public boolean effect(GameRenderer instance) {
		return this.effectActive && ClientDataHolder.getInstance().currentPass != RenderPass.THIRD;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.BEFORE, ordinal = 6), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", cancellable = true)
	public void mainMenu(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
		if (renderWorldIn && this.minecraft.level != null) {

		}
		else {
			this.minecraft.getProfiler().push("MainMenu");
			GL11.glDisable(GL11.GL_STENCIL_TEST);

			PoseStack pMatrixStack = new PoseStack();
			applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, pMatrixStack);
			this.renderGuiLayer(partialTicks, true, pMatrixStack);

			if (KeyboardHandler.Showing) {
				if (GameRendererVRMixin.DATA_HOLDER.vrSettings.physicalKeyboard) {
					this.renderPhysicalKeyboard(partialTicks, pMatrixStack);
				} else {
					this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
							KeyboardHandler.Rotation_room, false, pMatrixStack);
				}
			}

			if ((GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.THIRD
					|| GameRendererVRMixin.DATA_HOLDER.vrSettings.mixedRealityRenderHands)
					&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.CAMERA) {
				this.renderVRHands(partialTicks, true, true, true, true, pMatrixStack);
			}
		}
		this.minecraft.getProfiler().pop();
		info.cancel();
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
	public void renderpick(GameRenderer g, float  pPartialTicks) {
		if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.LEFT) {
			this.pick(pPartialTicks);

			if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != HitResult.Type.MISS) {
				this.crossVec = this.minecraft.hitResult.getLocation();
			}

			if (this.minecraft.screen == null) {
				GameRendererVRMixin.DATA_HOLDER.teleportTracker.updateTeleportDestinations((GameRenderer)(Object)this, this.minecraft,
						this.minecraft.player);
			}
		}

		this.cacheRVEPos((LivingEntity) this.minecraft.getCameraEntity());
		this.setupRVE();
		this.setupOverlayStatus(pPartialTicks);
	}

	@Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
	public void removeBobHurt(PoseStack poseStack, float f, CallbackInfo ci) {
		ci.cancel();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"), method = "renderLevel")
	public void removeBobView(GameRenderer instance, PoseStack poseStack, float f) {
		return;
	}
	
	public void limiti() {
		
	}
	
	public void changeVariable() {

	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1), method = "renderLevel")
	public void noHandProfiler(ProfilerFiller instance, String s) {
		GL11.glDisable(GL11.GL_STENCIL_TEST);
		this.minecraft.getProfiler().popPush("ShadersEnd"); //TODO needed?
		return;
	}
	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
	public boolean noHands(GameRenderer instance) {
		return false;
	}

	@Inject(at = @At(value = "TAIL", shift = Shift.BEFORE), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
	public void restoreVE(float f, long j, PoseStack p, CallbackInfo i) {
		this.restoreRVEPos((LivingEntity) this.minecraft.getCameraEntity());
	}

	private void setupOverlayStatus(float partialTicks) {
		this.inBlock = 0.0F;
		this.inwater = false;
		this.onfire = false;

		if (!this.minecraft.player.isSpectator() && !this.isInMenuRoom() && this.minecraft.player.isAlive()) {
			Vec3 vec3 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass).getPosition();
			Triple<Float, BlockState, BlockPos> triple = ((ItemInHandRendererExtension) this.itemInHandRenderer).getNearOpaqueBlock(vec3, (double) this.minClipDistance);

			if (triple != null && !Xevents.renderBlockOverlay(this.minecraft.player, new PoseStack(), triple.getMiddle(), triple.getRight())) {
				this.inBlock = triple.getLeft();
			} else {
				this.inBlock = 0.0F;
			}

			this.inwater = this.minecraft.player.isEyeInFluid(FluidTags.WATER) && !Xevents.renderWaterOverlay(this.minecraft.player, new PoseStack());
			this.onfire = GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.THIRD
					&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.CAMERA && this.minecraft.player.isOnFire() && !Xevents.renderFireOverlay(this.minecraft.player, new PoseStack());
		}
	}

	@Override
	public void setupRVE() {
		if (this.cached) {
			VRData.VRDevicePose vrdata$vrdevicepose = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render
					.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass);
			Vec3 vec3 = vrdata$vrdevicepose.getPosition();
			LivingEntity livingentity = (LivingEntity) this.minecraft.getCameraEntity();
			livingentity.setPosRaw(vec3.x, vec3.y, vec3.z);
			livingentity.xOld = vec3.x;
			livingentity.yOld = vec3.y;
			livingentity.zOld = vec3.z;
			livingentity.xo = vec3.x;
			livingentity.yo = vec3.y;
			livingentity.zo = vec3.z;
			livingentity.setXRot(-vrdata$vrdevicepose.getPitch());
			livingentity.xRotO = livingentity.getXRot();
			livingentity.setYRot(vrdata$vrdevicepose.getYaw());
			livingentity.yHeadRot = livingentity.getYRot();
			livingentity.yHeadRotO = livingentity.getYRot();
			livingentity.eyeHeight = 0;
		}
	}

	@Override
	public void cacheRVEPos(LivingEntity e) {
		if (this.minecraft.getCameraEntity() != null) {
			if (!this.cached) {
				this.rveX = e.getX();
				this.rveY = e.getY();
				this.rveZ = e.getZ();
				this.rvelastX = e.xOld;
				this.rvelastY = e.yOld;
				this.rvelastZ = e.zOld;
				this.rveprevX = e.xo;
				this.rveprevY = e.yo;
				this.rveprevZ = e.zo;
				this.rveyaw = e.yHeadRot;
				this.rvepitch = e.getXRot();
				this.rvelastyaw = e.yHeadRotO;
				this.rvelastpitch = e.xRotO;
				this.rveHeight = e.getEyeHeight();
				this.cached = true;
			}
		}
	}

	void renderMainMenuHand(int c, float partialTicks, boolean depthAlways, PoseStack poseStack) {
		this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(mainCamera, partialTicks, false)));
		poseStack.pushPose();
		poseStack.setIdentity();
		RenderSystem.disableTexture();
		RenderSystem.enableDepthTest();
		applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, poseStack);
		SetupRenderingAtController(c, poseStack);

		if (this.minecraft.getOverlay() == null) {
			RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
		}

		Tesselator tesselator = Tesselator.getInstance();

		if (depthAlways && c == 0) {
			RenderSystem.depthFunc(519);
		} else {
			RenderSystem.depthFunc(515);
		}

		Vec3i vec3i = new Vec3i(64, 64, 64);
		byte b0 = -1;
		Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
		Vec3 vec31 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(c).getDirection();
		Vec3 vec32 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(c)
				.getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
		vec32 = new Vec3(0.0D, 1.0D, 0.0D);
		vec31 = new Vec3(0.0D, 0.0D, -1.0D);
		Vec3 vec33 = new Vec3(vec3.x - vec31.x * 0.18D, vec3.y - vec31.y * 0.18D, vec3.z - vec31.z * 0.18D);

		if (this.minecraft.level != null) {
			float f = (float) this.minecraft.level.getMaxLocalRawBrightness(
					new BlockPos(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition()));
			// int i = Config.isShaders() ? 8 : 4; TODO
			int i = 4;
			if (Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
				i = IrisHelper.ShaderLight();
			}

			if (f < (float) i) {
				f = (float) i;
			}

			float f1 = f / (float) this.minecraft.level.getMaxLightLevel();
			vec3i = new Vec3i((double) ((float) vec3i.getX() * f1), (double) ((float) vec3i.getY() * f1),
					(double) ((float) vec3i.getZ() * f1));
		}
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		this.renderBox(tesselator, vec3, vec33, -0.02F, 0.02F, -0.0125F, 0.0125F, vec32, vec3i, b0, poseStack);
		tesselator.getBuilder().end();
		BufferUploader.end(tesselator.getBuilder());
		RenderSystem.enableTexture();
		poseStack.popPose();
		RenderSystem.depthFunc(515);
	}

	private void renderVRHands(float partialTicks, boolean renderright, boolean renderleft, boolean menuhandright,
			boolean menuhandleft, PoseStack poseStack) {
		this.minecraft.getProfiler().push("hands");

		if (renderright) {
			this.minecraft.getItemRenderer();
			ClientDataHolder.ismainhand = true;

			if (menuhandright) {
				this.renderMainMenuHand(0, partialTicks, false, poseStack);
			} else {
				this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
				PoseStack posestack = new PoseStack();
				posestack.last().pose().setIdentity();
				this.applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, posestack);
				this.renderVRHand_Main(posestack, partialTicks);
			}

			this.minecraft.getItemRenderer();
			ClientDataHolder.ismainhand = false;
		}

		if (renderleft) {
			if (menuhandleft) {
				this.renderMainMenuHand(1, partialTicks, false, poseStack);
			} else {
				this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
				PoseStack posestack1 = new PoseStack();
				posestack1.last().pose().setIdentity();
				this.applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, posestack1);
				this.renderVRHand_Offhand(partialTicks, true, posestack1);
			}
		}

		this.minecraft.getProfiler().pop();
	}

	@Override
	public boolean isInWater() {
		return inwater;
	}

	@Override
	public boolean isInMenuRoom() {
		return this.minecraft.level == null || this.minecraft.screen instanceof WinScreen || ClientDataHolder.getInstance().integratedServerLaunchInProgress || this.minecraft.getOverlay() != null;
	}

	@Override
	public Vec3 getControllerRenderPos(int c) {
		ClientDataHolder dataholder = GameRendererVRMixin.DATA_HOLDER;
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
		Matrix4f modelView = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(currentPass)
				.getMatrix().transposed().toMCMatrix();
		poseStack.last().pose().multiply(modelView);
		poseStack.last().normal().mul(new Matrix3f(modelView));
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

		for (int i = 0; i < edges + 1; i++) {
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
			this.polyblendsrca = GlStateManager.BLEND.srcAlpha;
			this.polyblenddsta = GlStateManager.BLEND.dstAlpha;
			this.polyblendsrcrgb = GlStateManager.BLEND.srcRgb;
			this.polyblenddstrgb = GlStateManager.BLEND.dstRgb;
			this.polyblend = GL43C.glIsEnabled(GL11.GL_BLEND);
			this.polytex = true;
			this.polylight = false;
			this.polycull = true;
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
		((RenderTargetExtension) this.minecraft.getMainRenderTarget()).genMipMaps();
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


			GameRendererVRMixin.DATA_HOLDER.pumpkineffect = 0.0F;

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

				if (!ClientDataHolder.viewonly) {
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
					//this.minecraft.screen.render(posestack1, i, j, this.minecraft.getDeltaFrameTime());
//					}
					Xevents.drawScreen(this.minecraft.screen, posestack1, i, j, this.minecraft.getDeltaFrameTime());

					// Vivecraft
					((GuiExtension) this.minecraft.gui).drawMouseMenuQuad(i, j);
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

			// this.lightTexture.setAllowed(true);
			posestack.popPose();
			RenderSystem.applyModelViewMatrix();
		}

		if (this.minecraft.options.renderDebugCharts && !this.minecraft.options.hideGui) {
			((MinecraftExtension)this.minecraft).drawProfiler();
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
		((RenderTargetExtension) this.minecraft.getMainRenderTarget()).genMipMaps();
		this.minecraft.getMainRenderTarget().unbindRead();

	}

	private void renderVRHand_Main(PoseStack matrix, float partialTicks) {
		matrix.pushPose();
		this.SetupRenderingAtController(0, matrix);
		ItemStack itemstack = this.minecraft.player.getMainHandItem();
		ItemStack itemstack1 = null; // this.minecraft.physicalGuiManager.getHeldItemOverride();

		if (itemstack1 != null) {
			itemstack = itemstack1;
		}

		if (GameRendererVRMixin.DATA_HOLDER.climbTracker.isClimbeyClimb() && itemstack.getItem() != Items.SHEARS) {
			itemstack = itemstack1 == null ? this.minecraft.player.getOffhandItem() : itemstack1;
		}

		if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.MAIN_HAND)) {
			int i = 0;

			if (GameRendererVRMixin.DATA_HOLDER.vrSettings.reverseShootingEye) {
				i = 1;
			}

			ItemStack itemstack2 = this.minecraft.player.getProjectile(this.minecraft.player.getMainHandItem());

			if (itemstack2 != ItemStack.EMPTY && !GameRendererVRMixin.DATA_HOLDER.bowTracker.isNotched()) {
				itemstack = itemstack2;
			} else {
				itemstack = ItemStack.EMPTY;
			}
		} else if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.OFF_HAND)
				&& GameRendererVRMixin.DATA_HOLDER.bowTracker.isNotched()) {
			int j = 0;

			if (GameRendererVRMixin.DATA_HOLDER.vrSettings.reverseShootingEye) {
				j = 1;
			}

			itemstack = ItemStack.EMPTY;
		}

		boolean flag = false;

//		if (Config.isShaders()) { TODO
//			Shaders.beginHand(matrix, flag);
//		} else {
		matrix.pushPose();
//		}

		this.lightTexture.turnOnLightLayer();
		MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
		(this.itemInHandRenderer).renderArmWithItem(this.minecraft.player, partialTicks,
				0.0F, InteractionHand.MAIN_HAND, this.minecraft.player.getAttackAnim(partialTicks), itemstack, 0.0F,
				matrix, multibuffersource$buffersource,
				this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks));
		multibuffersource$buffersource.endBatch();
		this.lightTexture.turnOffLightLayer();

//		if (Config.isShaders()) { TODO
//			Shaders.endHand(matrix);
//		} else {
		matrix.popPose();
//		}

		matrix.popPose();
	}

	private void renderVRHand_Offhand(float partialTicks, boolean renderTeleport, PoseStack matrix) {
		// boolean flag = Config.isShaders();TODO
		boolean flag = false;
		boolean flag1 = false;

//		if (flag) {
//			flag1 = Shaders.isShadowPass;
//		}

		matrix.pushPose();
		this.SetupRenderingAtController(1, matrix);
		ItemStack itemstack = this.minecraft.player.getOffhandItem();
		ItemStack itemstack1 = null;// this.minecraft.physicalGuiManager.getOffhandOverride();

		if (itemstack1 != null) {
			itemstack = itemstack1;
		}

		if (GameRendererVRMixin.DATA_HOLDER.climbTracker.isClimbeyClimb()
				&& (itemstack == null || itemstack.getItem() != Items.SHEARS)) {
			itemstack = this.minecraft.player.getMainHandItem();
		}

		if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.MAIN_HAND)) {
			int i = 1;

			if (GameRendererVRMixin.DATA_HOLDER.vrSettings.reverseShootingEye) {
				i = 0;
			}

			itemstack = this.minecraft.player.getMainHandItem();
		}

		boolean flag2 = false;

//		if (Config.isShaders()) { TODO
//			Shaders.beginHand(matrix, flag2);
//		} else {
		matrix.pushPose();
//		}

		this.lightTexture.turnOnLightLayer();
		MultiBufferSource.BufferSource multibuffersource$buffersource = this.renderBuffers.bufferSource();
		this.itemInHandRenderer.renderArmWithItem(this.minecraft.player, partialTicks,
				0.0F, InteractionHand.OFF_HAND, this.minecraft.player.getAttackAnim(partialTicks), itemstack, 0.0F,
				matrix, multibuffersource$buffersource,
				this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks));
		multibuffersource$buffersource.endBatch();
		this.lightTexture.turnOffLightLayer();

//		if (Config.isShaders()) { TODO
//			Shaders.endHand(matrix);
//		} else {
		matrix.popPose();
//		}

		matrix.popPose();

		if (renderTeleport) {
			matrix.pushPose();
			matrix.setIdentity();
			this.applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, matrix);
//			net.optifine.shaders.Program program = Shaders.activeProgram; TODO

//			if (Config.isShaders()) {
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
//			}

			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.disableTexture();

			if (ClientNetworkHelper.isLimitedSurvivalTeleport() && !GameRendererVRMixin.DATA_HOLDER.vrPlayer.getFreeMove()
					&& this.minecraft.gameMode.hasMissTime()
					&& GameRendererVRMixin.DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming
					&& !GameRendererVRMixin.DATA_HOLDER.bowTracker.isActive(this.minecraft.player)) {
				matrix.pushPose();
				this.SetupRenderingAtController(1, matrix);
				Vec3 vec3 = new Vec3(0.0D, 0.005D, 0.03D);
				float f1 = 0.03F;
				float f;

				if (GameRendererVRMixin.DATA_HOLDER.teleportTracker.isAiming()) {
					f = 2.0F * (float) ((double) GameRendererVRMixin.DATA_HOLDER.teleportTracker.getTeleportEnergy()
							- 4.0D * GameRendererVRMixin.DATA_HOLDER.teleportTracker.movementTeleportDistance) / 100.0F * f1;
				} else {
					f = 2.0F * GameRendererVRMixin.DATA_HOLDER.teleportTracker.getTeleportEnergy() / 100.0F * f1;
				}

				if (f < 0.0F) {
					f = 0.0F;
				}
				RenderSystem.setShader(GameRenderer::getPositionColorShader);
				RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
				this.renderFlatQuad(vec3.add(0.0D, 0.05001D, 0.0D), f, f, 0.0F, this.tpLimitedColor.getX(),
						this.tpLimitedColor.getY(), this.tpLimitedColor.getZ(), 128, matrix);
				this.renderFlatQuad(vec3.add(0.0D, 0.05D, 0.0D), f1, f1, 0.0F, this.tpLimitedColor.getX(),
						this.tpLimitedColor.getY(), this.tpLimitedColor.getZ(), 50, matrix);
				matrix.popPose();
			}

			if (GameRendererVRMixin.DATA_HOLDER.teleportTracker.isAiming()) {
				RenderSystem.enableDepthTest();

				if (GameRendererVRMixin.DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming) {
					this.renderTeleportArc(GameRendererVRMixin.DATA_HOLDER.vrPlayer, matrix);
				}

			}

			RenderSystem.enableTexture();
			RenderSystem.defaultBlendFunc();

//			if (Config.isShaders()) {
//				Shaders.useProgram(program);
//			}

			matrix.popPose();
		}
	}

	void render2D(float par1, RenderTarget framebuffer, Vec3 pos, org.vivecraft.utils.math.Matrix4f rot,
			boolean depthAlways, PoseStack poseStack) {
		if (!GameRendererVRMixin.DATA_HOLDER.bowTracker.isDrawing) {
			boolean flag = this.isInMenuRoom();
			this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, par1, true)));
			poseStack.pushPose();
			poseStack.setIdentity();
			this.applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, poseStack);
			Vec3 vec3 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render
					.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass).getPosition();
			Vec3 vec31 = new Vec3(0.0D, 0.0D, 0.0D);
			float f = GuiHandler.guiScale;
			VRPlayer vrplayer = GameRendererVRMixin.DATA_HOLDER.vrPlayer;
			Vec3 guipos = VRPlayer.room_to_world_pos(pos, GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render);
			org.vivecraft.utils.math.Matrix4f matrix4f = org.vivecraft.utils.math.Matrix4f
					.rotationY(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.rotation_radians);
			org.vivecraft.utils.math.Matrix4f guirot = org.vivecraft.utils.math.Matrix4f.multiply(matrix4f, rot);

			poseStack.translate((float) (guipos.x - vec3.x), (float) (guipos.y - vec3.y), (float) (guipos.z - vec3.z));
			poseStack.mulPoseMatrix(guirot.toMCMatrix());
			poseStack.translate((float) vec31.x, (float) vec31.y, (float) vec31.z);
			float f1 = f * GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
			poseStack.scale(f1, f1, f1);

			framebuffer.bindRead();
			GlStateManager._disableCull();
			GlStateManager._enableTexture();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, framebuffer.getColorTextureId());

			float[] color = new float[] { 1, 1, 1, 1 };
			if (!flag) {
				if (this.minecraft.screen == null) {
					color[3] = GameRendererVRMixin.DATA_HOLDER.vrSettings.hudOpacity;
				}

				if (this.minecraft.player != null && this.minecraft.player.isShiftKeyDown()) {
					color[3] *= 0.75F;
				}

				GlStateManager._enableBlend();
				RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
						GlStateManager.DestFactor.ONE);
			} else {
				GlStateManager._disableBlend();
			}

			if (depthAlways) {
				GlStateManager._depthFunc(519);
			} else {
				GlStateManager._depthFunc(515);
			}

			GlStateManager._depthMask(true);
			GlStateManager._enableDepthTest();


			if (this.minecraft.level != null) {
				if (((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(vec3)) {
					vec3 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition();
				}

//				int i = Config.isShaders() ? 8 : 4; TODO
				int i = 4;
				if (Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
					i = IrisHelper.ShaderLight();
				}
				int j = Utils.getCombinedLightWithMin(this.minecraft.level, new BlockPos(vec3), i);
				this.drawSizedQuadWithLightmap((float) this.minecraft.getWindow().getGuiScaledWidth(),
						(float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, j, color,
						poseStack.last().pose());
			} else {
				this.drawSizedQuad((float) this.minecraft.getWindow().getGuiScaledWidth(),
						(float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, color, poseStack.last().pose());
			}

			RenderSystem.defaultBlendFunc();
			GlStateManager._depthFunc(515);
			GlStateManager._enableCull();

			poseStack.popPose();
		}
	}

	void renderPhysicalKeyboard(float partialTicks, PoseStack poseStack) {
		if (!GameRendererVRMixin.DATA_HOLDER.bowTracker.isDrawing) {
			this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
			poseStack.pushPose();
			poseStack.setIdentity();
			// RenderSystem.enableRescaleNormal();
			// Lighting.setupFor3DItems();

			this.minecraft.getProfiler().push("applyPhysicalKeyboardModelView");
			Vec3 vec3 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render
					.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass).getPosition();
			VRPlayer vrplayer = GameRendererVRMixin.DATA_HOLDER.vrPlayer;
			Vec3 guipos = VRPlayer.room_to_world_pos(KeyboardHandler.Pos_room,
					GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render);
			org.vivecraft.utils.math.Matrix4f matrix4f = org.vivecraft.utils.math.Matrix4f
					.rotationY(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.rotation_radians);
			org.vivecraft.utils.math.Matrix4f guirot = org.vivecraft.utils.math.Matrix4f.multiply(matrix4f,
					KeyboardHandler.Rotation_room);
			poseStack.mulPoseMatrix(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render
					.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass).getMatrix().transposed().toMCMatrix());
			poseStack.translate((float) (guipos.x - vec3.x), (float) (guipos.y - vec3.y), (float) (guipos.z - vec3.z));
			// GlStateManager._multMatrix(guirot.transposed().toFloatBuffer());
			poseStack.mulPoseMatrix(guirot.toMCMatrix());
			float f = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
			poseStack.scale(f, f, f);
			this.minecraft.getProfiler().pop();

			KeyboardHandler.physicalKeyboard.render(poseStack);
			// Lighting.turnOff();
			// RenderSystem.disableRescaleNormal();
			poseStack.popPose();
			RenderSystem.applyModelViewMatrix();
		}
	}

	private void renderGuiLayer(float par1, boolean depthAlways, PoseStack pMatrix) {
		if (!GameRendererVRMixin.DATA_HOLDER.bowTracker.isDrawing) {
			if (this.minecraft.screen != null || !this.minecraft.options.hideGui) {
				if (!RadialHandler.isShowing()) {
					boolean flag = this.isInMenuRoom();

					PoseStack poseStack = RenderSystem.getModelViewStack();
					poseStack.pushPose();
					poseStack.setIdentity();
					RenderSystem.applyModelViewMatrix();

					if (flag) {
						pMatrix.pushPose();
						Vec3 eye = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render
								.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass).getPosition();
						pMatrix.translate((GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.origin.x - eye.x),
								(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.origin.y - eye.y),
								(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.origin.z - eye.z));
						//System.out.println(eye + " eye");
						//System.out.println(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.origin + " world");

//						if (GameRendererVRMixin.DATA_HOLDER.menuWorldRenderer != null
//								&& GameRendererVRMixin.DATA_HOLDER.menuWorldRenderer.isReady()) {
//							try {
//								//this.renderTechjarsAwesomeMainMenuRoom();
//							} catch (Exception exception) {
//								System.out.println("Error rendering main menu world, unloading to prevent more errors");
//								exception.printStackTrace();
//								GameRendererVRMixin.DATA_HOLDER.menuWorldRenderer.destroy();
//							}
//						} else {
							this.renderJrbuddasAwesomeMainMenuRoomNew(pMatrix);
//						}
						pMatrix.popPose();
					}

					pMatrix.pushPose();
					Vec3 vec31 = GuiHandler.applyGUIModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, pMatrix);
					GuiHandler.guiFramebuffer.bindRead();
					RenderSystem.disableCull();
					RenderSystem.enableTexture();
					RenderSystem.setShaderTexture(0, GuiHandler.guiFramebuffer.getColorTextureId());

					float[] color = new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
					if (!flag) {
						if (this.minecraft.screen == null) {
							color[3] = GameRendererVRMixin.DATA_HOLDER.vrSettings.hudOpacity;
						}

						if (this.minecraft.player != null && this.minecraft.player.isShiftKeyDown()) {
							color[3] *= 0.75F;
						}

						RenderSystem.enableBlend();
						RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
								GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
								GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA, GlStateManager.DestFactor.ONE);
					} else {
						RenderSystem.disableBlend();
					}

					if (depthAlways) {
						RenderSystem.depthFunc(519);
					} else {
						RenderSystem.depthFunc(515);
					}

					RenderSystem.depthMask(true);
					RenderSystem.enableDepthTest();

					// RenderSystem.disableLighting();

					if (this.minecraft.level != null) {
						if (((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(vec31)) {
							vec31 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getPosition();
						}

//						int i = Config.isShaders() ? 8 : 4; TODO
						int i = 4;
						if (Xplat.isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
							i = IrisHelper.ShaderLight();
						}
						int j = Utils.getCombinedLightWithMin(this.minecraft.level, new BlockPos(vec31), i);
						this.drawSizedQuadWithLightmap((float) this.minecraft.getWindow().getGuiScaledWidth(),
								(float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, j, color,
								pMatrix.last().pose());
					} else {
						RenderSystem.setShader(GameRenderer::getPositionTexShader);
						this.drawSizedQuad((float) this.minecraft.getWindow().getGuiScaledWidth(),
								(float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, color,
								pMatrix.last().pose());
					}

					// RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
					RenderSystem.depthFunc(515);
					RenderSystem.enableDepthTest();
					// RenderSystem.defaultAlphaFunc();
					RenderSystem.defaultBlendFunc();
					RenderSystem.enableCull();
					pMatrix.popPose();

					poseStack.popPose();
					RenderSystem.applyModelViewMatrix();
				}
			}
		}
	}

	public void SetupRenderingAtController(int controller, PoseStack matrix) {
		Vec3 vec3 = this.getControllerRenderPos(controller);
		vec3 = vec3.subtract(GameRendererVRMixin.DATA_HOLDER.vrPlayer.getVRDataWorld()
				.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass).getPosition());
		matrix.translate((double) ((float) vec3.x), (double) ((float) vec3.y), (double) ((float) vec3.z));
		float sc = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale;
		if (minecraft.level != null && TelescopeTracker.isTelescope(minecraft.player.getUseItem())) {
			matrix.mulPoseMatrix(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.hmd.getMatrix().inverted()
					.transposed().toMCMatrix());
			MethodHolder.rotateDegXp(matrix, 90);
			matrix.translate(controller == 0 ? 0.075 * sc : -0.075 * sc, -0.025 * sc, 0.0325 * sc);
		} else {
			matrix.mulPoseMatrix(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(controller)
					.getMatrix().inverted().transposed().toMCMatrix());
		}

		matrix.scale(sc, sc, sc);

	}

	public void renderFlatQuad(Vec3 pos, float width, float height, float yaw, int r, int g, int b, int a,
			PoseStack poseStack) {
		Tesselator tesselator = Tesselator.getInstance();
		tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
		Vec3 vec3 = (new Vec3((double) (-width / 2.0F), 0.0D, (double) (height / 2.0F)))
				.yRot((float) Math.toRadians((double) (-yaw)));
		Vec3 vec31 = (new Vec3((double) (-width / 2.0F), 0.0D, (double) (-height / 2.0F)))
				.yRot((float) Math.toRadians((double) (-yaw)));
		Vec3 vec32 = (new Vec3((double) (width / 2.0F), 0.0D, (double) (-height / 2.0F)))
				.yRot((float) Math.toRadians((double) (-yaw)));
		Vec3 vec33 = (new Vec3((double) (width / 2.0F), 0.0D, (double) (height / 2.0F)))
				.yRot((float) Math.toRadians((double) (-yaw)));
		Matrix4f mat = poseStack.last().pose();
		tesselator.getBuilder().vertex(mat, (float) (pos.x + vec3.x), (float) pos.y, (float) (pos.z + vec3.z))
				.color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
		tesselator.getBuilder().vertex(mat, (float) (pos.x + vec31.x), (float) pos.y, (float) (pos.z + vec31.z))
				.color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
		tesselator.getBuilder().vertex(mat, (float) (pos.x + vec32.x), (float) pos.y, (float) (pos.z + vec32.z))
				.color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
		tesselator.getBuilder().vertex(mat, (float) (pos.x + vec33.x), (float) pos.y, (float) (pos.z + vec33.z))
				.color(r, g, b, a).normal(0.0F, 1.0F, 0.0F).endVertex();
		tesselator.end();

	}

	private void renderBox(Tesselator tes, Vec3 start, Vec3 end, float minX, float maxX, float minY, float maxY,
			Vec3 up, Vec3i color, byte alpha, PoseStack poseStack) {
		Vec3 vec3 = start.subtract(end).normalize();
		Vec3 vec31 = vec3.cross(up);
		up = vec31.cross(vec3);
		Vec3 vec32 = new Vec3(vec31.x * (double) minX, vec31.y * (double) minX, vec31.z * (double) minX);
		vec31 = vec31.scale((double) maxX);
		Vec3 vec33 = new Vec3(up.x * (double) minY, up.y * (double) minY, up.z * (double) minY);
		up = up.scale((double) maxY);
		org.vivecraft.utils.lwjgl.Vector3f vector3f = Utils.convertToVector3f(vec3);
		org.vivecraft.utils.lwjgl.Vector3f vector3f1 = Utils.convertToVector3f(up.normalize());
		org.vivecraft.utils.lwjgl.Vector3f vector3f2 = Utils.convertToVector3f(vec31.normalize());
		Vec3 vec34 = start.add(vec31.x + vec33.x, vec31.y + vec33.y, vec31.z + vec33.z);
		Vec3 vec35 = start.add(vec31.x + up.x, vec31.y + up.y, vec31.z + up.z);
		Vec3 vec36 = start.add(vec32.x + vec33.x, vec32.y + vec33.y, vec32.z + vec33.z);
		Vec3 vec37 = start.add(vec32.x + up.x, vec32.y + up.y, vec32.z + up.z);
		Vec3 vec38 = end.add(vec31.x + vec33.x, vec31.y + vec33.y, vec31.z + vec33.z);
		Vec3 vec39 = end.add(vec31.x + up.x, vec31.y + up.y, vec31.z + up.z);
		Vec3 vec310 = end.add(vec32.x + vec33.x, vec32.y + vec33.y, vec32.z + vec33.z);
		Vec3 vec311 = end.add(vec32.x + up.x, vec32.y + up.y, vec32.z + up.z);
		BufferBuilder bufferbuilder = tes.getBuilder();
		Matrix4f mat = poseStack.last().pose();
		bufferbuilder.vertex(mat, (float) vec34.x, (float) vec34.y, (float) vec34.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec36.x, (float) vec36.y, (float) vec36.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec37.x, (float) vec37.y, (float) vec37.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec35.x, (float) vec35.y, (float) vec35.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f.x, vector3f.y, vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec310.x, (float) vec310.y, (float) vec310.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec38.x, (float) vec38.y, (float) vec38.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec39.x, (float) vec39.y, (float) vec39.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec311.x, (float) vec311.y, (float) vec311.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f.x, -vector3f.y, -vector3f.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec38.x, (float) vec38.y, (float) vec38.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec34.x, (float) vec34.y, (float) vec34.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec35.x, (float) vec35.y, (float) vec35.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec39.x, (float) vec39.y, (float) vec39.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f2.x, vector3f2.y, vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec36.x, (float) vec36.y, (float) vec36.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec310.x, (float) vec310.y, (float) vec310.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec311.x, (float) vec311.y, (float) vec311.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec37.x, (float) vec37.y, (float) vec37.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f2.x, -vector3f2.y, -vector3f2.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec37.x, (float) vec37.y, (float) vec37.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec311.x, (float) vec311.y, (float) vec311.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec39.x, (float) vec39.y, (float) vec39.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec35.x, (float) vec35.y, (float) vec35.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(vector3f1.x, vector3f1.y, vector3f1.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec310.x, (float) vec310.y, (float) vec310.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec36.x, (float) vec36.y, (float) vec36.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec34.x, (float) vec34.y, (float) vec34.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
				.endVertex();
		bufferbuilder.vertex(mat, (float) vec38.x, (float) vec38.y, (float) vec38.z)
				.color(color.getX(), color.getY(), color.getZ(), alpha).normal(-vector3f1.x, -vector3f1.y, -vector3f1.z)
				.endVertex();
	}

	private void renderJrbuddasAwesomeMainMenuRoomNew(PoseStack pMatrixStack) {
		int i = 4;
		float f = 2.5F;
		float f1 = 1.3F;
		float[] afloat = GameRendererVRMixin.DATA_HOLDER.vr.getPlayAreaSize();
		if (afloat == null)
			afloat = new float[] { 2, 2 };

		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		RenderSystem.depthFunc(519);
		RenderSystem.depthMask(true); //TODO temp fix
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableTexture();
		RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		pMatrixStack.pushPose();
		float f2 = afloat[0] + f1;
		float f3 = afloat[1] + f1;
		pMatrixStack.translate(-f2 / 2.0F, 0.0F, -f3 / 2.0F);

		Matrix4f matrix4f = pMatrixStack.last().pose();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

		float a, b, c, d;
		a = b = c = d = 0.8f;

		bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(a, b, c, d).normal(0, 1, 0).endVertex();
		bufferbuilder.vertex(matrix4f, 0, 0, f3).uv(0, i * f3).color(a, b, c, d).normal(0, 1, 0).endVertex();
		bufferbuilder.vertex(matrix4f, f2, 0, f3).uv(i * f2, i * f3).color(a, b, c, d).normal(0, 1, 0).endVertex();
		bufferbuilder.vertex(matrix4f, f2, 0, 0).uv(i * f2, 0).color(a, b, c, d).normal(0, 1, 0).endVertex();

		bufferbuilder.vertex(matrix4f, 0, f, f3).uv(0, 0).color(a, b, c, d).normal(0, -1, 0).endVertex();
		bufferbuilder.vertex(matrix4f, 0, f, 0).uv(0, i * f3).color(a, b, c, d).normal(0, -1, 0).endVertex();
		bufferbuilder.vertex(matrix4f, f2, f, 0).uv(i * f2, i * f3).color(a, b, c, d).normal(0, -1, 0).endVertex();
		bufferbuilder.vertex(matrix4f, f2, f, f3).uv(i * f2, 0).color(a, b, c, d).normal(0, -1, 0).endVertex();

		bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(a, b, c, d).normal(1, 0, 0).endVertex();
		bufferbuilder.vertex(matrix4f, 0, f, 0).uv(0, i * f).color(a, b, c, d).normal(1, 0, 0).endVertex();
		bufferbuilder.vertex(matrix4f, 0, f, f3).uv(i * f3, i * f).color(a, b, c, d).normal(1, 0, 0).endVertex();
		bufferbuilder.vertex(matrix4f, 0, 0, f3).uv(i * f3, 0).color(a, b, c, d).normal(1, 0, 0).endVertex();

		bufferbuilder.vertex(matrix4f, f2, 0, 0).uv(0, 0).color(a, b, c, d).normal(-1, 0, 0).endVertex();
		bufferbuilder.vertex(matrix4f, f2, 0, f3).uv(i * f3, 0).color(a, b, c, d).normal(-1, 0, 0).endVertex();
		bufferbuilder.vertex(matrix4f, f2, f, f3).uv(i * f3, i * f).color(a, b, c, d).normal(-1, 0, 0).endVertex();
		bufferbuilder.vertex(matrix4f, f2, f, 0).uv(0, i * f).color(a, b, c, d).normal(-1, 0, 0).endVertex();

		bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(a, b, c, d).normal(0, 0, 1).endVertex();
		bufferbuilder.vertex(matrix4f, f2, 0, 0).uv(i * f2, 0).color(a, b, c, d).normal(0, 0, 1).endVertex();
		bufferbuilder.vertex(matrix4f, f2, f, 0).uv(i * f2, i * f).color(a, b, c, d).normal(0, 0, 1).endVertex();
		bufferbuilder.vertex(matrix4f, 0, f, 0).uv(0, i * f).color(a, b, c, d).normal(0, 0, 1).endVertex();

		bufferbuilder.vertex(matrix4f, 0, 0, f3).uv(0, 0).color(a, b, c, d).normal(0, 0, -1).endVertex();
		bufferbuilder.vertex(matrix4f, 0, f, f3).uv(0, i * f).color(a, b, c, d).normal(0, 0, -1).endVertex();
		bufferbuilder.vertex(matrix4f, f2, f, f3).uv(i * f2, i * f).color(a, b, c, d).normal(0, 0, -1).endVertex();
		bufferbuilder.vertex(matrix4f, f2, 0, f3).uv(i * f2, 0).color(a, b, c, d).normal(0, 0, -1).endVertex();

		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		pMatrixStack.popPose();

	}

	public void renderVRFabulous(float partialTicks, LevelRenderer worldrendererin, boolean menuhandright,
			boolean menuhandleft, PoseStack pMatrix) {
		if (ClientDataHolder.getInstance().currentPass == RenderPass.SCOPEL || ClientDataHolder.getInstance().currentPass == RenderPass.SCOPER)
			return;
		this.minecraft.getProfiler().popPush("VR");
		this.renderCrosshairAtDepth(!ClientDataHolder.getInstance().vrSettings.useCrosshairOcclusion, pMatrix);
		this.minecraft.getMainRenderTarget().unbindWrite();
		((LevelRendererExtension)worldrendererin).getAlphaSortVROccludedFramebuffer().clear(Minecraft.ON_OSX);
		((LevelRendererExtension)worldrendererin).getAlphaSortVROccludedFramebuffer().copyDepthFrom(this.minecraft.getMainRenderTarget());
		((LevelRendererExtension)worldrendererin).getAlphaSortVROccludedFramebuffer().bindWrite(true);

		if (this.shouldOccludeGui()) {
			this.renderGuiLayer(partialTicks, false, pMatrix);
			this.renderVrShadow(partialTicks, false, pMatrix);

			if (KeyboardHandler.Showing) {
				if (DATA_HOLDER.vrSettings.physicalKeyboard) {
					this.renderPhysicalKeyboard(partialTicks, pMatrix);
				} else {
					this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
							KeyboardHandler.Rotation_room, false, pMatrix);
				}
			}

			if (RadialHandler.isShowing()) {
				this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
						RadialHandler.Rotation_room, false, pMatrix);
			}
		}

		((LevelRendererExtension)worldrendererin).getAlphaSortVRUnoccludedFramebuffer().clear(Minecraft.ON_OSX);
		((LevelRendererExtension)worldrendererin).getAlphaSortVRUnoccludedFramebuffer().bindWrite(true);

		if (!this.shouldOccludeGui()) {
			this.renderGuiLayer(partialTicks, false, pMatrix);
			this.renderVrShadow(partialTicks, false, pMatrix);

			if (KeyboardHandler.Showing) {
				if (DATA_HOLDER.vrSettings.physicalKeyboard) {
					this.renderPhysicalKeyboard(partialTicks, pMatrix);
				} else {
					this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
							KeyboardHandler.Rotation_room, false, pMatrix);
				}
			}

			if (RadialHandler.isShowing()) {
				this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
						RadialHandler.Rotation_room, false, pMatrix);
			}
		}

		this.renderVRSelfEffects(partialTicks);
		VRWidgetHelper.renderVRThirdPersonCamWidget();
		VRWidgetHelper.renderVRHandheldCameraWidget();
		boolean flag = this.shouldRenderHands();
		this.renderVRHands(partialTicks, flag && menuhandright, flag && menuhandleft, true, true, pMatrix);
		((LevelRendererExtension)worldrendererin).getAlphaSortVRHandsFramebuffer().clear(Minecraft.ON_OSX);
		((LevelRendererExtension)worldrendererin).getAlphaSortVRHandsFramebuffer().copyDepthFrom(this.minecraft.getMainRenderTarget());
		((LevelRendererExtension)worldrendererin).getAlphaSortVRHandsFramebuffer().bindWrite(true);
		this.renderVRHands(partialTicks, flag && !menuhandright, flag && !menuhandleft, false, false, pMatrix);
		RenderSystem.enableTexture();
		RenderSystem.defaultBlendFunc();
		// RenderSystem.defaultAlphaFunc();
		RenderSystem.setShaderColor(1,1,1,1);
		// Lighting.turnBackOn();
		// Lighting.turnOff();
		this.minecraft.getMainRenderTarget().bindWrite(true);
	}

	@Override
	public void renderVrFast(float partialTicks, boolean secondpass, boolean menuright, boolean menuleft,
			PoseStack pMatrix) {
		if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.SCOPEL
				|| GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.SCOPER)
			return;
		this.minecraft.getProfiler().popPush("VR");
		this.lightTexture.turnOffLightLayer();

		if (secondpass) {
			this.renderVrShadow(partialTicks, !this.shouldOccludeGui(), pMatrix);
		}

		if (!secondpass) {
			this.renderCrosshairAtDepth(!GameRendererVRMixin.DATA_HOLDER.vrSettings.useCrosshairOcclusion, pMatrix);
		}

		if (!secondpass) {
			VRWidgetHelper.renderVRThirdPersonCamWidget();
		}

		if (!secondpass) {
			VRWidgetHelper.renderVRHandheldCameraWidget();
		}

		if (secondpass) {
			this.renderGuiLayer(partialTicks, !this.shouldOccludeGui(), pMatrix);
		}

		if (secondpass && KeyboardHandler.Showing) {
			if (GameRendererVRMixin.DATA_HOLDER.vrSettings.physicalKeyboard) {
				this.renderPhysicalKeyboard(partialTicks, pMatrix);
			} else {
				this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
						KeyboardHandler.Rotation_room, !this.shouldOccludeGui(), pMatrix);
			}
		}

		if (secondpass && RadialHandler.isShowing()) {
			this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room,
					!this.shouldOccludeGui(), pMatrix);
		}

		this.renderVRHands(partialTicks, this.shouldRenderHands(), this.shouldRenderHands(), menuright, menuleft,
				pMatrix);
		this.renderVRSelfEffects(partialTicks);
	}

	public void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color) {
		float f = displayHeight / displayWidth;
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
		bufferbuilder.vertex((double) (-(size / 2.0F)), (double) (-(size * f) / 2.0F), 0.0D).uv(0.0F, 0.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex((double) (size / 2.0F), (double) (-(size * f) / 2.0F), 0.0D).uv(1.0F, 0.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex((double) (size / 2.0F), (double) (size * f / 2.0F), 0.0D).uv(1.0F, 1.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex((double) (-(size / 2.0F)), (double) (size * f / 2.0F), 0.0D).uv(0.0F, 1.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
	}

	public void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
		float f = displayHeight / displayWidth;
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
		bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * f) / 2.0F), 0).uv(0.0F, 0.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * f) / 2.0F), 0).uv(1.0F, 0.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * f / 2.0F), 0).uv(1.0F, 1.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * f / 2.0F), 0).uv(0.0F, 1.0F)
				.color(color[0], color[1], color[2], color[3]).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
	}

	public void drawSizedQuadSolid(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
		RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
		this.lightTexture.turnOnLightLayer();
		float f = displayHeight / displayWidth;
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);
		int light = LightTexture.pack(15, 15);
		bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(0.0F, 0.0F).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(1.0F, 0.0F).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(1.0F, 1.0F).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(0.0F, 1.0F).uv2(light).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		this.lightTexture.turnOffLightLayer();
	}


    public void drawSizedQuad(float displayWidth, float displayHeight, float size) {
        this.drawSizedQuad(displayWidth, displayHeight, size, new float[] { 1, 1, 1, 1 });
    }

	public void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
			float[] color, Matrix4f pMatrix) {
		RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
		float f = displayHeight / displayWidth;
		this.lightTexture.turnOnLightLayer();
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);
		bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(0.0F, 0.0F).uv2(lighti).normal(0,0,1).endVertex();
		bufferbuilder.vertex(pMatrix, (size / 2.0F), (-(size * f) / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(1.0F, 0.0F).uv2(lighti).normal(0,0,1).endVertex();
		bufferbuilder.vertex(pMatrix, (size / 2.0F), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(1.0F, 1.0F).uv2(lighti).normal(0,0,1).endVertex();
		bufferbuilder.vertex(pMatrix, (-(size / 2.0F)), (size * f / 2.0F), 0).color(color[0], color[1], color[2], color[3])
				.uv(0.0F, 1.0F).uv2(lighti).normal(0,0,1).endVertex();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		this.lightTexture.turnOffLightLayer();
	}

	public void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
			Matrix4f pMatrix) {
		this.drawSizedQuadWithLightmap(displayWidth, displayHeight, size, lighti, new float[] { 1, 1, 1, 1 }, pMatrix);
	}

	private void renderTeleportArc(VRPlayer vrPlayer, PoseStack poseStack) {
		if (GameRendererVRMixin.DATA_HOLDER.teleportTracker.vrMovementStyle.showBeam
				&& GameRendererVRMixin.DATA_HOLDER.teleportTracker.isAiming()
				&& GameRendererVRMixin.DATA_HOLDER.teleportTracker.movementTeleportArcSteps > 1) {
			this.minecraft.getProfiler().push("teleportArc");
			// boolean flag = Config.isShaders();
			boolean flag = false;
			RenderSystem.enableCull();
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
			Tesselator tesselator = Tesselator.getInstance();
			tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
			double d0 = GameRendererVRMixin.DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset;
			Vec3 vec3 = GameRendererVRMixin.DATA_HOLDER.teleportTracker.getDestination();
			boolean flag1 = vec3.x != 0.0D || vec3.y != 0.0D || vec3.z != 0.0D;
			byte b0 = -1;
			Vec3i vec3i;

			if (!flag1) {
				vec3i = new Vec3i(83, 75, 83);
				b0 = -128;
			} else {
				if (ClientNetworkHelper.isLimitedSurvivalTeleport() && !this.minecraft.player.getAbilities().mayfly) {
					vec3i = this.tpLimitedColor;
				} else {
					vec3i = this.tpUnlimitedColor;
				}

				d0 = GameRendererVRMixin.DATA_HOLDER.vrRenderer.getCurrentTimeSecs()
						* (double) GameRendererVRMixin.DATA_HOLDER.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
				GameRendererVRMixin.DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset = d0;
			}

			float f = GameRendererVRMixin.DATA_HOLDER.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
			int i = GameRendererVRMixin.DATA_HOLDER.teleportTracker.movementTeleportArcSteps - 1;

			if (GameRendererVRMixin.DATA_HOLDER.teleportTracker.vrMovementStyle.beamGrow) {
				i = (int) ((double) i * GameRendererVRMixin.DATA_HOLDER.teleportTracker.movementTeleportProgress);
			}

			double d1 = 1.0D / (double) i;
			Vec3 vec31 = new Vec3(0.0D, 1.0D, 0.0D);

			for (int j = 0; j < i; ++j) {
				double d2 = (double) j / (double) i + d0 * d1;
				int k = Mth.floor(d2);
				d2 = d2 - (double) ((float) k);
				Vec3 vec32 = GameRendererVRMixin.DATA_HOLDER.teleportTracker
						.getInterpolatedArcPosition((float) (d2 - d1 * (double) 0.4F))
						.subtract(this.minecraft.getCameraEntity().position());
				Vec3 vec33 = GameRendererVRMixin.DATA_HOLDER.teleportTracker.getInterpolatedArcPosition((float) d2)
						.subtract(this.minecraft.getCameraEntity().position());
				float f2 = (float) d2 * 2.0F;
				this.renderBox(tesselator, vec32, vec33, -f, f, (-1.0F + f2) * f, (1.0F + f2) * f, vec31, vec3i, b0,
						poseStack);
			}

			tesselator.end();
			RenderSystem.disableCull();

			if (flag1 && GameRendererVRMixin.DATA_HOLDER.teleportTracker.movementTeleportProgress >= 1.0D) {
				Vec3 vec34 = (new Vec3(vec3.x, vec3.y, vec3.z)).subtract(this.minecraft.getCameraEntity().position());
				int l = 1;
				float f1 = 0.01F;
				double d4 = 0.0D;
				double d5 = 0.0D;
				double d3 = 0.0D;

				if (l == 0) {
					d5 -= (double) f1;
				}

				if (l == 1) {
					d5 += (double) f1;
				}

				if (l == 2) {
					d3 -= (double) f1;
				}

				if (l == 3) {
					d3 += (double) f1;
				}

				if (l == 4) {
					d4 -= (double) f1;
				}

				if (l == 5) {
					d4 += (double) f1;
				}

				this.renderFlatQuad(vec34.add(d4, d5, d3), 0.6F, 0.6F, 0.0F, (int) ((double) vec3i.getX() * 1.03D),
						(int) ((double) vec3i.getY() * 1.03D), (int) ((double) vec3i.getZ() * 1.03D), 64, poseStack);

				if (l == 0) {
					d5 -= (double) f1;
				}

				if (l == 1) {
					d5 += (double) f1;
				}

				if (l == 2) {
					d3 -= (double) f1;
				}

				if (l == 3) {
					d3 += (double) f1;
				}

				if (l == 4) {
					d4 -= (double) f1;
				}

				if (l == 5) {
					d4 += (double) f1;
				}

				this.renderFlatQuad(vec34.add(d4, d5, d3), 0.4F, 0.4F, 0.0F, (int) ((double) vec3i.getX() * 1.04D),
						(int) ((double) vec3i.getY() * 1.04D), (int) ((double) vec3i.getZ() * 1.04D), 64, poseStack);

				if (l == 0) {
					d5 -= (double) f1;
				}

				if (l == 1) {
					d5 += (double) f1;
				}

				if (l == 2) {
					d3 -= (double) f1;
				}

				if (l == 3) {
					d3 += (double) f1;
				}

				if (l == 4) {
					d4 -= (double) f1;
				}

				if (l == 5) {
					d4 += (double) f1;
				}

				this.renderFlatQuad(vec34.add(d4, d5, d3), 0.2F, 0.2F, 0.0F, (int) ((double) vec3i.getX() * 1.05D),
						(int) ((double) vec3i.getY() * 1.05D), (int) ((double) vec3i.getZ() * 1.05D), 64, poseStack);
			}

			this.minecraft.getProfiler().pop();
			RenderSystem.enableCull();
		}
	}

	@Override
	public void drawEyeStencil(boolean flag1) {

		if (GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.SCOPEL
				&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.SCOPER) {
			if ((GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.LEFT
					|| GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.RIGHT)
					&& GameRendererVRMixin.DATA_HOLDER.vrSettings.vrUseStencil) {
//				net.optifine.shaders.Program program = Shaders.activeProgram;
//
//				if (shaders && Shaders.dfb != null) {
//					Shaders.dfb.bindFramebuffer();
//					Shaders.useProgram(Shaders.ProgramNone);
//
//					for (int i = 0; i < Shaders.usedDepthBuffers; ++i) {
//						GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(i));
//						this.minecraft.vrRenderer.doStencil(false);
//					}
//
//					Shaders.useProgram(program);
//				} else {
				GameRendererVRMixin.DATA_HOLDER.vrRenderer.doStencil(false);
//				}
			} else {
				GL11.glDisable(GL11.GL_STENCIL_TEST);
			}
		} else {
			// No stencil for telescope
			// GameRendererVRMixin.DATA_HOLDER.vrRenderer.doStencil(true);
		}
	}

	private void renderFaceOverlay(float par1, PoseStack pMatrix) {
//		boolean flag = Config.isShaders();
		boolean flag = false;

//		if (flag) { TODO
//			Shaders.beginFPOverlay();
//		}

		if (this.inBlock > 0.0F) {
			this.renderFaceInBlock();
			this.renderGuiLayer(par1, true, pMatrix);

			if (KeyboardHandler.Showing) {
				if (GameRendererVRMixin.DATA_HOLDER.vrSettings.physicalKeyboard) {
					this.renderPhysicalKeyboard(par1, pMatrix);
				} else {
					this.render2D(par1, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
							KeyboardHandler.Rotation_room, true, pMatrix);
				}
			}

			if (RadialHandler.isShowing()) {
				this.render2D(par1, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room,
						true, pMatrix);
			}

			if (this.inBlock >= 1.0F) {
				this.renderVRHands(par1, true, true, true, true, pMatrix);
			}
		}

//		if (flag) { TODO
//			Shaders.endFPOverlay();
//		}
	}

	private void renderFaceInBlock() {
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, ((GameRendererExtension) this.minecraft.gameRenderer).inBlock());

		// orthographic matrix, (-1, -1) to (1, 1), near = 0.0, far 2.0
		Matrix4f mat = new Matrix4f();
		mat.m00 = 1.0F;
		mat.m11 = 1.0F;
		mat.m22 = -1.0F;
		mat.m33 = 1.0F;
		mat.m23 = -1.0F;

		GlStateManager._disableDepthTest();
		GlStateManager._disableTexture();
		GlStateManager._enableBlend();
		GlStateManager._disableCull();
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
		bufferbuilder.vertex(mat, -1.5F, -1.5F, 0.0F).endVertex();
		bufferbuilder.vertex(mat, 1.5F, -1.5F, 0.0F).endVertex();
		bufferbuilder.vertex(mat, 1.5F, 1.5F, 0.0F).endVertex();
		bufferbuilder.vertex(mat, -1.5F, 1.5F, 0.0F).endVertex();
		tesselator.end();
		GlStateManager._enableTexture();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public boolean shouldRenderCrosshair() {
		if (ClientDataHolder.viewonly) {
			return false;
		} else if (this.minecraft.level == null) {
			return false;
		} else if (this.minecraft.screen != null) {
			return false;
		} else {
			boolean flag = GameRendererVRMixin.DATA_HOLDER.vrSettings.renderInGameCrosshairMode == VRSettings.RenderPointerElement.ALWAYS
					|| (GameRendererVRMixin.DATA_HOLDER.vrSettings.renderInGameCrosshairMode == VRSettings.RenderPointerElement.WITH_HUD
							&& !this.minecraft.options.hideGui);

			if (!flag) {
				return false;
			} else if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.THIRD) {
				return false;
			} else if (GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.SCOPEL
					&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.SCOPER) {
				if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.CAMERA) {
					return false;
				} else if (KeyboardHandler.Showing) {
					return false;
				} else if (RadialHandler.isUsingController(ControllerType.RIGHT)) {
					return false;
				} else if (GameRendererVRMixin.DATA_HOLDER.bowTracker.isNotched()) {
					return false;
				} else if (!GameRendererVRMixin.DATA_HOLDER.vr.getInputAction(GameRendererVRMixin.DATA_HOLDER.vr.keyVRInteract)
						.isEnabledRaw(ControllerType.RIGHT)
						&& !GameRendererVRMixin.DATA_HOLDER.vr.keyVRInteract.isDown(ControllerType.RIGHT)) {
					if (!GameRendererVRMixin.DATA_HOLDER.vr.getInputAction(GameRendererVRMixin.DATA_HOLDER.vr.keyClimbeyGrab)
							.isEnabledRaw(ControllerType.RIGHT)
							&& !GameRendererVRMixin.DATA_HOLDER.vr.keyClimbeyGrab.isDown(ControllerType.RIGHT)) {
						if (GameRendererVRMixin.DATA_HOLDER.teleportTracker.isAiming()) {
							return false;
						} else if (GameRendererVRMixin.DATA_HOLDER.climbTracker.isGrabbingLadder(0)) {
							return false;
						} else {
							return !(GameRendererVRMixin.DATA_HOLDER.vrPlayer.worldScale > 15.0F);
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
	}

	private void renderCrosshairAtDepth(boolean depthAlways, PoseStack poseStack) {
		if (this.shouldRenderCrosshair()) {
			this.minecraft.getProfiler().popPush("crosshair");
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			Vec3 vec3 = this.crossVec;
			Vec3 vec31 = vec3.subtract(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getPosition());
			float f = (float) vec31.length();
			float f1 = (float) ((double) (0.125F * GameRendererVRMixin.DATA_HOLDER.vrSettings.crosshairScale)
					* Math.sqrt((double) GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.worldScale));
			vec3 = vec3.add(vec31.normalize().scale(-0.01D));
			poseStack.pushPose();
			poseStack.setIdentity();
			applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, poseStack);

			Vec3 vec32 = vec3.subtract(this.minecraft.getCameraEntity().position());
			poseStack.translate(vec32.x, vec32.y, vec32.z);

			if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
				BlockHitResult blockhitresult = (BlockHitResult) this.minecraft.hitResult;

				if (blockhitresult.getDirection() == Direction.DOWN) {
					MethodHolder.rotateDeg(poseStack,
							GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F, 1.0F,
							0.0F);
					MethodHolder.rotateDeg(poseStack, -90.0F, 1.0F, 0.0F, 0.0F);
				} else if (blockhitresult.getDirection() == Direction.EAST) {
					MethodHolder.rotateDeg(poseStack, 90.0F, 0.0F, 1.0F, 0.0F);
				} else if (blockhitresult.getDirection() != Direction.NORTH
						&& blockhitresult.getDirection() != Direction.SOUTH) {
					if (blockhitresult.getDirection() == Direction.UP) {
						MethodHolder.rotateDeg(poseStack,
								-GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F,
								1.0F, 0.0F);
						MethodHolder.rotateDeg(poseStack, -90.0F, 1.0F, 0.0F, 0.0F);
					} else if (blockhitresult.getDirection() == Direction.WEST) {
						MethodHolder.rotateDeg(poseStack, 90.0F, 0.0F, 1.0F, 0.0F);
					}
				}
			} else {
				MethodHolder.rotateDeg(poseStack,
						-GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getYaw(), 0.0F, 1.0F,
						0.0F);
				MethodHolder.rotateDeg(poseStack,
						-GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getController(0).getPitch(), 1.0F, 0.0F,
						0.0F);
			}

			if (GameRendererVRMixin.DATA_HOLDER.vrSettings.crosshairScalesWithDistance) {
				float f5 = 0.3F + 0.2F * f;
				f1 *= f5;
			}

			this.lightTexture.turnOnLightLayer();
			poseStack.scale(f1, f1, f1);
			RenderSystem.depthMask(true);
			RenderSystem.enableDepthTest();
			// RenderSystem.disableLighting();
			RenderSystem.disableCull();

			if (depthAlways) {
				RenderSystem.depthFunc(519);
			} else {
				RenderSystem.depthFunc(515);
			}

			// boolean flag = Config.isShaders();
			boolean flag = false;
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
					GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			int i = LevelRenderer.getLightColor(this.minecraft.level, new BlockPos(vec3));
			float f2 = 1.0F;

			if (this.minecraft.hitResult == null || this.minecraft.hitResult.getType() == HitResult.Type.MISS) {
				f2 = 0.5F;
			}

			RenderSystem.setShaderTexture(0, Screen.GUI_ICONS_LOCATION);
			float f3 = 0.00390625F;
			float f4 = 0.00390625F;

			BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

			RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
			bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);

			bufferbuilder.vertex(poseStack.last().pose(), -1.0F, 1.0F, 0.0F).color(f2, f2, f2, 1.0F)
					.uv(0.0F, 15.0F * f4).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();
			bufferbuilder.vertex(poseStack.last().pose(), 1.0F, 1.0F, 0.0F).color(f2, f2, f2, 1.0F)
					.uv(15.0F * f3, 15.0F * f4).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();
			bufferbuilder.vertex(poseStack.last().pose(), 1.0F, -1.0F, 0.0F).color(f2, f2, f2, 1.0F)
					.uv(15.0F * f3, 0.0F).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();
			bufferbuilder.vertex(poseStack.last().pose(), -1.0F, -1.0F, 0.0F).color(f2, f2, f2, 1.0F)
					.uv(0.0F, 0.0F).uv2(i).normal(0.0F, 0.0F, 1.0F).endVertex();

			bufferbuilder.end();
			BufferUploader.end(bufferbuilder);
  			RenderSystem.defaultBlendFunc();
			RenderSystem.disableBlend();
			RenderSystem.enableCull();
			RenderSystem.depthFunc(515);
			poseStack.popPose();
		}
	}

	public boolean shouldOccludeGui() {
		Vec3 vec3 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass)
				.getPosition();

		if (GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.THIRD
				&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.CAMERA) {
			return !this.isInMenuRoom() && this.minecraft.screen == null && !KeyboardHandler.Showing
					&& !RadialHandler.isShowing() && GameRendererVRMixin.DATA_HOLDER.vrSettings.hudOcclusion
					&& !((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(vec3);
		} else {
			return true;
		}
	}

	private void renderVrShadow(float par1, boolean depthAlways, PoseStack poseStack) {
		if (GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.THIRD
				&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.CAMERA) {
			if (this.minecraft.player.isAlive()) {
				if (!(((PlayerExtension) this.minecraft.player).getRoomYOffsetFromPose() < 0.0D)) {
					if (this.minecraft.player.getVehicle() == null) {
						AABB aabb = this.minecraft.player.getBoundingBox();

						if (GameRendererVRMixin.DATA_HOLDER.vrSettings.vrShowBlueCircleBuddy && aabb != null) {

							poseStack.pushPose();
							poseStack.setIdentity();
							GlStateManager._disableCull();
							this.applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, poseStack);
							Vec3 vec3 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render
									.getEye(GameRendererVRMixin.DATA_HOLDER.currentPass).getPosition();
							LocalPlayer localplayer = this.minecraft.player;
							Vec3 vec31 = new Vec3(this.rvelastX + (this.rveX - this.rvelastX) * (double) par1,
									this.rvelastY + (this.rveY - this.rvelastY) * (double) par1,
									this.rvelastZ + (this.rveZ - this.rvelastZ) * (double) par1);
							Vec3 vec32 = vec31.subtract(vec3).add(0.0D, 0.005D, 0.0D);
							this.setupPolyRendering(true);
							RenderSystem.enableDepthTest();

							if (depthAlways) {
								RenderSystem.depthFunc(519);
							} else {
								GlStateManager._depthFunc(515);
							}

							RenderSystem.setShader(GameRenderer::getPositionColorShader);
							RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
							this.renderFlatQuad(vec32, (float) (aabb.maxX - aabb.minX), (float) (aabb.maxZ - aabb.minZ),
									0.0F, 0, 0, 0, 64, poseStack);
							RenderSystem.depthFunc(515);
							this.setupPolyRendering(false);
							poseStack.popPose();
							GlStateManager._enableCull();
						}
					}
				}
			}
		}
	}

	public boolean shouldRenderHands() {
		if (GameRendererVRMixin.DATA_HOLDER.viewonly) {
			return false;
		} else if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.THIRD) {
			return GameRendererVRMixin.DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY;
		} else {
			return GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.CAMERA;
		}
	}

	private void renderVRSelfEffects(float par1) {
		if (this.onfire && GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.THIRD
				&& GameRendererVRMixin.DATA_HOLDER.currentPass != RenderPass.CAMERA) {

			if (this.onfire) {
				this.renderFireInFirstPerson();
			}

			this.renderItemActivationAnimation(0, 0, par1);
		}
	}

	private void renderFireInFirstPerson() {
		PoseStack posestack = new PoseStack();
		this.applyVRModelView(GameRendererVRMixin.DATA_HOLDER.currentPass, posestack);
		this.applystereo(GameRendererVRMixin.DATA_HOLDER.currentPass, posestack);
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		RenderSystem.depthFunc(519);

		if (GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.THIRD
				|| GameRendererVRMixin.DATA_HOLDER.currentPass == RenderPass.CAMERA) {
			GlStateManager._depthFunc(515);
		}

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_1.sprite();
		RenderSystem.enableDepthTest();

//		if (SmartAnimations.isActive()) { TODO
//			SmartAnimations.spriteRendered(textureatlassprite);
//		}

		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
		float f = textureatlassprite.getU0();
		float f1 = textureatlassprite.getU1();
		float f2 = (f + f1) / 2.0F;
		float f3 = textureatlassprite.getV0();
		float f4 = textureatlassprite.getV1();
		float f5 = (f3 + f4) / 2.0F;
		float f6 = textureatlassprite.uvShrinkRatio();
		float f7 = Mth.lerp(f6, f, f2);
		float f8 = Mth.lerp(f6, f1, f2);
		float f9 = Mth.lerp(f6, f3, f5);
		float f10 = Mth.lerp(f6, f4, f5);
		float f11 = 1.0F;
		float f12 = 0.3F;
		float f13 = (float) (GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getHeadPivot().y
				- ((GameRendererExtension) this.minecraft.gameRenderer).getRveY());

		for (int i = 0; i < 4; ++i) {
			posestack.pushPose();
			posestack.mulPose(Vector3f.YP.rotationDegrees(
					(float) i * 90.0F - GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getBodyYaw()));
			posestack.translate(0.0D, (double) (-f13), 0.0D);
			Matrix4f matrix4f = posestack.last().pose();
			bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
			bufferbuilder.vertex(matrix4f, -f12, 0.0F, -f12).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f10).endVertex();
			bufferbuilder.vertex(matrix4f, f12, 0.0F, -f12).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f10).endVertex();
			bufferbuilder.vertex(matrix4f, f12, f13, -f12).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f7, f9).endVertex();
			bufferbuilder.vertex(matrix4f, -f12, f13, -f12).color(1.0F, 1.0F, 1.0F, 0.9F).uv(f8, f9).endVertex();
			bufferbuilder.end();
			BufferUploader.end(bufferbuilder);
			posestack.popPose();
		}

		RenderSystem.depthFunc(515);
		RenderSystem.disableBlend();
	}

	public void applystereo(RenderPass currentPass, PoseStack matrix) {
		if (currentPass == RenderPass.LEFT || currentPass == RenderPass.RIGHT) {
			Vec3 vec3 = GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(currentPass).getPosition()
					.subtract(GameRendererVRMixin.DATA_HOLDER.vrPlayer.vrdata_world_render.getEye(RenderPass.CENTER)
							.getPosition());
			matrix.translate((double) ((float) (-vec3.x)), (double) ((float) (-vec3.y)), (double) ((float) (-vec3.z)));
		}
	}

	@Override
	public void restoreRVEPos(LivingEntity e) {
		if (e != null) {
			e.setPosRaw(this.rveX, this.rveY, this.rveZ);
			e.xOld = this.rvelastX;
			e.yOld = this.rvelastY;
			e.zOld = this.rvelastZ;
			e.xo = this.rveprevX;
			e.yo = this.rveprevY;
			e.zo = this.rveprevZ;
			e.setYRot(this.rveyaw);
			e.setXRot(this.rvepitch);
			e.yRotO = this.rvelastyaw;
			e.xRotO = this.rvelastpitch;
			e.yHeadRot = this.rveyaw;
			e.yHeadRotO = this.rvelastyaw;
			e.eyeHeight = this.rveHeight;
			this.cached = false;
		}
	}

	@Override
	public void DrawScopeFB(PoseStack matrixStackIn, int i) {
		if (ClientDataHolder.getInstance().currentPass != RenderPass.SCOPEL && ClientDataHolder.getInstance().currentPass != RenderPass.SCOPER) {
			//this.lightTexture.turnOffLightLayer();
			matrixStackIn.pushPose();
			RenderSystem.enableDepthTest();
			RenderSystem.enableTexture();

			if (i == 0) {
				ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferR.bindRead();
				RenderSystem.setShaderTexture(0, ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferR.getColorTextureId());
			}
			else {
				ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferL.bindRead();
				RenderSystem.setShaderTexture(0, ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferL.getColorTextureId());
			}

			float scale = 0.0785F;
			//actual framebuffer
			float f = TelescopeTracker.viewPercent(i);
			// this.drawSizedQuad(720.0F, 720.0F, scale, new float[]{f, f, f, 1}, matrixStackIn.last().pose());
			this.drawSizedQuadSolid(720.0F, 720.0F, scale, new float[]{f, f, f, 1}, matrixStackIn.last().pose());

			RenderSystem.setShaderTexture(0, new ResourceLocation("textures/misc/spyglass_scope.png"));
			RenderSystem.enableBlend();
			matrixStackIn.translate(0.0D, 0.0D, 0.00001D);
			int light = LevelRenderer.getLightColor(this.minecraft.level, new BlockPos(ClientDataHolder.getInstance().vrPlayer.vrdata_world_render.getController(i).getPosition()));
			this.drawSizedQuadWithLightmap(720.0F, 720.0F, scale, light, matrixStackIn.last().pose());

			matrixStackIn.popPose();
			this.lightTexture.turnOnLightLayer();
		}
	}
}