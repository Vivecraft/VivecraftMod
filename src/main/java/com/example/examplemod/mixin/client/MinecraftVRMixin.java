package com.example.examplemod.mixin.client;

import java.io.File;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.lwjgl.opengl.ARBShaderObjects;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.menuworlds.MenuWorldRenderer;
import org.vivecraft.provider.openvr_jna.MCOpenVR;
import org.vivecraft.provider.openvr_jna.OpenVRStereoRenderer;
import org.vivecraft.provider.ovr_lwjgl.MC_OVR;
import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;
import org.vivecraft.render.RenderConfigException;
import org.vivecraft.render.RenderPass;
import org.vivecraft.render.VRFirstPersonArmSwing;
import org.vivecraft.render.VRShaders;
import org.vivecraft.settings.VRHotkeys;
import org.vivecraft.settings.VRSettings;
import org.vivecraft.utils.LangHelper;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Vector3;

import com.example.examplemod.DataHolder;
import com.example.examplemod.GameRendererExtension;
import com.example.examplemod.GlStateHelper;
import com.example.examplemod.PlayerExtension;
import com.example.examplemod.RenderTargetExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

@Mixin(Minecraft.class)
public abstract class MinecraftVRMixin {

	private static final String ENTITY = null;

	@Unique
	private boolean oculus;

	@Unique
	private long mirroNotifyStart;

	@Unique
	private long mirroNotifyLen;

	@Unique
	private boolean mirrorNotifyClear;

	@Unique
	private String mirrorNotifyText;

	@Unique
	private float fov = 1.0F;

	@Unique
	private FloatBuffer matrixBuffer = MemoryTracker.create(16).asFloatBuffer();

	@Unique
	private FloatBuffer matrixBuffer2 = MemoryTracker.create(16).asFloatBuffer();

	@Shadow
	@Final
	public File gameDirectory;

	@Shadow
	private Options options;

	@Shadow
	private Screen screen;

	@Shadow
	private ProfilerFiller profiler;

	@Shadow
	@Final
	private Window window;

	@Shadow
	private Overlay overlay;

	@Shadow
	private Font font;

	@Shadow
	private static boolean ON_OSX;

	@Shadow
	private boolean pause;

	@Shadow
	private float pausePartialTick;

	@Shadow
	private Timer timer;

	@Shadow
	private GameRenderer gameRenderer;

	@Shadow
	private ClientLevel level;

	@Shadow
	public RenderTarget mainRenderTarget;

	@Shadow
	private SoundManager soundManager;

	@Shadow
	private boolean noRender;

	@Shadow
	private LocalPlayer player;

	@Shadow
	private ProfileResults fpsPieResults;

	@Shadow
	private int rightClickDelay;

	private Object hitResult;

	private MultiPlayerGameMode gameMode;

	@Shadow
	abstract void selectMainFont(boolean p_91337_);

	@Shadow
	abstract void resizeDisplay();

	@Shadow
	protected abstract Entity getCameraEntity();

	@Shadow
	protected abstract void renderFpsMeter(PoseStack poseStack, ProfileResults fpsPieResults2);

	// TODO @Redirect really?
	@Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public Thread settings() {
		if (!this.oculus) {
			DataHolder.getInstance().vr = new MCOpenVR((Minecraft) (Object) this);
		} else {
			DataHolder.getInstance().vr = new MC_OVR((Minecraft) (Object) this);
		}

		VRSettings.initSettings((Minecraft) (Object) this, this.gameDirectory);

		if (!DataHolder.getInstance().vrSettings.badStereoProviderPluginID.isEmpty()) {
			DataHolder.getInstance().vrSettings.stereoProviderPluginID = DataHolder
					.getInstance().vrSettings.badStereoProviderPluginID;
			DataHolder.getInstance().vrSettings.badStereoProviderPluginID = "";
			DataHolder.getInstance().vrSettings.saveOptions();
		}
		return Thread.currentThread();
	}

