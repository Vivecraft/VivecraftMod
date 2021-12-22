package com.example.examplemod.mixin.client;

import java.io.File;
import java.util.Optional;
import java.util.function.Consumer;

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
import org.vivecraft.menuworlds.MenuWorldRenderer;
import org.vivecraft.provider.openvr_jna.MCOpenVR;
import org.vivecraft.provider.openvr_jna.OpenVRStereoRenderer;
import org.vivecraft.provider.ovr_lwjgl.MC_OVR;
import org.vivecraft.provider.ovr_lwjgl.OVR_StereoRenderer;
import org.vivecraft.settings.VRSettings;

import com.example.examplemod.DataHolder;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Minecraft.class)
public abstract class MinecraftVRMixin {

	@Unique
	private boolean oculus;

	@Shadow
	@Final
	public File gameDirectory;

	@Shadow
	private Options options;

	private Screen screen;

	@Shadow
	abstract void selectMainFont(boolean p_91337_);

	@Shadow
	abstract void resizeDisplay();

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
	
	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;ifElse(Ljava/util/Optional;Ljava/util/function/Consumer;Ljava/lang/Runnable;)Ljava/util/Optional;"), 
			method = "<init>(Lnet/minecraft/client/main/GameConfig;)V")
	public Optional<Throwable> menuInit(Optional<Throwable> p_137522_, Consumer<Throwable> p_137523_, Runnable p_137524_) {
		if (DataHolder.getInstance().vrRenderer.isInitialized()) {
			DataHolder.getInstance().menuWorldRenderer.init();
		}
		DataHolder.getInstance().vr.postinit();
		return Util.ifElse(p_137522_, p_137523_, p_137524_);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;ifElse(Ljava/util/Optional;Ljava/util/function/Consumer;Ljava/lang/Runnable;)Ljava/util/Optional;"), 
			method = "reloadResourcePacks(Z)Ljava/util/concurrent/CompletableFuture;")
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
	
	@Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", shift = Shift.BEFORE), 
			method = "setScreen(Lnet/minecraft/client/gui/screens/Screen;)V")
	public void gui(Screen pGuiScreen, CallbackInfo info) {
		GuiHandler.onScreenChanged(this.screen, pGuiScreen, true);
	}

}
