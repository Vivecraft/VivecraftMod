package com.example.examplemod.mixin.client;

import java.io.File;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.gameplay.VRPlayer;
import org.vivecraft.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.menuworlds.MenuWorldRenderer;
import org.vivecraft.provider.openvr_jna.MCOpenVR;
import org.vivecraft.provider.openvr_jna.OpenVRStereoRenderer;
import org.vivecraft.provider.ovr_lwjgl.MC_OVR;
import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;
import org.vivecraft.settings.VRSettings;

import com.example.examplemod.DataHolder;
import com.example.examplemod.NewMinecraftExtension;
import com.example.examplemod.Render;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.Util;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.Timer;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;

@Mixin(Minecraft.class)
public abstract class NewMineCraftVRMixin extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler, NewMinecraftExtension {

	public NewMineCraftVRMixin(String string) {
		super(string);
		// TODO Auto-generated constructor stub
	}

	@Shadow
	private Window window;
	@Shadow
	private CompletableFuture<Void> pendingReload;
	@Shadow
	private ProfilerFiller profiler;
	@Shadow
	private Timer timer;
	@Shadow
	private Overlay overlay;
	@Shadow
	private Queue<Runnable> progressTasks;
	@Shadow
	private MouseHandler mouseHandler;
	@Shadow
	private SoundManager soundManager;
	@Shadow
	private GameRenderer gameRenderer;
	@Shadow
	private RenderTarget mainRenderTarget;
	@Shadow
	private boolean noRender;
	@Shadow
	private boolean pause;
	@Shadow
	private float pausePartialTick;
	@Shadow
	private ToastComponent toast;
	@Shadow
	private ProfileResults fpsPieResults;
	@Shadow
	private int frames;
	@Shadow
	private Screen screen;
	@Shadow
	private long lastNanoTime;
	@Shadow
	private FrameTimer frameTimer;
	@Shadow
	private String fpsString;
	@Shadow
	private long lastTime;
	@Shadow
	private Options options;
	@Shadow
	private IntegratedServer singleplayerServer;
	
	private DataHolder dataHolder = DataHolder.getInstance();
	@Shadow
	private PackRepository resourcePackRepository;
	private boolean oculus;
	@Shadow
	private File gameDirectory;

	@Shadow
	protected abstract void tick();

	@Shadow
	protected abstract void stop();

	@Shadow
	protected abstract CompletableFuture<Void> reloadResourcePacks();

	@Shadow
	protected abstract void renderFpsMeter(PoseStack poseStack, ProfileResults profileResults);

	@Shadow
	protected abstract boolean hasSingleplayerServer();

	@Shadow
	protected abstract int getFramerateLimit();
	
	@Redirect(at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;", remap = false), method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
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

	@Shadow
	protected abstract void selectMainFont(boolean b);

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
	
	@Overwrite
	private void rollbackResourcePacks(Throwable pThrowable) {
		if (this.resourcePackRepository.getSelectedPacks().stream().anyMatch(e -> !e.isRequired())) {
			TextComponent component;
			if (pThrowable instanceof SimpleReloadableResourceManager.ResourcePackLoadingFailure) {
				component = new TextComponent(((SimpleReloadableResourceManager.ResourcePackLoadingFailure)pThrowable).getPack().getName());
			} else {
				component = null;
			}

			this.clearResourcePacksOnError(pThrowable, component);
		} else {
			Util.throwAsRuntime(pThrowable);
		}

	}
	
	@Shadow
	protected abstract void clearResourcePacksOnError(Throwable throwable, @Nullable Component component);

	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE), method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
	public void gui(Screen pGuiScreen, CallbackInfo info) {
		GuiHandler.onScreenChanged(this.screen, pGuiScreen, true);
	}

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Ljava/lang/System;nanoTime()J", remap = false), method = "destroy()V")
	public void destroy(CallbackInfo info) {
		try {
			DataHolder.getInstance().vr.destroy();
		} catch (Exception exception) {
		}
	}
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runTick(Z)V"), method = "run")
    void loop(Minecraft minecraftClient, boolean tick) {
		Render r = new Render();
		r.renderTick();
	}

	@Override
	public void preRender(boolean tick) {
		this.window.setErrorSection("Pre render");
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

		Runnable runnable;
		while ((runnable = this.progressTasks.poll()) != null) {
			runnable.run();
		}
		if (tick) {
			int i = this.timer.advanceTime(Util.getMillis());
			this.profiler.push("scheduledExecutables");
			this.runAllTasks();
			this.profiler.pop();
			this.profiler.push("tick");
			for (int j = 0; j < Math.min(10, i); ++j) {
				this.profiler.incrementCounter("clientTick");
				this.tick();
			}
			this.profiler.pop();
		}
	}

	@Override
	public void doRender(boolean tick, long frameStartTime) {
		this.profiler.push("render");
		PoseStack i = RenderSystem.getModelViewStack();
		i.pushPose();
		RenderSystem.applyModelViewMatrix();
		RenderSystem.clear(16640, Minecraft.ON_OSX);
		this.mainRenderTarget.bindWrite(true);
		FogRenderer.setupNoFog();
		this.profiler.push("display");
		RenderSystem.enableTexture();
		RenderSystem.enableCull();
		this.profiler.pop();
		if (!this.noRender) {
			this.profiler.popPush("gameRenderer");
			this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, frameStartTime, tick);
			this.profiler.popPush("toasts");
			this.toast.render(new PoseStack());
			this.profiler.pop();
		}
		if (this.fpsPieResults != null) {
			this.profiler.push("fpsPie");
			this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
			this.profiler.pop();
		}
		this.profiler.push("blit");
		this.mainRenderTarget.unbindWrite();
		i.popPose();
		i.pushPose();
		RenderSystem.applyModelViewMatrix();
		this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
		i.popPose();
		RenderSystem.applyModelViewMatrix();
		this.profiler.popPush("updateDisplay");
		this.window.updateDisplay();
		int j = this.getFramerateLimit();
		if ((double) j < Option.FRAMERATE_LIMIT.getMaxValue()) {
			RenderSystem.limitDisplayFPS(j);
		}
		this.profiler.popPush("yield");
		Thread.yield();
		this.profiler.pop();
	}

	@Override
	public void posRender(boolean tick) {
		this.window.setErrorSection("Post render");
		++this.frames;
		boolean bl3 = tick = this.hasSingleplayerServer()
				&& (this.screen != null && this.screen.isPauseScreen()
						|| this.overlay != null && this.overlay.isPauseScreen())
				&& !this.singleplayerServer.isPublished();
		if (this.pause != tick) {
			if (this.pause) {
				this.pausePartialTick = this.timer.partialTick;
			} else {
				this.timer.partialTick = this.pausePartialTick;
			}
			this.pause = tick;
		}
		long m = Util.getNanos();
		this.frameTimer.logFrameDuration(m - this.lastNanoTime);
		this.lastNanoTime = m;
		this.profiler.push("fpsUpdate");
		while (Util.getMillis() >= this.lastTime + 1000L) {
			int fps = this.frames;
			this.fpsString = String.format("%d fps T: %s%s%s%s B: %d", fps,
					(double) this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf"
							: Integer.valueOf(this.options.framerateLimit),
					this.options.enableVsync ? " vsync" : "", this.options.graphicsMode.toString(),
					this.options.renderClouds == CloudStatus.OFF ? ""
							: (this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
					this.options.biomeBlendRadius);
			this.lastTime += 1000L;
			this.frames = 0;
		}
		this.profiler.pop();
	}

}
