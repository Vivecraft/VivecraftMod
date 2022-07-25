package com.example.examplemod.mixin.client;

import java.io.File;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import net.minecraft.client.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.opengl.ARBShaderObjects;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.gameplay.trackers.TelescopeTracker;
import org.vivecraft.menuworlds.MenuWorldRenderer;
import org.vivecraft.provider.openvr_jna.MCOpenVR;
import org.vivecraft.provider.openvr_jna.OpenVRStereoRenderer;
import org.vivecraft.provider.openvr_jna.VRInputAction;
import org.vivecraft.provider.ovr_lwjgl.MC_OVR;
import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;
import org.vivecraft.render.PlayerModelController;
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
import com.example.examplemod.MinecraftExtension;
import com.example.examplemod.PlayerExtension;
import com.example.examplemod.RenderTargetExtension;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
//TODO Done except controls
@Mixin(Minecraft.class)
public abstract class MinecraftVRMixin2 extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, MinecraftExtension {

	@Unique
	private boolean lastClick;

	public MinecraftVRMixin2(String string) {
		super(string);
	}

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
	protected int missTime;

	@Final
	@Shadow
	private Gui gui;

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

	@Final
	@Shadow
	private Font font;

	@Final
	@Shadow
	private static boolean ON_OSX;

	@Shadow
	private boolean pause;

	@Shadow
	private float pausePartialTick;

	@Final
	@Shadow
	private Timer timer;

	@Final
	@Shadow
	private GameRenderer gameRenderer;

	@Shadow
	private ClientLevel level;

	@Shadow
	public RenderTarget mainRenderTarget;

	@Final
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

	@Shadow
	private HitResult hitResult;

	@Shadow
	private MultiPlayerGameMode gameMode;

	@Shadow
	@Final
	private PackRepository resourcePackRepository;
	
	@Shadow
	private CompletableFuture<Void> pendingReload;

	@Shadow
	@Final
	private Queue<Runnable> progressTasks;

	@Shadow
	@Final
	private MouseHandler mouseHandler;

	@Shadow
	@Final
	private ToastComponent toast;

	@Shadow
	private int frames;
	
	@Shadow
	private IntegratedServer singleplayerServer;

	@Shadow
	@Final
	private FrameTimer frameTimer;
	
	@Shadow
	private long lastNanoTime;
	
	@Shadow
	private long lastTime;

	@Shadow
	private String fpsString;

	@Final
	@Shadow
	private TextureManager textureManager;

	@Shadow
	private static int fps;

	@Shadow
	abstract void selectMainFont(boolean p_91337_);

	@Shadow
	abstract Entity getCameraEntity();

	@Shadow
	abstract void renderFpsMeter(PoseStack poseStack, ProfileResults fpsPieResults2);

	@Shadow
	abstract void clearResourcePacksOnError(Throwable throwable, @Nullable Component component);

	@Shadow
	abstract boolean hasSingleplayerServer();

	@Shadow
	abstract int getFramerateLimit();

	@Shadow
	abstract void tick();

	@Shadow
	abstract CompletableFuture<Void> reloadResourcePacks();

	@Shadow
	abstract void stop();

	@Shadow @Final public LevelRenderer levelRenderer;

