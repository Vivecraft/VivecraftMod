package org.vivecraft.mixin.client;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.ClientDataHolder;
import org.vivecraft.GlStateHelper;
import org.vivecraft.IrisHelper;
import org.vivecraft.MethodHolder;
import org.vivecraft.SodiumHelper;
import org.vivecraft.Xevents;
import org.vivecraft.Xplat;
import org.vivecraft.extensions.GameRendererExtension;
import org.vivecraft.extensions.MinecraftExtension;
import org.vivecraft.extensions.PlayerExtension;
import org.vivecraft.extensions.RenderTargetExtension;
import org.vivecraft.api.ClientNetworkHelper;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.gameplay.trackers.TelescopeTracker;
import org.vivecraft.provider.openvr_jna.MCOpenVR;
import org.vivecraft.provider.openvr_jna.OpenVRStereoRenderer;
import org.vivecraft.provider.openvr_jna.VRInputAction;
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
//import org.vivecraft.provider.ovr_lwjgl.MC_OVR;
//import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;
//import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftVRMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, MinecraftExtension {

	@Unique
	private boolean lastClick;
	@Unique
	private ItemStack itemInHand; //Captured item

	public MinecraftVRMixin(String string) {
		super(string);
	}

	@Unique
	private boolean oculus = false;

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

	@Shadow
	protected int missTime;

	@Final
	@Shadow
	public Gui gui;

	@Shadow
	@Final
	public File gameDirectory;

	@Shadow
	public Options options;

	@Shadow
	public Screen screen;

	@Shadow
	private ProfilerFiller profiler;

	@Shadow
	@Final
	private Window window;

	@Shadow
	private Overlay overlay;

	@Final
	@Shadow
	public Font font;

	@Final
	@Shadow
	public static boolean ON_OSX;

	@Shadow
	private boolean pause;

	@Shadow
	private float pausePartialTick;

	@Final
	@Shadow
	private Timer timer;

	@Final
	@Shadow
	public GameRenderer gameRenderer;

	@Shadow
	public ClientLevel level;

	@Shadow
	public RenderTarget mainRenderTarget;

	@Final
	@Shadow
	private SoundManager soundManager;

	@Shadow
	public boolean noRender;

	@Shadow
	public LocalPlayer player;

	@Shadow
	private ProfileResults fpsPieResults;

	@Shadow
	private int rightClickDelay;

	@Shadow
	public HitResult hitResult;

	@Shadow
	public MultiPlayerGameMode gameMode;

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
	public MouseHandler mouseHandler;

	@Shadow
	@Final
	private ToastComponent toast;

	@Shadow
	private int frames;
	
	@Shadow
	private IntegratedServer singleplayerServer;

	@Shadow
	@Final
	public FrameTimer frameTimer;
	
	@Shadow
	private long lastNanoTime;
	
	@Shadow
	private long lastTime;

	@Shadow
	public String fpsString;

	@Final
	@Shadow
	private TextureManager textureManager;

	@Shadow
	private static int fps;

	@Shadow
	abstract void selectMainFont(boolean p_91337_);

	@Shadow
	public abstract Entity getCameraEntity();

	@Shadow
	protected abstract void renderFpsMeter(PoseStack poseStack, ProfileResults fpsPieResults2);

	@Shadow
	public abstract void clearResourcePacksOnError(Throwable throwable, @Nullable Component component);

	@Shadow
	public abstract boolean hasSingleplayerServer();

	@Shadow
	protected abstract int getFramerateLimit();

	@Shadow
	public abstract void tick();

	@Shadow
	public abstract CompletableFuture<Void> reloadResourcePacks();

	@Shadow
	public abstract void stop();

	@Shadow @Final public LevelRenderer levelRenderer;

	@Shadow private static Minecraft instance;

	@Shadow protected abstract boolean startAttack();

	@Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;", remap = false), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public Thread settings() {
		if (!this.oculus) {
			ClientDataHolder.getInstance().vr = new MCOpenVR((Minecraft) (Object) this, ClientDataHolder.getInstance());
		} else {
			//DataHolder.getInstance().vr = new MC_OVR((Minecraft) (Object) this, DataHolder.getInstance());
		}

		VRSettings.initSettings((Minecraft) (Object) this, this.gameDirectory);

		if (!ClientDataHolder.getInstance().vrSettings.badStereoProviderPluginID.isEmpty()) {
			ClientDataHolder.getInstance().vrSettings.stereoProviderPluginID = ClientDataHolder
					.getInstance().vrSettings.badStereoProviderPluginID;
			ClientDataHolder.getInstance().vrSettings.badStereoProviderPluginID = "";
			ClientDataHolder.getInstance().vrSettings.saveOptions();
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
			ClientDataHolder dh = ClientDataHolder.getInstance();
			dh.vr.init();

			if (!this.oculus) {
				dh.vrRenderer = new OpenVRStereoRenderer(dh.vr);
			} else {
				//dh.vrRenderer = new OVR_StereoRenderer(dh.vr);
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
		//DataHolder.getInstance().menuWorldRenderer = new MenuWorldRenderer();
		ClientDataHolder.getInstance().vrSettings.firstRun = false;
		ClientDataHolder.getInstance().vrSettings.saveOptions();
	}

//	/**
//	 * @author
//	 * @reason
//	 */
//	@Overwrite
//	private void rollbackResourcePacks(Throwable pThrowable) {
//		if (this.resourcePackRepository.getSelectedPacks().stream().anyMatch(e -> !e.isRequired())) {
//			TextComponent component;
//			if (pThrowable instanceof SimpleReloadableResourceManager.ResourcePackLoadingFailure) {
//				component = new TextComponent(
//						((SimpleReloadableResourceManager.ResourcePackLoadingFailure) pThrowable).getPack().getName());
//			} else {
//				component = null;
//			}
//
//			this.clearResourcePacksOnError(pThrowable, component);
//		} else {
//			Util.throwAsRuntime(pThrowable);
//		}
//	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;delayedCrash:Ljava/util/function/Supplier;", shift = Shift.BEFORE), method = "destroy()V")
	public void destroy(CallbackInfo info) {
		try {
			ClientDataHolder.getInstance().vr.destroy();
		}
		catch (Exception exception) {
		}
	}
	
	@Inject(at = @At("HEAD"), method = "runTick(Z)V", cancellable = true)
	public void replaceTick(boolean bl, CallbackInfo callback)  {
		if (Xplat.isModLoaded("sodium") || Xplat.isModLoaded("rubidium")) {
			SodiumHelper.preRenderMinecraft();
		}
		newRunTick(bl);
		if (Xplat.isModLoaded("sodium") || Xplat.isModLoaded("rubidium")) {
			SodiumHelper.postRenderMinecraft();
		}
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
			++ClientDataHolder.getInstance().frameIndex;
			//
			int i = this.timer.advanceTime(Util.getMillis());
			this.profiler.push("scheduledExecutables");
			this.runAllTasks();
			this.profiler.pop();
			// v
			try {
				ClientDataHolder.getInstance().vrRenderer.setupRenderConfiguration();
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
					if (MethodHolder.isKeyDown(GLFW.GLFW_KEY_Q)) {
						System.out.println("Resetting VR status!");
						Path file = Xplat.getConfigPath("vivecraft-config.properties");

						Properties properties = new Properties();
						properties.setProperty("vrStatus", "false");
						try {
							properties.store(Files.newOutputStream(file), "This file stores if VR should be enabled.");
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						Minecraft.getInstance().stop();
					}
					this.notifyMirror(
							LangHelper.get("vivecraft.messages.rendersetupfailed", renderconfigexception.error), true,
							10000);
					this.drawNotifyMirror();

					if (ClientDataHolder.getInstance().frameIndex % 300L == 0L) {
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
			ClientDataHolder.getInstance().vr.poll(ClientDataHolder.getInstance().frameIndex);
			this.profiler.pop();
			ClientDataHolder.getInstance().vrPlayer.postPoll();
			//
			this.profiler.push("tick");

			for (j = 0; j < Math.min(10, i); ++j) {
				this.profiler.incrementCounter("clientTick");
				// v
				ClientDataHolder.getInstance().vrPlayer.preTick();
				//
				this.tick();
				// v
				ClientDataHolder.getInstance().vrPlayer.postTick();
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
			ClientDataHolder.getInstance().vrRenderer.setupRenderConfiguration();
			this.checkGLError("post render setup ");
		}
		catch (Exception exception1) {
			exception1.printStackTrace();
		}

		float f = this.pause ? this.pausePartialTick : this.timer.partialTick;
		this.profiler.popPush("preRender");
		ClientDataHolder.getInstance().vrPlayer.preRender(f);
		this.profiler.popPush("2D");
		//

		FogRenderer.setupNoFog();
//		this.profiler.push("display");
		RenderSystem.enableTexture();
		RenderSystem.enableCull();
//		this.profiler.pop();

		// v
		this.profiler.push("Gui");
		ClientDataHolder.getInstance().currentPass = RenderPass.GUI;
		this.gameRenderer.getMainCamera().setup(this.level, this.getCameraEntity(), false, false, f);

		//

		if (!this.noRender) {
//			this.profiler.popPush("gameRenderer");
//			this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, l, bl);
//			this.profiler.popPush("toasts");
//			this.toast.render(new PoseStack());
//			this.profiler.pop();
			Xevents.onRenderTickStart(this.pause ? this.pausePartialTick : this.timer.partialTick);
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

		if (KeyboardHandler.Showing
				&& !ClientDataHolder.getInstance().vrSettings.physicalKeyboard) {
			this.mainRenderTarget = KeyboardHandler.Framebuffer;
			this.mainRenderTarget.clear(Minecraft.ON_OSX);
			this.mainRenderTarget.bindWrite(true);
			((GameRendererExtension) this.gameRenderer).drawScreen(f,
					KeyboardHandler.UI, new PoseStack());
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
		ClientDataHolder.getInstance().currentPass = RenderPass.CENTER;
		this.soundManager.updateSource(this.gameRenderer.getMainCamera());
		this.profiler.pop();
		//

		j = this.getFramerateLimit();
//		if ((double) j < Option.FRAMERATE_LIMIT.getMaxValue()) {
//			RenderSystem.limitDisplayFPS(j);
//		}

		if (!this.noRender) {
			List<RenderPass> list = ClientDataHolder.getInstance().vrRenderer.getRenderPasses();

			ClientDataHolder.getInstance().isFirstPass = true;
			for (RenderPass renderpass : list) {
				ClientDataHolder.getInstance().currentPass = renderpass;

				switch (renderpass) {
				case LEFT:
				case RIGHT:
					this.mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.framebufferVrRender;
					break;

				case CENTER:
					this.mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.framebufferUndistorted;
					break;

				case THIRD:
					this.mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.framebufferMR;
					break;

				case SCOPEL:
					this.mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferL;
					break;

				case SCOPER:
					this.mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.telescopeFramebufferR;
					break;

				case CAMERA:
					this.mainRenderTarget = ClientDataHolder.getInstance().vrRenderer.cameraRenderFramebuffer;
				}

				this.profiler.push("Eye:" + ClientDataHolder.getInstance().currentPass.ordinal());
				this.profiler.push("setup");
				this.mainRenderTarget.bindWrite(true);
				this.profiler.pop();
				this.renderSingleView(renderpass, f, bl);
				this.profiler.pop();

				if (ClientDataHolder.getInstance().grabScreenShot) {
					boolean flag;

					if (list.contains(RenderPass.CAMERA)) {
						flag = renderpass == RenderPass.CAMERA;
					} else if (list.contains(RenderPass.CENTER)) {
						flag = renderpass == RenderPass.CENTER;
					} else {
						flag = ClientDataHolder.getInstance().vrSettings.displayMirrorLeftEye ? renderpass == RenderPass.LEFT
								: renderpass == RenderPass.RIGHT;
					}

					if (flag) {
						RenderTarget rendertarget = this.mainRenderTarget;

						if (renderpass == RenderPass.CAMERA) {
							rendertarget = ClientDataHolder.getInstance().vrRenderer.cameraFramebuffer;
						}

						this.mainRenderTarget.unbindWrite();
						Utils.takeScreenshot(rendertarget);
						this.window.updateDisplay();
						ClientDataHolder.getInstance().grabScreenShot = false;
					}
				}

				ClientDataHolder.getInstance().isFirstPass = false;
			}

			if (bl) {
				ClientDataHolder.getInstance().vrPlayer.postRender(f);
				this.profiler.push("Display/Reproject");

				try {
					ClientDataHolder.getInstance().vrRenderer.endFrame();
				} catch (Exception exception) {
					//LOGGER.error(exception.toString());
				}

				this.profiler.pop();
				this.checkGLError("post submit ");
			}

			if (!this.noRender) {
				Xevents.onRenderTickEnd(this.pause ? this.pausePartialTick : this.timer.partialTick);
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
		if (ClientDataHolder.getInstance().vrRenderer != null) {
			ClientDataHolder.getInstance().vrRenderer.reinitFrameBuffers("Main Window Changed");
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
		if (ClientDataHolder.getInstance().vrSettings.seated) {
			this.gameMode.stopDestroyBlock();
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"), method = "startUseItem()V")
	public boolean seatedCheck(MultiPlayerGameMode gameMode) {
		return !(!gameMode.isDestroying() || !ClientDataHolder.getInstance().vrSettings.seated);
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;rightClickDelay:I", shift = Shift.AFTER, opcode = Opcodes.PUTFIELD), method = "startUseItem()V")
	public void breakDelay(CallbackInfo info) {
		if (ClientDataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.VANILLA)
			this.rightClickDelay = 4;
		else if (ClientDataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOW)
			this.rightClickDelay = 6;
		else if (ClientDataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWER)
			this.rightClickDelay = 8;
		else if (ClientDataHolder.getInstance().vrSettings.rightclickDelay == VRSettings.RightClickDelay.SLOWEST)
			this.rightClickDelay = 10;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), method = "startUseItem")
	public ItemStack activeHand2(LocalPlayer instance, InteractionHand interactionHand) {
		ItemStack itemInHand = instance.getItemInHand(interactionHand);
		if (ClientDataHolder.getInstance().vrSettings.seated || !TelescopeTracker.isTelescope(itemInHand)) {
			ClientNetworkHelper.sendActiveHand((byte) interactionHand.ordinal());
		}
		this.itemInHand = itemInHand;
		return itemInHand;
	}

	@Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1), method = "startUseItem")
	public HitResult activeHand2(Minecraft instance) {
		if (ClientDataHolder.getInstance().vrSettings.seated || !TelescopeTracker.isTelescope(itemInHand)){
			return instance.hitResult;
		}
		return null;
	}


	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "startUseItem")
	public void swingUse(LocalPlayer instance, InteractionHand interactionHand) {
		((PlayerExtension)instance).swingArm(interactionHand, VRFirstPersonArmSwing.Use);
	}

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void tick(CallbackInfo info) {
		++ClientDataHolder.getInstance().tickCounter;
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
		this.profiler.popPush("vrProcessInputs");
		ClientDataHolder.getInstance().vr.processInputs();
		ClientDataHolder.getInstance().vr.processBindings();
	}

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;", ordinal = 4, shift = Shift.BEFORE), method = "tick")
	public void vrActions(CallbackInfo ci) {
		this.profiler.popPush("vrInputActionsTick");

		for (VRInputAction vrinputaction : ClientDataHolder.getInstance().vr.getInputActions()) {
			vrinputaction.tick();
		}

		if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
			VRHotkeys.handleMRKeys();
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 2, shift = Shift.BEFORE), method = "tick")
	public void freeMove(CallbackInfo ci) {
		if (this.player != null) {
			ClientDataHolder.getInstance().vrPlayer.updateFreeMove();

			if (ClientDataHolder.getInstance().vrPlayer.teleportWarningTimer >= 0 && --ClientDataHolder.getInstance().vrPlayer.teleportWarningTimer == 0) {
				this.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.noserverplugin"));
			}
		}
	}


	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;pause:Z", ordinal = 5, shift = Shift.BEFORE), method = "tick()V")
	public void tickmenu(CallbackInfo info) {
//		if (DataHolder.getInstance().menuWorldRenderer != null) {
//			DataHolder.getInstance().menuWorldRenderer.tick();
//		}

		PlayerModelController.getInstance().tick();

	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"), method = "handleKeybinds")
	public void vrMirrorOption(Options instance, CameraType cameraType) {
		ClientDataHolder.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
		this.notifyMirror(ClientDataHolder.getInstance().vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
		//this.levelRenderer.needsUpdate();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"), method = "handleKeybinds")
	public void noPosEffect(GameRenderer instance, Entity entity) {
		return ;
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"), method = "handleKeybinds()V")
	public void swingArmhandleKeybinds(LocalPlayer instance, InteractionHand interactionHand) {
		((PlayerExtension) player).swingArm(InteractionHand.MAIN_HAND, VRFirstPersonArmSwing.Attack);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2), method = "handleKeybinds")
	public boolean vrKeyuse(KeyMapping instance) {
		return !(!instance.isDown() && (!ClientDataHolder.getInstance().bowTracker.isActive(this.player) || ClientDataHolder.getInstance().vrSettings.seated) && ! ClientDataHolder.getInstance().autoFood.isEating());
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V", shift = Shift.BEFORE), method = "handleKeybinds")
	public void activeHand(CallbackInfo ci) {
		ClientNetworkHelper.sendActiveHand((byte)this.player.getUsedItemHand().ordinal());
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 13), method = "handleKeybinds")
	public boolean notConsumeClick(KeyMapping instance) {
		return false;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;consumeClick()Z", ordinal = 14, shift = Shift.BEFORE), method = "handleKeybinds")
	public void attackDown(CallbackInfo ci) {
		if (this.options.keyAttack.consumeClick() && this.screen == null){
			this.startAttack();
			this.lastClick = true;
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

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"), method = "handleKeybinds")
	public boolean alwaysGrapped(MouseHandler instance) {
		return true;
	}

	@Inject(at = @At("HEAD"), method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V")
	public void roomScale(ClientLevel pLevelClient, CallbackInfo info) {
		ClientDataHolder.getInstance().vrPlayer.setRoomOrigin(0.0D, 0.0D, 0.0D, true);
	}

	private void drawNotifyMirror() {
		if (System.currentTimeMillis() < this.mirroNotifyStart + this.mirroNotifyLen) {
			RenderSystem.viewport(0, 0, this.window.getScreenWidth(), this.window.getScreenHeight());
			Matrix4f matrix4f = Matrix4f.orthographic(0.0F, (float) this.window.getScreenWidth(), 0.0F,
					(float) this.window.getScreenHeight(), 1000.0F, 3000.0F);
			RenderSystem.setProjectionMatrix(matrix4f);
			RenderSystem.getModelViewStack().pushPose();
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
			RenderSystem.getModelViewStack().popPose();
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

	private void renderSingleView(RenderPass eye, float nano, boolean renderworld) {
		GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
		GlStateHelper.clear(16384);
		GlStateManager._enableTexture();
		GlStateManager._enableDepthTest();
		this.profiler.push("updateCameraAndRender");
		this.gameRenderer.render(nano, System.nanoTime(), renderworld);
		this.profiler.pop();
		this.checkGLError("post game render " + eye.name());

		if (ClientDataHolder.getInstance().currentPass == RenderPass.LEFT
				|| ClientDataHolder.getInstance().currentPass == RenderPass.RIGHT) {
			this.profiler.push("postprocesseye");
			RenderTarget rendertarget = this.mainRenderTarget;

			if (ClientDataHolder.getInstance().vrSettings.useFsaa) {
				GlStateManager._clearColor(RenderSystem.getShaderFogColor()[0], RenderSystem.getShaderFogColor()[1], RenderSystem.getShaderFogColor()[2], RenderSystem.getShaderFogColor()[3]);
				if (eye == RenderPass.LEFT) {
					ClientDataHolder.getInstance().vrRenderer.framebufferEye0.bindWrite(true);
				} else {
					ClientDataHolder.getInstance().vrRenderer.framebufferEye1.bindWrite(true);
				}
				GlStateHelper.clear(16384);
				this.profiler.push("fsaa");
				// DataHolder.getInstance().vrRenderer.doFSAA(Config.isShaders()); TODO
				ClientDataHolder.getInstance().vrRenderer.doFSAA(eye, false);
				rendertarget = ClientDataHolder.getInstance().vrRenderer.fsaaLastPassResultFBO;
				this.checkGLError("fsaa " + eye.name());
				this.profiler.pop();
			}

			if (eye == RenderPass.LEFT) {
				ClientDataHolder.getInstance().vrRenderer.framebufferEye0.bindWrite(true);
			} else {
				ClientDataHolder.getInstance().vrRenderer.framebufferEye1.bindWrite(true);
			}

			if (ClientDataHolder.getInstance().vrSettings.useFOVReduction
					&& ClientDataHolder.getInstance().vrPlayer.getFreeMove()) {
				if (this.player != null && (Math.abs(this.player.zza) > 0.0F || Math.abs(this.player.xxa) > 0.0F)) {
					this.fov = (float) ((double) this.fov - 0.05D);

					if (this.fov < ClientDataHolder.getInstance().vrSettings.fovReductionMin) {
						this.fov = ClientDataHolder.getInstance().vrSettings.fovReductionMin;
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

			VRShaders._FOVReduction_OffsetUniform.set(
					ClientDataHolder.getInstance().vrSettings.fovRedutioncOffset);
			float red = 0.0F;
			float black = 0.0F;
			float blue = 0.0F;
			float time = (float) Util.getMillis() / 1000.0F;

			if (this.player != null && this.level != null) {
				if (((GameRendererExtension) this.gameRenderer)
						.isInWater() != ((GameRendererExtension) this.gameRenderer).isInWater()) {
					ClientDataHolder.getInstance().watereffect = 2.3F;
				} else {
					if (((GameRendererExtension) this.gameRenderer).isInWater()) {
						ClientDataHolder.getInstance().watereffect -= 0.008333334F;
					} else {
						ClientDataHolder.getInstance().watereffect -= 0.016666668F;
					}

					if (ClientDataHolder.getInstance().watereffect < 0.0F) {
						ClientDataHolder.getInstance().watereffect = 0.0F;
					}
				}

				((GameRendererExtension) this.gameRenderer)
						.setWasInWater(((GameRendererExtension) this.gameRenderer).isInWater());

				if (Xplat
						.isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
					if (!IrisHelper.hasWaterEffect()) {
						ClientDataHolder.getInstance().watereffect = 0.0F;
					}
				}

				if (((GameRendererExtension) this.gameRenderer).isInPortal()) {
					ClientDataHolder.getInstance().portaleffect = 1.0F;
				} else {
					ClientDataHolder.getInstance().portaleffect -= 0.016666668F;

					if (ClientDataHolder.getInstance().portaleffect < 0.0F) {
						ClientDataHolder.getInstance().portaleffect = 0.0F;
					}
				}

				ItemStack itemstack = this.player.getInventory().getArmor(3);

				if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem()
						&& (!itemstack.hasTag() || itemstack.getTag().getInt("CustomModelData") == 0)) {
					ClientDataHolder.getInstance().pumpkineffect = 1.0F;
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

				if (ClientDataHolder.getInstance().vr.isWalkingAbout && (double) black < 0.8D) {
					black = 0.5F;
				}
			} else {
				ClientDataHolder.getInstance().watereffect = 0.0F;
				ClientDataHolder.getInstance().portaleffect = 0.0F;
				ClientDataHolder.getInstance().pumpkineffect = 0.0F;
			}

			if (ClientDataHolder.getInstance().pumpkineffect > 0.0F) {
				VRShaders._FOVReduction_RadiusUniform.set(0.3F);
				VRShaders._FOVReduction_BorderUniform.set(0.0F);
			} else {
				VRShaders._FOVReduction_RadiusUniform.set(this.fov);
				VRShaders._FOVReduction_BorderUniform.set(0.06F);
			}

			VRShaders._Overlay_HealthAlpha.set(red);
			VRShaders._Overlay_FreezeAlpha.set(blue);
			VRShaders._Overlay_BlackAlpha.set(black);
			VRShaders._Overlay_time.set(time);
			VRShaders._Overlay_waterAmplitude.set(ClientDataHolder.getInstance().watereffect);
			VRShaders._Overlay_portalAmplitutde.set(ClientDataHolder.getInstance().portaleffect);
			VRShaders._Overlay_pumpkinAmplitutde.set(
					ClientDataHolder.getInstance().pumpkineffect);
			RenderPass renderpass = ClientDataHolder.getInstance().currentPass;

			VRShaders._Overlay_eye.set(
					ClientDataHolder.getInstance().currentPass == RenderPass.LEFT ? 1 : -1);
			((RenderTargetExtension) rendertarget).blitFovReduction(VRShaders.fovReductionShader, ClientDataHolder.getInstance().vrRenderer.framebufferEye0.viewWidth,
					ClientDataHolder.getInstance().vrRenderer.framebufferEye0.viewHeight);
			GlStateManager._glUseProgram(0);
			this.checkGLError("post overlay" + eye);
			this.profiler.pop();
		}

		if (ClientDataHolder.getInstance().currentPass == RenderPass.CAMERA) {
			this.profiler.push("cameracopy");
			ClientDataHolder.getInstance().vrRenderer.cameraFramebuffer.bindWrite(true);
			GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
			GlStateHelper.clear(16640);
			((RenderTargetExtension) ClientDataHolder.getInstance().vrRenderer.cameraRenderFramebuffer).blitToScreen(0,
					ClientDataHolder.getInstance().vrRenderer.cameraFramebuffer.viewWidth,
					ClientDataHolder.getInstance().vrRenderer.cameraFramebuffer.viewHeight, 0, true, 0.0F, 0.0F, false);
			this.profiler.pop();
		}
	}

	private void copyToMirror() {
		// TODO: fix mixed reality... again
		if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF
				&& ClientDataHolder.getInstance().vr.isHMDTracking()) {
			this.notifyMirror("Mirror is OFF", true, 1000);
		} else if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
			if (VRShaders.depthMaskShader != null) {
				this.doMixedRealityMirror();
			} else {
				this.notifyMirror("Shader compile failed, see log", true, 10000);
			}
		} else if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.DUAL) {
			RenderTarget rendertarget = ClientDataHolder.getInstance().vrRenderer.framebufferEye0;
			RenderTarget rendertarget1 = ClientDataHolder.getInstance().vrRenderer.framebufferEye1;

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
			RenderTarget source = ClientDataHolder.getInstance().vrRenderer.framebufferEye0;

			if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
				source = ClientDataHolder.getInstance().vrRenderer.framebufferUndistorted;
			} else if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
				source = ClientDataHolder.getInstance().vrRenderer.framebufferMR;
			} else if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.SINGLE
					|| ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF) {
				if (!ClientDataHolder.getInstance().vrSettings.displayMirrorLeftEye)
					source = ClientDataHolder.getInstance().vrRenderer.framebufferEye1;
			} else if (ClientDataHolder.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.CROPPED) {
				if (!ClientDataHolder.getInstance().vrSettings.displayMirrorLeftEye)
					source = ClientDataHolder.getInstance().vrRenderer.framebufferEye1;

				xcrop = 0.15F;
				ycrop = 0.15F;
				ar = true;
			}
			// Debug
			// source = GuiHandler.guiFramebuffer;
			// source = DataHolder.getInstance().vrRenderer.telescopeFramebufferR;
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
		boolean flag1 = ClientDataHolder.getInstance().vrSettings.mixedRealityUnityLike
				&& ClientDataHolder.getInstance().vrSettings.mixedRealityAlphaMask;

		if (!flag1) {
			GlStateManager._clearColor(
					(float) ClientDataHolder.getInstance().vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
					(float) ClientDataHolder.getInstance().vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
					(float) ClientDataHolder.getInstance().vrSettings.mixedRealityKeyColor.getBlue() / 255.0F, 1.0F);
		} else {
			GlStateManager._clearColor(0.0F, 0.0F, 0.0F, 1.0F);
		}

		GlStateHelper.clear(16640);
		Vec3 vec3 = ClientDataHolder.getInstance().vrPlayer.vrdata_room_pre.getHeadPivot()
				.subtract(ClientDataHolder.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getPosition());
		Matrix4f matrix4f = ClientDataHolder.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD)
				.getMatrix().transposed().toMCMatrix();
		Vector3 vector3 = ClientDataHolder.getInstance().vrPlayer.vrdata_room_pre.getEye(RenderPass.THIRD).getMatrix()
				.transform(Vector3.forward());
		VRShaders._DepthMask_projectionMatrix.set(((GameRendererExtension) this.gameRenderer).getThirdPassProjectionMatrix());
		VRShaders._DepthMask_viewMatrix.set(matrix4f);
		VRShaders._DepthMask_hmdViewPosition.set((float) vec3.x, (float) vec3.y,
				(float) vec3.z);
		VRShaders._DepthMask_hmdPlaneNormal.set( -vector3.getX(), 0.0F, -vector3.getZ());
		VRShaders._DepthMask_keyColorUniform.set(
				(float) ClientDataHolder.getInstance().vrSettings.mixedRealityKeyColor.getRed() / 255.0F,
				(float) ClientDataHolder.getInstance().vrSettings.mixedRealityKeyColor.getGreen() / 255.0F,
				(float) ClientDataHolder.getInstance().vrSettings.mixedRealityKeyColor.getBlue() / 255.0F);
		VRShaders._DepthMask_alphaModeUniform.set(flag1 ? 1 : 0);
		GlStateManager._activeTexture(33985);
		RenderSystem.setShaderTexture(0, ClientDataHolder.getInstance().vrRenderer.framebufferMR.getColorTextureId());
		GlStateManager._activeTexture(33986);

//		if (flag && Shaders.dfb != null) { TODO
//			GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(0));
//		} else {
		RenderSystem.setShaderTexture(1, ClientDataHolder.getInstance().vrRenderer.framebufferMR.getDepthTextureId());

//		}

		GlStateManager._activeTexture(33984);

		for (int i = 0; i < (flag1 ? 3 : 2); ++i) {
			int j = this.window.getScreenWidth() / 2;
			int k = this.window.getScreenHeight();
			int l = this.window.getScreenWidth() / 2 * i;
			int i1 = 0;

			if (ClientDataHolder.getInstance().vrSettings.mixedRealityUnityLike) {
				j = this.window.getScreenWidth() / 2;
				k = this.window.getScreenHeight() / 2;

				if (ClientDataHolder.getInstance().vrSettings.mixedRealityAlphaMask && i == 2) {
					l = this.window.getScreenWidth() / 2;
					i1 = this.window.getScreenHeight() / 2;
				} else {
					l = 0;
					i1 = this.window.getScreenHeight() / 2 * (1 - i);
				}
			}

			VRShaders._DepthMask_resolutionUniform.set((float) j, (float) k);
			VRShaders._DepthMask_positionUniform.set((float) l, (float) i1);
			VRShaders._DepthMask_passUniform.set(i);
			((RenderTargetExtension) ClientDataHolder.getInstance().vrRenderer.framebufferMR).blitToScreen(VRShaders.depthMaskShader, l, j, k, i1, true,
					0.0F, 0.0F, false);
		}

		GlStateManager._glUseProgram(0);

		if (ClientDataHolder.getInstance().vrSettings.mixedRealityUnityLike) {
			if (ClientDataHolder.getInstance().vrSettings.mixedRealityUndistorted) {
				((RenderTargetExtension) ClientDataHolder.getInstance().vrRenderer.framebufferUndistorted).blitToScreen(
						this.window.getScreenWidth() / 2, this.window.getScreenWidth() / 2,
						this.window.getScreenHeight() / 2, 0, true, 0.0F, 0.0F, false);
			} else {
				((RenderTargetExtension) ClientDataHolder.getInstance().vrRenderer.framebufferEye0).blitToScreen(
						this.window.getScreenWidth() / 2, this.window.getScreenWidth() / 2,
						this.window.getScreenHeight() / 2, 0, true, 0.0F, 0.0F, false);
			}
		}
	}

}
