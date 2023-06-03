package org.vivecraft.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client_vr.provider.nullvr.NullVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;

public class VRState {

    public static boolean vrRunning = false;
    public static boolean vrEnabled = false;
    public static boolean vrInitialized = false;

    public static void initializeVR() {
        if (vrInitialized) {
            return;
        }
        vrInitialized = true;
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        if (dh.vrSettings.stereoProviderPluginID == VRSettings.VRProvider.OPENVR) {
            dh.vr = new MCOpenVR(Minecraft.getInstance(), dh);
        } else {
            dh.vr = new NullVR(Minecraft.getInstance(), dh);
        }
        if (!dh.vr.init()) {
            Minecraft.getInstance().setScreen(new ErrorScreen("VR init Error", Component.translatable("vivecraft.messages.rendersetupfailed", dh.vr.initStatus + "\nVR provider: " + dh.vr.getName())));
            vrEnabled = false;
            destroyVR();
            return;
        }

        dh.vrRenderer = dh.vr.createVRRenderer();
        dh.vrRenderer.lastGuiScale = Minecraft.getInstance().options.guiScale().get();
        try {
            dh.vrRenderer.setupRenderConfiguration();
            RenderPassManager.setVanillaRenderPass();
        } catch(RenderConfigException renderConfigException) {
            Minecraft.getInstance().setScreen(new ErrorScreen("VR Render Error", Component.translatable("vivecraft.messages.rendersetupfailed", renderConfigException.error + "\nVR provider: " + dh.vr.getName())));
            vrEnabled = false;
            destroyVR();
            return;
        } catch(Exception e) {
            e.printStackTrace();
        }

        dh.vrPlayer = new VRPlayer();
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
        dh.vrPlayer.registerTracker(dh.crawlTracker);
        dh.vrPlayer.registerTracker(dh.cameraTracker);

        dh.vr.postinit();
    }

    public static void startVR() {
        GLFW.glfwSwapInterval(0);
    }

    public static void destroyVR() {
        ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
        if (dh.vr != null) {
            dh.vr.destroy();
        }
        dh.vr = null;
        dh.vrPlayer = null;
        if (dh.vrRenderer != null) {
            dh.vrRenderer.destroy();
        }
        dh.vrRenderer = null;
        vrInitialized = false;
        vrRunning = false;
    }

    public static void pauseVR() {
        //        GLFW.glfwSwapInterval(bl ? 1 : 0);
    }
}