	@Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;", remap = false), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public Thread settings() {
		if (!this.oculus) {
			DataHolder.getInstance().vr = new MCOpenVR((Minecraft) (Object) this, DataHolder.getInstance());
		} else {
			DataHolder.getInstance().vr = new MC_OVR((Minecraft) (Object) this, DataHolder.getInstance());
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

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWidth()I", ordinal = 0), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public int mainWidth(Window w) {
		return this.window.getScreenWidth();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getHeight()I", ordinal = 0), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public int mainHeight(Window w) {
		return this.window.getScreenHeight();
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

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/server/packs/resources/ReloadInstance;Ljava/util/function/Consumer;Z)V"), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public Consumer<Optional<Throwable>> menuInitvar(Consumer<Optional<Throwable>> c) {
		if (DataHolder.getInstance().vrRenderer.isInitialized()) {
			DataHolder.getInstance().menuWorldRenderer.init();
		}
		DataHolder.getInstance().vr.postinit();
		return c;
	}

	@ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;<init>(Lnet/minecraft/client/Minecraft;Lnet/minecraft/server/packs/resources/ReloadInstance;Ljava/util/function/Consumer;Z)V"), method = "reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;")
	public Consumer<Optional<Throwable>> reloadVar(Consumer<Optional<Throwable>> c) {
		if (DataHolder.getInstance().menuWorldRenderer.isReady() && DataHolder.getInstance().resourcePacksChanged) {
			try {
				DataHolder.getInstance().menuWorldRenderer.destroy();
				DataHolder.getInstance().menuWorldRenderer.prepare();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		DataHolder.getInstance().resourcePacksChanged = false;
		return c;
	}

	/**
	 * @author
	 * @reason
	 */
	@Overwrite
	private void rollbackResourcePacks(Throwable pThrowable) {
		if (this.resourcePackRepository.getSelectedPacks().stream().anyMatch(e -> !e.isRequired())) {
			TextComponent component;
			if (pThrowable instanceof SimpleReloadableResourceManager.ResourcePackLoadingFailure) {
				component = new TextComponent(
						((SimpleReloadableResourceManager.ResourcePackLoadingFailure) pThrowable).getPack().getName());
			} else {
				component = null;
			}

			this.clearResourcePacksOnError(pThrowable, component);
		} else {
			Util.throwAsRuntime(pThrowable);
		}
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE, ordinal = 2), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
	public void gui(Screen pGuiScreen, CallbackInfo info) {
		GuiHandler.onScreenChanged(this.screen, pGuiScreen, true);
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;delayedCrash:Ljava/util/function/Supplier;", shift = Shift.BEFORE), method = "destroy()V")
	public void destroy(CallbackInfo info) {
		try {
			DataHolder.getInstance().vr.destroy();
		}
		catch (Exception exception) {
		}
	}
	
	@Inject(at = @At("HEAD"), method = "runTick(Z)V", cancellable = true)
	public void replaceTick(boolean bl, CallbackInfo callback)  {
		newRunTick(bl);
		callback.cancel();
	}

	//Replaces normal runTick
	public void newRunTick(boolean bl) {
		this.window.setErrorSection("Pre render");
		// long l = Util.getNanos();
		if (this.window.shouldClose()) {
			this.stop();
		}

		if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
			CompletableFuture<Void> completableFuture = this.pendingReload;
			this.pendingReload = null;
			this.reloadResourcePacks().thenRun(() -> {
				completableFuture.complete(null);
			});
		}

		Runnable completableFuture;
		while ((completableFuture = (Runnable) this.progressTasks.poll()) != null) {
			completableFuture.run();
		}

		int j;
		if (bl) {
			// v
			++DataHolder.getInstance().frameIndex;
			//
			int i = this.timer.advanceTime(Util.getMillis());
			this.profiler.push("scheduledExecutables");
			this.runAllTasks();
			this.profiler.pop();
			// v
			try {
				DataHolder.getInstance().vrRenderer.setupRenderConfiguration();
			}
			catch (RenderConfigException renderconfigexception) {
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
				}
				else {
					this.notifyMirror(
							LangHelper.get("vivecraft.messages.rendersetupfailed", renderconfigexception.error), true,
							10000);
					this.drawNotifyMirror();

					if (DataHolder.getInstance().frameIndex % 300L == 0L) {
						System.out.println(renderconfigexception.title + " " + renderconfigexception.error);
					}

					try {
						Thread.sleep(10L);
					}
					catch (InterruptedException interruptedexception) {
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
			//
			this.profiler.push("tick");

			for (j = 0; j < Math.min(10, i); ++j) {
				this.profiler.incrementCounter("clientTick");
				// v
				DataHolder.getInstance().vrPlayer.preTick();
				//
				this.tick();
				// v
				DataHolder.getInstance().vrPlayer.postTick();
				//
			}

			this.profiler.pop();
		}

		// v
		this.profiler.push("setupRenderConfiguration");
		//
		this.mouseHandler.turnPlayer();
		this.window.setErrorSection("Render");
		this.profiler.push("sound");
		this.soundManager.updateSource(this.gameRenderer.getMainCamera());
		this.profiler.pop();
//		this.profiler.push("render");
//		PoseStack i = RenderSystem.getModelViewStack();
//		i.pushPose();
//		RenderSystem.applyModelViewMatrix();
//		RenderSystem.clear(16640, ON_OSX);
//		this.mainRenderTarget.bindWrite(true);

		// v
		try {
			this.checkGLError("pre render setup ");
			DataHolder.getInstance().vrRenderer.setupRenderConfiguration();
			this.checkGLError("post render setup ");
		}
		catch (Exception exception1) {
			exception1.printStackTrace();
		}

		float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
		this.profiler.popPush("preRender");
		DataHolder.getInstance().vrPlayer.preRender(f);
		this.profiler.popPush("2D");
		//

		FogRenderer.setupNoFog();
//		this.profiler.push("display");
		RenderSystem.enableTexture();
		RenderSystem.enableCull();
//		this.profiler.pop();

		// v
		this.profiler.push("Gui");
		DataHolder.getInstance().currentPass = RenderPass.GUI;
		this.gameRenderer.getMainCamera().setup(this.level, this.getCameraEntity(), false, false, f);

		//

		if (!this.noRender) {
//			this.profiler.popPush("gameRenderer");
//			this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, l, bl);
//			this.profiler.popPush("toasts");
//			this.toast.render(new PoseStack());
//			this.profiler.pop();
		}

//		if (this.fpsPieResults != null) {
//			this.profiler.push("fpsPie");
//			this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
//			this.profiler.pop();
//		}

		// v
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(true, true, true, true);
		this.mainRenderTarget = GuiHandler.guiFramebuffer;
		this.mainRenderTarget.clear(Minecraft.ON_OSX);
		this.mainRenderTarget.bindWrite(true);
		((GameRendererExtension) this.gameRenderer).drawFramebufferNEW(f, bl, new PoseStack());

		if (org.vivecraft.gameplay.screenhandlers.KeyboardHandler.Showing
				&& !DataHolder.getInstance().vrSettings.physicalKeyboard) {
			this.mainRenderTarget = org.vivecraft.gameplay.screenhandlers.KeyboardHandler.Framebuffer;
			this.mainRenderTarget.clear(Minecraft.ON_OSX);
			this.mainRenderTarget.bindWrite(true);
			((GameRendererExtension) this.gameRenderer).drawScreen(f,
					org.vivecraft.gameplay.screenhandlers.KeyboardHandler.UI, new PoseStack());
		}
		//

//		this.profiler.push("blit");
//		this.mainRenderTarget.unbindWrite();
//		i.popPose();
//		i.pushPose();
//		RenderSystem.applyModelViewMatrix();
//		this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
//		i.popPose();
//		RenderSystem.applyModelViewMatrix();
//		this.profiler.popPush("updateDisplay");
//		this.window.updateDisplay();

		// v
		if (RadialHandler.isShowing()) {
			this.mainRenderTarget = RadialHandler.Framebuffer;
			this.mainRenderTarget.clear(Minecraft.ON_OSX);
			this.mainRenderTarget.bindWrite(true);
			((GameRendererExtension) this.gameRenderer).drawScreen(f, RadialHandler.UI, new PoseStack());
		}

		this.checkGLError("post 2d ");
		VRHotkeys.updateMovingThirdPersonCam();
		this.profiler.popPush("sound");
		DataHolder.getInstance().currentPass = RenderPass.CENTER;
		this.soundManager.updateSource(this.gameRenderer.getMainCamera());
		this.profiler.pop();
		//

		j = this.getFramerateLimit();
//		if ((double) j < Option.FRAMERATE_LIMIT.getMaxValue()) {
//			RenderSystem.limitDisplayFPS(j);
//		}

		if (!this.noRender) {
			List<RenderPass> list = DataHolder.getInstance().vrRenderer.getRenderPasses();

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
				this.renderSingleView(renderpass.ordinal(), f, bl);
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

			if (bl) {
				DataHolder.getInstance().vrPlayer.postRender(f);
				this.profiler.push("Display/Reproject");

				try {
					DataHolder.getInstance().vrRenderer.endFrame();
				} catch (Exception exception) {
					//LOGGER.error(exception.toString());
				}

				this.profiler.pop();
				this.checkGLError("post submit ");
			}

			if (!this.noRender) {
				//Reflector.call(Reflector.BasicEventHooks_onRenderTickEnd, f);
			}

			this.profiler.push("mirror");
			this.mainRenderTarget.unbindWrite();
			this.copyToMirror();
			this.drawNotifyMirror();
			this.checkGLError("post-mirror ");
			this.profiler.pop();
		}

		//
		
//		this.profiler.popPush("yield");
//		Thread.yield();
//		this.profiler.pop();
		this.window.setErrorSection("Post render");
		
		//v
		this.window.updateDisplay();
		//
		
		++this.frames;
		boolean bl2 = this.hasSingleplayerServer()
				&& (this.screen != null && this.screen.isPauseScreen()
						|| this.overlay != null && this.overlay.isPauseScreen())
				&& !this.singleplayerServer.isPublished();
		if (this.pause != bl2) {
			if (this.pause) {
				this.pausePartialTick = this.timer.partialTick;
			} else {
				this.timer.partialTick = this.pausePartialTick;
			}

			this.pause = bl2;
		}

		long m = Util.getNanos();
		this.frameTimer.logFrameDuration(m - this.lastNanoTime);
		this.lastNanoTime = m;
		this.profiler.push("fpsUpdate");

		while (Util.getMillis() >= this.lastTime + 1000L) {
			fps = this.frames;
			this.fpsString = String.format("%d fps T: %s%s%s%s B: %d", fps,
					(double) this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf"
							: this.options.framerateLimit,
					this.options.enableVsync ? " vsync" : "", this.options.graphicsMode.toString(),
					this.options.renderClouds == CloudStatus.OFF ? ""
							: (this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
					this.options.biomeBlendRadius);
			this.lastTime += 1000L;
			this.frames = 0;
		}
		this.profiler.pop();
	}

	@Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setGuiScale(D)V", shift = Shift.AFTER), method = "resizeDisplay()V")
	public void reinitFrame(CallbackInfo info) {
		if (DataHolder.getInstance().vrRenderer != null) {
			DataHolder.getInstance().vrRenderer.reinitFrameBuffers("Main Window Changed");
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getMainRenderTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"), method = "resizeDisplay()V")
	public RenderTarget removeRenderTarget(Minecraft mc) {
		return null;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;resize(IIZ)V"), method = "resizeDisplay()V")
	public void cancelResizeTarget(RenderTarget r, int w, int h, boolean b) {
		return;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resize(II)V"), method = "resizeDisplay()V")
	public void cancelResizeGame(GameRenderer r, int w, int h) {
		return;
	}

	public void drawProfiler() {
		if (this.fpsPieResults != null) {
			this.profiler.push("fpsPie");
			this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
			this.profiler.pop();
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "continueAttack(Z)V")
	public void swingArmcontinueAttack(LocalPlayer player, InteractionHand hand) {
		((PlayerExtension) player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"), method = "continueAttack(Z)V")
	public void destroyseated(MultiPlayerGameMode gm) {
		if (DataHolder.getInstance().vrSettings.seated) {
			this.gameMode.stopDestroyBlock();
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"), method = "startUseItem()V")
	public boolean seatedCheck(MultiPlayerGameMode gameMode) {
		return gameMode.isDestroying() || !DataHolder.getInstance().vrSettings.seated;
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = Shift.AFTER, opcode = Opcodes.PUTFIELD), method = "startUseItem()V")
	public void breakDelay(CallbackInfo info) {
		if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.VANILLA)
			this.rightClickDelay = 4;
		else if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOW)
			this.rightClickDelay = 6;
		else if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWER)
			this.rightClickDelay = 8;
		else if (DataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWEST)
			this.rightClickDelay = 10;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), method = "startUseItem")
	public ItemStack activeHand2(LocalPlayer instance, InteractionHand interactionHand) {
		ItemStack itemInHand = instance.getItemInHand(interactionHand);
		if (DataHolder.getInstance().vrSettings.seated || !TelescopeTracker.isTelescope(itemInHand)) {
			NetworkHelper.sendActiveHand((byte) interactionHand.ordinal());
		}
		return itemInHand;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "startUseItem")
	public void swingUse(LocalPlayer instance, InteractionHand interactionHand) {
		((PlayerExtension)instance).swingArm(interactionHand, VRFirstPersonArmSwing.Use);
	}

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void tick(CallbackInfo info) {
		++DataHolder.getInstance().tickCounter;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "tick")
	public void removePick(GameRenderer instance, float f) {
		return;
	}
	
	public void textures() {
		this.textureManager.tick();
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;overlay:Lnet/minecraft/client/gui/screens/Overlay;", shift = Shift.BEFORE), method = "tick")
	public void vrInputs(CallbackInfo ci) {
		this.profiler.push("vrProcessInputs");
		DataHolder.getInstance().vr.processInputs();
		DataHolder.getInstance().vr.processBindings();
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;", ordinal = 4, shift = Shift.BEFORE), method = "tick")
	public void vrActions(CallbackInfo ci) {
		this.profiler.popPush("vrInputActionsTick");

		for (VRInputAction vrinputaction : DataHolder.getInstance().vr.getInputActions()) {
			vrinputaction.tick();
		}

		if (DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || DataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
			VRHotkeys.handleMRKeys();
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 2, shift = Shift.BEFORE), method = "tick")
	public void freeMove(CallbackInfo ci) {
		if (this.player != null) {
			DataHolder.getInstance().vrPlayer.updateFreeMove();

			if (DataHolder.getInstance().vrPlayer.teleportWarningTimer >= 0 && --DataHolder.getInstance().vrPlayer.teleportWarningTimer == 0) {
				this.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.noserverplugin"));
			}
		}
	}


	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;tick(Z)V"), method = "tick()V")
	public void tickmenu(CallbackInfo info) {
		if (DataHolder.getInstance().menuWorldRenderer != null) {
			DataHolder.getInstance().menuWorldRenderer.tick();
		}

		PlayerModelController.getInstance().tick();

	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
	public void noCamera(Options instance, CameraType cameraType) {
		return ;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
	public void vrMirrorOption(Options instance, CameraType cameraType) {
		DataHolder.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
		this.notifyMirror(DataHolder.getInstance().vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
		this.levelRenderer.needsUpdate();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"), method = "handleKeybinds")
	public void noPosEffect(GameRenderer instance, Entity entity) {
		return ;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "handleKeybinds()V")
	public void swingArmhandleKeybinds(LocalPlayer instance, InteractionHand interactionHand) {
		((PlayerExtension) player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"), method = "handleKeybinds")
	public boolean vrKeyuse(KeyMapping instance) {
		return !(!instance.isDown() && (!DataHolder.getInstance().bowTracker.isActive(this.player) || DataHolder.getInstance().vrSettings.seated) && ! DataHolder.getInstance().autoFood.isEating());
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", shift = Shift.BEFORE), method = "handleKeybinds")
	public void activeHand(CallbackInfo ci) {
		NetworkHelper.sendActiveHand((byte)this.player.getUsedItemHand().ordinal());
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 13), method = "handleKeybinds")
	public boolean notConsumeClick(KeyMapping instance) {
		return false;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()V", shift = Shift.AFTER), method = "handleKeybinds")
	public void lastClick(CallbackInfo ci) {
		this.lastClick = true;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 14, shift = Shift.BEFORE), method = "handleKeybinds")
	public void attackDown(CallbackInfo ci) {
		if (this.options.keyAttack.consumeClick() && this.screen == null){

		}
		else if (!this.options.keyAttack.isDown())
		{
			this.missTime = 0;

			if (this.lastClick) {
				this.gameMode.stopDestroyBlock();
			}

			this.lastClick = false;
		}
	}

	@Inject(at = @At("HEAD"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
	public void roomScale(ClientLevel pLevelClient, CallbackInfo info) {
		DataHolder.getInstance().vrPlayer.setRoomOrigin(0.0D, 0.0D, 0.0D, true);
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

	@Override
	public void notifyMirror(String text, boolean clear, int lengthMs) {
		this.mirroNotifyStart = System.currentTimeMillis();
		this.mirroNotifyLen = (long) lengthMs;
		this.mirrorNotifyText = text;
		this.mirrorNotifyClear = clear;
	}

	private void checkGLError(String string) {
		// TODO optifine
		if (GlStateManager._getError() != 0) {
			System.err.println(string);
		}

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