	// Mod args
	public void rendertarget() {

	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;selectMainFont(Z)V"), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public void minecrift(Minecraft mc, boolean b) {
		this.selectMainFont(b);
		try {
			DataHolder dh = DataHolder.getInstance();
			dh.vr.init();

			if (!this.oculus) {
				dh.vrRenderer = new OpenVRStereoRenderer(dh.vr);
			} else {
				dh.vrRenderer = new OVR_StereoRenderer(dh.vr);
			}

			dh.vrPlayer = new VRPlayer();
			dh.vrRenderer.lastGuiScale = this.options.guiScale;
			dh.vrPlayer.registerTracker(dh.backpackTracker);
			dh.vrPlayer.registerTracker(dh.bowTracker);
			dh.vrPlayer.registerTracker(dh.climbTracker);
			dh.vrPlayer.registerTracker(dh.autoFood);
			dh.vrPlayer.registerTracker(dh.jumpTracker);
			dh.vrPlayer.registerTracker(dh.rowTracker);
			dh.vrPlayer.registerTracker(dh.runTracker);
			dh.vrPlayer.registerTracker(dh.sneakTracker);
			dh.vrPlayer.registerTracker(dh.swimTracker);
			dh.vrPlayer.registerTracker(dh.swingTracker);
			dh.vrPlayer.registerTracker(dh.interactTracker);
			dh.vrPlayer.registerTracker(dh.teleportTracker);
			dh.vrPlayer.registerTracker(dh.horseTracker);
			dh.vrPlayer.registerTracker(dh.vehicleTracker);
			// this.vrPlayer.registerTracker(this.physicalGuiManager);
			dh.vrPlayer.registerTracker(dh.crawlTracker);
			dh.vrPlayer.registerTracker(dh.cameraTracker);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;resizeDisplay()V"), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public void resize(Minecraft mc) {
		this.resizeDisplay();
		DataHolder.getInstance().menuWorldRenderer = new MenuWorldRenderer();
		DataHolder.getInstance().vrSettings.firstRun = false;
		DataHolder.getInstance().vrSettings.saveOptions();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;ifElse(Ljava/util/Optional;Ljava/util/function/Consumer;Ljava/lang/Runnable;)Ljava/util/Optional;"), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public Optional<Throwable> menuInit(Optional<Throwable> p_137522_, Consumer<Throwable> p_137523_,
			Runnable p_137524_) {
		if (DataHolder.getInstance().vrRenderer.isInitialized()) {
			DataHolder.getInstance().menuWorldRenderer.init();
		}
		DataHolder.getInstance().vr.postinit();
		return Util.ifElse(p_137522_, p_137523_, p_137524_);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;ifElse(Ljava/util/Optional;Ljava/util/function/Consumer;Ljava/lang/Runnable;)Ljava/util/Optional;"), method = "reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;")
	public void reload(boolean b, CallbackInfoReturnable<Boolean> info) {
		if (DataHolder.getInstance().menuWorldRenderer.isReady() && DataHolder.getInstance().resourcePacksChanged) {
			try {
				DataHolder.getInstance().menuWorldRenderer.destroy();
				DataHolder.getInstance().menuWorldRenderer.prepare();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		DataHolder.getInstance().resourcePacksChanged = false;
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
	public void gui(Screen pGuiScreen, CallbackInfo info) {
		GuiHandler.onScreenChanged(this.screen, pGuiScreen, true);
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/lang/System;nanoTime()J"), method = "destroy()V")
	public void destroy(CallbackInfo info) {
		try {
			DataHolder.getInstance().vr.destroy();
		} catch (Exception exception) {
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Timer;advanceTime(J)I", shift = Shift.BEFORE), method = "runTick(Z)V")
	public void time(boolean b, CallbackInfo info) {
		// this.options.ofFastRender = false; TODO Optifine
		++DataHolder.getInstance().frameIndex;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", shift = Shift.AFTER, ordinal = 0), method = "runTick(Z)V")
	public void renderSetup(boolean b, CallbackInfo info) {
		try {
			DataHolder.getInstance().vrRenderer.setupRenderConfiguration();
		} catch (RenderConfigException renderconfigexception) {
			this.screen = null;
			GlStateManager._viewport(0, 0, this.window.getScreenWidth(), this.window.getScreenHeight());

			if (this.overlay != null) {
				RenderSystem.clear(256, ON_OSX);
				Matrix4f matrix4f = Matrix4f.orthographic(
						(float) (this.window.getScreenWidth() / this.window.getGuiScale()),
						(float) (this.window.getScreenHeight() / this.window.getGuiScale()), 1000.0F, 3000.0F);
				RenderSystem.setProjectionMatrix(matrix4f);
				PoseStack p = new PoseStack();
				p.translate(0, 0, -2000);
				this.overlay.render(p, 0, 0, 0.0F);
			} else {
				this.notifyMirror(LangHelper.get("vivecraft.messages.rendersetupfailed", renderconfigexception.error),
						true, 10000);
				this.drawNotifyMirror();

				if (DataHolder.getInstance().frameIndex % 300L == 0L) {
					System.out.println(renderconfigexception.title + " " + renderconfigexception.error);
				}

				try {
					Thread.sleep(10L);
				} catch (InterruptedException interruptedexception) {
				}
			}

			this.window.updateDisplay();
			return;
		} catch (Exception exception2) {
			exception2.printStackTrace();
		}

		this.profiler.push("VR Poll/VSync");
		DataHolder.getInstance().vr.poll(DataHolder.getInstance().frameIndex);
		this.profiler.pop();
		DataHolder.getInstance().vrPlayer.postPoll();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.BEFORE), method = "runTick(Z)V")
	public void prevrtick(CallbackInfo info) {
		DataHolder.getInstance().vrPlayer.preTick();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.AFTER), method = "runTick(Z)V")
	public void postvrtick(CallbackInfo info) {
		DataHolder.getInstance().vrPlayer.postTick();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;turnPlayer()V", shift = Shift.BEFORE), method = "runTick(Z)V")
	public void push(CallbackInfo info) {
		// this.options.ofFastRender = false; TODO optifine
		this.profiler.push("setupRenderConfiguration");
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", ordinal = 3), method = "runTick(Z)V")
	public void removePush(ProfilerFiller f,String s) {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"), method = "runTick(Z)V")
	public void removePushPose(PoseStack s) {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;applyModelViewMatrix()V"), method = "runTick(Z)V")
	public void removeApply() {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V"), method = "runTick(Z)V")
	public void removeClear(int i, boolean b) {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V"), method = "runTick(Z)V")
	public void removeBind(RenderTarget t, boolean b) {
		return;
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.AFTER), method = "runTick(Z)V")
	public void setupRender(boolean pRenderLevel, CallbackInfo info) {
		try {
			this.checkGLError("pre render setup ");
			DataHolder.getInstance().vrRenderer.setupRenderConfiguration();
			this.checkGLError("post render setup ");
		} catch (Exception exception1) {
			exception1.printStackTrace();
		}

		float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
		this.profiler.popPush("preRender");
		DataHolder.getInstance().vrPlayer.preRender(f);
		this.profiler.popPush("2D");

	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", ordinal = 4), method = "runTick(Z)V")
	public void removeDisplayPush(ProfilerFiller f, String s) {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 3), method = "runTick(Z)V")
	public void removeDisplayPop(ProfilerFiller p) {
		this.profiler.push("Gui");
		DataHolder.getInstance().currentPass = RenderPass.GUI;
		float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
		//this.gameRenderer.getMainCamera().setup(this.level, this.getCameraEntity(), false, false, f);
	}
	
	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;fpsPieResults:Lnet/minecraft/util/profiling/ProfileResults;"), method = "runTick(Z)V")
	public ProfileResults rewriteFPDPieResults(Minecraft mc, boolean b) {
		return null;
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", shift = Shift.BEFORE, ordinal = 6), method = "runTick(Z)V")
	public void injectfPSPie(boolean pRenderLevel, CallbackInfo info) {
		float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
		this.mainRenderTarget = GuiHandler.guiFramebuffer;
		this.mainRenderTarget.clear(Minecraft.ON_OSX); 
		this.mainRenderTarget.bindWrite(true);
		((GameRendererExtension) this.gameRenderer).drawFramebufferNEW(f, pRenderLevel, new PoseStack());
		
		if (org.vivecraft.gameplay.screenhandlers.KeyboardHandler.Showing
				&& !DataHolder.getInstance().vrSettings.physicalKeyboard) {
			this.mainRenderTarget = org.vivecraft.gameplay.screenhandlers.KeyboardHandler.Framebuffer;
			this.mainRenderTarget.clear(Minecraft.ON_OSX);
			this.mainRenderTarget.bindWrite(true);
			((GameRendererExtension) this.gameRenderer).drawScreen(f,
					org.vivecraft.gameplay.screenhandlers.KeyboardHandler.UI, new PoseStack());
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", ordinal = 6), method = "runTick(Z)V")
	public void removeBlitpush(ProfilerFiller f, String s) {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;unbindWrite()V"), method = "runTick(Z)V")
	public void removeunbindWrite(RenderTarget t) {

	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"), method = "runTick(Z)V")
	public void removePopPose(PoseStack p) {

	}
	
//	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"), method = "runTick(Z)V")
//	public void removePushPose() {
//
//	}

//	public void removeApply2() {
//
//	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(II)V", ordinal = 0), method = "runTick(Z)V")
	public void removeblit(RenderTarget t, int i, int j) {

	}

//	public void removePopPose2() {
//
//	}
//
//	public void removeApply3() {
//
//	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 2), method = "runTick(Z)V")
	public void removePoppushDisplay(ProfilerFiller f,String s) {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateDisplay()V"), method = "runTick(Z)V")
	public void removeUpdateDisplay(Window window) {

	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/blaze3d/platform/Window;updateDisplay()V", shift = Shift.AFTER), method = "runTick(Z)V")
	public void radical(boolean pRenderLevel, CallbackInfo info) {
		if (RadialHandler.isShowing()) {
			this.mainRenderTarget = RadialHandler.Framebuffer;
			this.mainRenderTarget.clear(Minecraft.ON_OSX);
			this.mainRenderTarget.bindWrite(true);
			float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
			((GameRendererExtension) this.gameRenderer).drawScreen(f, RadialHandler.UI, new PoseStack());
		}

		this.checkGLError("post 2d ");
		VRHotkeys.updateMovingThirdPersonCam();
		this.profiler.popPush("sound");
		DataHolder.getInstance().currentPass = RenderPass.CENTER;
		this.soundManager.updateSource(this.gameRenderer.getMainCamera());
		this.profiler.pop();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getFramerateLimit()I"), method = "runTick(Z)V")
	public int rewriteFramerateLimit(Minecraft mc) {
		return (int) Option.FRAMERATE_LIMIT.getMaxValue();
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/Minecraft;getFramerateLimit()I", shift = Shift.AFTER), method = "runTick(Z)V")
	public void render(boolean pRenderLevel, CallbackInfo info) {
		if (!this.noRender) {
			List<RenderPass> list = DataHolder.getInstance().vrRenderer.getRenderPasses();

			float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
			for (RenderPass renderpass : list) {
				DataHolder.getInstance().currentPass = renderpass;

				switch (renderpass) {
				case LEFT:
				case RIGHT:
					this.mainRenderTarget = DataHolder.getInstance().vrRenderer.framebufferVrRender;
					break;

				case CENTER:
					this.mainRenderTarget = DataHolder.getInstance().vrRenderer.framebufferUndistorted;
					break;

				case THIRD:
					this.mainRenderTarget = DataHolder.getInstance().vrRenderer.framebufferMR;
					break;

				case SCOPEL:
					this.mainRenderTarget = DataHolder.getInstance().vrRenderer.telescopeFramebufferL;
					break;

				case SCOPER:
					this.mainRenderTarget = DataHolder.getInstance().vrRenderer.telescopeFramebufferR;
					break;

				case CAMERA:
					this.mainRenderTarget = DataHolder.getInstance().vrRenderer.cameraRenderFramebuffer;
				}

				this.profiler.push("Eye:" + DataHolder.getInstance().currentPass.ordinal());
				this.profiler.push("setup");
				this.mainRenderTarget.bindWrite(true);
				this.profiler.pop();
				this.renderSingleView(renderpass.ordinal(), f, pRenderLevel);
				this.profiler.pop();

				if (DataHolder.getInstance().grabScreenShot) {
					boolean flag;

					if (list.contains(RenderPass.CAMERA)) {
						flag = renderpass == RenderPass.CAMERA;
					} else if (list.contains(RenderPass.CENTER)) {
						flag = renderpass == RenderPass.CENTER;
					} else {
						flag = DataHolder.getInstance().vrSettings.displayMirrorLeftEye ? renderpass == RenderPass.LEFT
								: renderpass == RenderPass.RIGHT;
					}

					if (flag) {
						RenderTarget rendertarget = this.mainRenderTarget;

						if (renderpass == RenderPass.CAMERA) {
							rendertarget = DataHolder.getInstance().vrRenderer.cameraFramebuffer;
						}

						this.mainRenderTarget.unbindWrite();
						Utils.takeScreenshot(rendertarget);
						this.window.updateDisplay();
						DataHolder.getInstance().grabScreenShot = false;
					}
				}
			}

			if (pRenderLevel) {
				DataHolder.getInstance().vrPlayer.postRender(f);
				this.profiler.push("Display/Reproject");

				try {
					DataHolder.getInstance().vrRenderer.endFrame();
				} catch (Exception exception) {
					// LOGGER.error(exception.toString());
				}

				this.profiler.pop();
				this.checkGLError("post submit ");
			}

			if (!this.noRender) {
				// TODO needed?
				net.minecraftforge.fmllegacy.hooks.BasicEventHooks
						.onRenderTickEnd(this.pause ? this.pausePartialTick : this.timer.partialTick);
			}

			this.profiler.push("mirror");
			this.mainRenderTarget.unbindWrite();
			this.copyToMirror();
			this.drawNotifyMirror();
			this.checkGLError("post-mirror ");
			this.profiler.pop();
		}
	}

	public void reinitFrame() {
		if (DataHolder.getInstance().vrRenderer != null) {
			DataHolder.getInstance().vrRenderer.reinitFrameBuffers("Main Window Changed");
		}
	}

	public void cancelResizeTarget() {
		return;
	}

	public void cancelResizeGame() {
		return;
	}

	public void drawProfiler() {
		if (this.fpsPieResults != null) {
			this.profiler.push("fpsPie");
			this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
			this.profiler.pop();
		}
	}

	public void swingArm() {
		((PlayerExtension) this.player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
	}

	public void destroyseated(CallbackInfo info) {
		if (!DataHolder.getInstance().vrSettings.seated) {
			info.cancel();
		}
	}

	public boolean seatedCheck(MultiPlayerGameMode gameMode) {
		return gameMode.isDestroying() || !DataHolder.getInstance().vrSettings.seated;
	}

	public void breakDelay() {
		if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.VANILLA)
			this.rightClickDelay = 4;
		else if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOW)
			this.rightClickDelay = 6;
		else if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWER)
			this.rightClickDelay = 8;
		else if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWEST)
			this.rightClickDelay = 10;

	}

	private void drawNotifyMirror() {
		if (System.currentTimeMillis() < this.mirroNotifyStart + this.mirroNotifyLen) {
			RenderSystem.viewport(0, 0, this.window.getScreenWidth(), this.window.getScreenHeight());
			Matrix4f matrix4f = Matrix4f.orthographic(0.0F, (float) this.window.getScreenWidth(), 0.0F,
					(float) this.window.getScreenHeight(), 1000.0F, 3000.0F);
			RenderSystem.setProjectionMatrix(matrix4f);
			RenderSystem.getModelViewStack().setIdentity();
			RenderSystem.getModelViewStack().translate(0, 0, -2000);
			RenderSystem.applyModelViewMatrix();
			PoseStack p = new PoseStack();
			p.scale(3, 3, 3);
			RenderSystem.clear(256, ON_OSX);

			if (this.mirrorNotifyClear) {
				RenderSystem.clearColor(0, 0, 0, 0);
				RenderSystem.clear(16384, ON_OSX);
			}

			int i = this.window.getScreenWidth() / 22;
			ArrayList<String> arraylist = new ArrayList<>();

			if (this.mirrorNotifyText != null) {
				Utils.wordWrap(this.mirrorNotifyText, i, arraylist);
			}

			int j = 1;
			int k = 12;

			for (String s : arraylist) {
				this.font.draw(p, s, 1.0F, (float) j, 16777215);
				j += 12;
			}
		}
	}

	private void notifyMirror(String text, boolean clear, int lengthMs) {
		this.mirroNotifyStart = System.currentTimeMillis();
		this.mirroNotifyLen = (long) lengthMs;
		this.mirrorNotifyText = text;
		this.mirrorNotifyClear = clear;
	}

	private void checkGLError(String string) {
		// TODO optifine
		System.err.println(string);
	}

	private void renderSingleView(int eye, float nano, boolean renderworld) {
		GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
		GlStateHelper.clear(16640);
		GlStateManager._enableTexture();
		GlStateManager._enableDepthTest();
		this.profiler.push("updateCameraAndRender");
		this.gameRenderer.render(nano, System.nanoTime(), renderworld);
		this.profiler.pop();
		this.checkGLError("post game render " + eye);

		if (DataHolder.getInstance().currentPass == RenderPass.LEFT
				|| DataHolder.getInstance().currentPass == RenderPass.RIGHT) {
			this.profiler.push("postprocesseye");
			RenderTarget rendertarget = this.mainRenderTarget;

			if (DataHolder.getInstance().vrSettings.useFsaa) {
				this.profiler.push("fsaa");
				// DataHolder.getInstance().vrRenderer.doFSAA(Config.isShaders()); TODO
				DataHolder.getInstance().vrRenderer.doFSAA(false);
				rendertarget = DataHolder.getInstance().vrRenderer.fsaaLastPassResultFBO;
				this.checkGLError("fsaa " + eye);
				this.profiler.pop();
			}

			if (DataHolder.getInstance().currentPass == RenderPass.LEFT) {
				DataHolder.getInstance().vrRenderer.framebufferEye0.bindWrite(true);
			} else {
				DataHolder.getInstance().vrRenderer.framebufferEye1.bindWrite(true);
			}

			if (DataHolder.getInstance().vrSettings.useFOVReduction
					&& DataHolder.getInstance().vrPlayer.getFreeMove()) {
				if (this.player != null && (Math.abs(this.player.zza) > 0.0F || Math.abs(this.player.xxa) > 0.0F)) {
					this.fov = (float) ((double) this.fov - 0.05D);

					if (this.fov < DataHolder.getInstance().vrSettings.fovReductionMin) {
						this.fov = DataHolder.getInstance().vrSettings.fovReductionMin;
					}
				} else {
					this.fov = (float) ((double) this.fov + 0.01D);

					if ((double) this.fov > 0.8D) {
						this.fov = 0.8F;
					}
				}
			} else {
				this.fov = 1.0F;
			}

			GlStateManager._glUseProgram(VRShaders._FOVReduction_shaderProgramId);
			ARBShaderObjects.glUniform1iARB(VRShaders._FOVReduction_TextureUniform, 0);
			ARBShaderObjects.glUniform1fARB(VRShaders._FOVReduction_OffsetUniform,
					DataHolder.getInstance().vrSettings.fovRedutioncOffset);
			float red = 0.0F;
			float black = 0.0F;
			float blue = 0.0F;
			float time = (float) Util.getMillis() / 1000.0F;

			if (this.player != null && this.level != null) {
				if (((GameRendererExtension) this.gameRenderer)
						.isInWater() != ((GameRendererExtension) this.gameRenderer).isInWater()) {
					DataHolder.getInstance().watereffect = 2.3F;
				} else {
					if (((GameRendererExtension) this.gameRenderer).isInWater()) {
						DataHolder.getInstance().watereffect -= 0.008333334F;
					} else {
						DataHolder.getInstance().watereffect -= 0.016666668F;
					}

					if (DataHolder.getInstance().watereffect < 0.0F) {
						DataHolder.getInstance().watereffect = 0.0F;
					}
				}

				((GameRendererExtension) this.gameRenderer)
						.setWasInWater(((GameRendererExtension) this.gameRenderer).isInWater());

//				if (Config.isShaders()) { TODO
//					DataHolder.getInstance().watereffect = 0.0F;
//				}

				if (((GameRendererExtension) this.gameRenderer).isInPortal()) {
					DataHolder.getInstance().portaleffect = 1.0F;
				} else {
					DataHolder.getInstance().portaleffect -= 0.016666668F;

					if (DataHolder.getInstance().portaleffect < 0.0F) {
						DataHolder.getInstance().portaleffect = 0.0F;
					}
				}

				ItemStack itemstack = this.player.getInventory().getArmor(3);

				if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem()
						&& (!itemstack.hasTag() || itemstack.getTag().getInt("CustomModelData") == 0)) {
					DataHolder.getInstance().pumpkineffect = 1.0F;
				}

				float hurtTimer = (float) this.player.hurtTime - nano;
				float healthpercent = 1.0F - this.player.getHealth() / this.player.getMaxHealth();
				healthpercent = (healthpercent - 0.5F) * 0.75F;

				if (hurtTimer > 0.0F) { // hurt flash
					hurtTimer = hurtTimer / (float) this.player.hurtDuration;
					hurtTimer = healthpercent
							+ Mth.sin(hurtTimer * hurtTimer * hurtTimer * hurtTimer * (float) Math.PI) * 0.5F;
					red = hurtTimer;
				} else { // red due to low health
					red = (float) ((double) healthpercent
							* Math.abs(Math.sin((double) (2.5F * time) / ((double) (1.0F - healthpercent) + 0.1D))));

					if (this.player.isCreative()) {
						red = 0.0F;
					}
				}

				float freeze = this.player.getPercentFrozen();
				if (freeze > 0) {
					blue = red;
					blue = Math.max(freeze / 2, blue);
					red = 0;
				}

				if (this.player.isSleeping() && (double) black < 0.8D) {
					black = 0.8F;
				}

				if (DataHolder.getInstance().vr.isWalkingAbout && (double) black < 0.8D) {
					black = 0.5F;
				}
			} else {
				DataHolder.getInstance().watereffect = 0.0F;
				DataHolder.getInstance().portaleffect = 0.0F;
				DataHolder.getInstance().pumpkineffect = 0.0F;
			}

			if (DataHolder.getInstance().pumpkineffect > 0.0F) {
				ARBShaderObjects.glUniform1fARB(VRShaders._FOVReduction_RadiusUniform, 0.3F);
				ARBShaderObjects.glUniform1fARB(VRShaders._FOVReduction_BorderUniform, 0.0F);
			} else {
				ARBShaderObjects.glUniform1fARB(VRShaders._FOVReduction_RadiusUniform, this.fov);
				ARBShaderObjects.glUniform1fARB(VRShaders._FOVReduction_BorderUniform, 0.06F);
			}

			ARBShaderObjects.glUniform1fARB(VRShaders._Overlay_HealthAlpha, red);
			ARBShaderObjects.glUniform1fARB(VRShaders._Overlay_FreezeAlpha, blue);
			ARBShaderObjects.glUniform1fARB(VRShaders._Overlay_BlackAlpha, black);
			ARBShaderObjects.glUniform1fARB(VRShaders._Overlay_time, time);
			ARBShaderObjects.glUniform1fARB(VRShaders._Overlay_waterAmplitude, DataHolder.getInstance().watereffect);
			ARBShaderObjects.glUniform1fARB(VRShaders._Overlay_portalAmplitutde, DataHolder.getInstance().portaleffect);
			ARBShaderObjects.glUniform1fARB(VRShaders._Overlay_pumpkinAmplitutde,
					DataHolder.getInstance().pumpkineffect);
			RenderPass renderpass = DataHolder.getInstance().currentPass;
			ARBShaderObjects.glUniform1iARB(VRShaders._Overlay_eye,
					DataHolder.getInstance().currentPass == RenderPass.LEFT ? 1 : -1);
			((RenderTargetExtension) rendertarget).setBlitLegacy(true);
			rendertarget.blitToScreen(DataHolder.getInstance().vrRenderer.framebufferEye0.viewWidth,
					DataHolder.getInstance().vrRenderer.framebufferEye0.viewHeight);
			((RenderTargetExtension) rendertarget).setBlitLegacy(false);
			GlStateManager._glUseProgram(0);
			this.checkGLError("post overlay" + eye);
			this.profiler.pop();
		}

		if (DataHolder.getInstance().currentPass == RenderPass.CAMERA) {
			this.profiler.push("cameracopy");
			DataHolder.getInstance().vrRenderer.cameraFramebuffer.bindWrite(true);
			GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
			GlStateHelper.clear(16640);
			((RenderTargetExtension) DataHolder.getInstance().vrRenderer.cameraRenderFramebuffer).blitToScreen(0,
					DataHolder.getInstance().vrRenderer.cameraFramebuffer.viewWidth,
					DataHolder.getInstance().vrRenderer.cameraFramebuffer.viewHeight, 0, true, 0.0F, 0.0F, false);
			this.profiler.pop();
		}
	}

	private void copyToMirror() {
		// TODO: fix mixed reality... again
		if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY)
			DataHolder.getInstance().vrSettings.displayMirrorMode = VRSettings.MirrorMode.CROPPED;

		if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF
				&& DataHolder.getInstance().vr.isHMDTracking()) {
			this.notifyMirror("Mirror is OFF", true, 1000);
		} else if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
			if (VRShaders._DepthMask_shaderProgramId != 0) {
				this.doMixedRealityMirror();
			} else {
				this.notifyMirror("Shader compile failed, see log", true, 10000);
			}
		} else if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.DUAL) {
			RenderTarget rendertarget = DataHolder.getInstance().vrRenderer.framebufferEye0;
			RenderTarget rendertarget1 = DataHolder.getInstance().vrRenderer.framebufferEye1;

			if (rendertarget != null) {
				((RenderTargetExtension) rendertarget).blitToScreen(0, this.window.getScreenWidth() / 2,
						this.window.getScreenHeight(), 0, true, 0.0F, 0.0F, false);
			}

			if (rendertarget1 != null) {
				((RenderTargetExtension) rendertarget1).blitToScreen(this.window.getScreenWidth() / 2,
						this.window.getScreenWidth() / 2, this.window.getScreenHeight(), 0, true, 0.0F, 0.0F, false);
			}
		} else {
			float xcrop = 0.0F;
			float ycrop = 0.0F;
			boolean ar = false;
			RenderTarget source = DataHolder.getInstance().vrRenderer.framebufferEye0;

			if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
				source = DataHolder.getInstance().vrRenderer.framebufferUndistorted;
			} else if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
				source = DataHolder.getInstance().vrRenderer.framebufferMR;
			} else if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.SINGLE
					|| DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF) {
				if (!DataHolder.getInstance().vrSettings.displayMirrorLeftEye)
					source = DataHolder.getInstance().vrRenderer.framebufferEye1;
			} else if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.CROPPED) {
				if (!DataHolder.getInstance().vrSettings.displayMirrorLeftEye)
					source = DataHolder.getInstance().vrRenderer.framebufferEye1;

				xcrop = 0.15F;
				ycrop = 0.15F;
				ar = true;
			}
			// Debug
			// source = GuiHandler.guiFramebuffer;
			// source = vrRenderer.telescopeFramebufferR;
			//
			if (source != null) {
				((RenderTargetExtension) source).blitToScreen(0, this.window.getScreenWidth(),
						this.window.getScreenHeight(), 0, true, xcrop, ycrop, ar);
			}
		}
	}

	private void doMixedRealityMirror() {
//		boolean flag = Config.isShaders();
		boolean flag = false;
		boolean flag1 = DataHolder.getInstance().vrSettings.mixedRealityUnityLike
				&& DataHolder.getInstance().vrSettings.mixedRealityAlphaMask;

		if (!flag1) {
			GlStateManager._clearColor(
					(float) DataHolder.getInstance().vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
					(float) DataHolder.getInstance().vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
					(float) DataHolder.getInstance().vrSettings.mixedRealityKeyColor.getBlue() / 255.0F, 1.0F);
		} else {
			GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
		}

		GlStateHelper.clear(16640);
		Vec3 vec3 = DataHolder.getInstance().vrPlayer.vrdata_room_pre.getHeadPivot()
				.subtract(DataHolder.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getPosition());
		com.mojang.math.Matrix4f matrix4f = DataHolder.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD)
				.getMatrix().transposed().toMCMatrix();
		Vector3 vector3 = DataHolder.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getMatrix()
				.transform(Vector3.forward());
		GlStateManager._glUseProgram(VRShaders._DepthMask_shaderProgramId);
		((GameRendererExtension) this.gameRenderer).getThirdPassProjectionMatrix().store(this.matrixBuffer);
		((Buffer) this.matrixBuffer).rewind();
		ARBShaderObjects.glUniformMatrix4fvARB(VRShaders._DepthMask_projectionMatrix, false, this.matrixBuffer);
		matrix4f.store(this.matrixBuffer);
		((Buffer) this.matrixBuffer).rewind();
		ARBShaderObjects.glUniformMatrix4fvARB(VRShaders._DepthMask_viewMatrix, false, this.matrixBuffer);
		ARBShaderObjects.glUniform1iARB(VRShaders._DepthMask_colorTexUniform, 1);
		ARBShaderObjects.glUniform1iARB(VRShaders._DepthMask_depthTexUniform, 2);
		ARBShaderObjects.glUniform3fARB(VRShaders._DepthMask_hmdViewPosition, (float) vec3.x, (float) vec3.y,
				(float) vec3.z);
		ARBShaderObjects.glUniform3fARB(VRShaders._DepthMask_hmdPlaneNormal, -vector3.getX(), 0.0F, -vector3.getZ());
		ARBShaderObjects.glUniform3fARB(VRShaders._DepthMask_keyColorUniform,
				(float) DataHolder.getInstance().vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
				(float) DataHolder.getInstance().vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
				(float) DataHolder.getInstance().vrSettings.mixedRealityKeyColor.getBlue() / 255.0F);
		ARBShaderObjects.glUniform1iARB(VRShaders._DepthMask_alphaModeUniform, flag1 ? 1 : 0);
		GlStateManager._activeTexture(33985);
		DataHolder.getInstance().vrRenderer.framebufferMR.bindRead();
		GlStateManager._activeTexture(33986);

//		if (flag && Shaders.dfb != null) { TODO
//			GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(0));
//		} else {
		GlStateManager._bindTexture(
				((RenderTargetExtension) DataHolder.getInstance().vrRenderer.framebufferMR).getDepthBufferId());
//		}

		GlStateManager._activeTexture(33984);

		for (int i = 0; i < (flag1 ? 3 : 2); ++i) {
			int j = this.window.getScreenWidth() / 2;
			int k = this.window.getScreenHeight();
			int l = this.window.getScreenWidth() / 2 * i;
			int i1 = 0;

			if (DataHolder.getInstance().vrSettings.mixedRealityUnityLike) {
				j = this.window.getScreenWidth() / 2;
				k = this.window.getScreenHeight() / 2;

				if (DataHolder.getInstance().vrSettings.mixedRealityAlphaMask && i == 2) {
					l = this.window.getScreenWidth() / 2;
					i1 = this.window.getScreenHeight() / 2;
				} else {
					l = 0;
					i1 = this.window.getScreenHeight() / 2 * (1 - i);
				}
			}

			ARBShaderObjects.glUniform2fARB(VRShaders._DepthMask_resolutionUniform, (float) j, (float) k);
			ARBShaderObjects.glUniform2fARB(VRShaders._DepthMask_positionUniform, (float) l, (float) i1);
			ARBShaderObjects.glUniform1iARB(VRShaders._DepthMask_passUniform, i);
			((RenderTargetExtension) DataHolder.getInstance().vrRenderer.framebufferMR).blitToScreen(l, j, k, i1, true,
					0.0F, 0.0F, false);
		}

		GlStateManager._glUseProgram(0);

		if (DataHolder.getInstance().vrSettings.mixedRealityUnityLike) {
			if (DataHolder.getInstance().vrSettings.mixedRealityUndistorted) {
				((RenderTargetExtension) DataHolder.getInstance().vrRenderer.framebufferUndistorted).blitToScreen(
						this.window.getScreenWidth() / 2, this.window.getScreenWidth() / 2,
						this.window.getScreenHeight() / 2, 0, true, 0.0F, 0.0F, false);
			} else {
				((RenderTargetExtension) DataHolder.getInstance().vrRenderer.framebufferEye0).blitToScreen(
						this.window.getScreenWidth() / 2, this.window.getScreenWidth() / 2,
						this.window.getScreenHeight() / 2, 0, true, 0.0F, 0.0F, false);
			}
		}
	}

}
