package org.vivecraft.client_vr;

import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.nullvr.NullVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

/**
 * The VR State Controller.
 * <br>
 * Holds global toggles and other data holders.
 */
public class VRState {

    public static boolean vrRunning = false;
    public static boolean vrEnabled = false;
    public static boolean vrInitialized = false;
    /**
     * The MC Data Holder.
     * @apiNote Intended for use internal to Vivecraft.
     * <br>
     * Other mods should {@link net.minecraft.client.Minecraft#getInstance()} instead.
     * @see org.vivecraft.mixin.client_vr.MinecraftVRMixin#initVivecraft(net.minecraft.client.gui.screens.Overlay)
     */
    @Nonnull public static net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
    /**
     * The VR Data Holder.
     * @see org.vivecraft.mixin.client_vr.MinecraftVRMixin#initVivecraft(net.minecraft.client.gui.screens.Overlay)
     */
    @Nonnull public static final ClientDataHolderVR dh = new ClientDataHolderVR();

    public static void initializeVR() {
        if (vrInitialized) {
            return;
        }
        try {
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isAntialiasing()) {
                throw new RenderConfigException(Component.translatable("vivecraft.messages.incompatiblesettings").getString(), Component.translatable("vivecraft.messages.optifineaa"));
            }

            vrInitialized = true;
            dh.vr = switch (dh.vrSettings.stereoProviderPluginID) {
                case OPENVR -> new MCOpenVR();
                default -> new NullVR();
            };
            if (dh.vr.init()) {
                dh.vrRenderer = dh.vr.createVRRenderer();
                dh.vrRenderer.lastGuiScale = mc.options.guiScale().get();
                try
                {
                    dh.vrRenderer.setupRenderConfiguration();
                    RenderPassManager.setVanillaRenderPass();
                } catch (RenderConfigException renderConfigException)
                {
                    throw new RenderConfigException("VR Render Error", Component.translatable("vivecraft.messages.rendersetupfailed", renderConfigException.error.getString() + "\nVR provider: " + dh.vr.getName()));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }

                dh.vrPlayer = new VRPlayer();
                dh.vrPlayer.registerTracker(dh.backpackTracker);
                dh.vrPlayer.registerTracker(dh.bowTracker);
                dh.vrPlayer.registerTracker(dh.climbTracker);
                dh.vrPlayer.registerTracker(dh.eatingTracker);
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

                dh.menuWorldRenderer = new MenuWorldRenderer();

                dh.menuWorldRenderer.init();
            }
            else
            {
                throw new RenderConfigException("VR init Error", Component.translatable("vivecraft.messages.rendersetupfailed", dh.vr.initStatus + "\nVR provider: " + dh.vr.getName()));
            }
        } catch (RenderConfigException renderConfigException) {
            vrEnabled = false;
            destroyVR(true);
            mc.setScreen(new ErrorScreen(renderConfigException.title, renderConfigException.error));
        }
    }

    public static void destroyVR(boolean disableVRSetting) {
        if (dh.vr != null) {
            dh.vr.destroy();
        }
        dh.vr = null;
        dh.vrPlayer = null;
        if (dh.vrRenderer != null) {
            dh.vrRenderer.destroy();
        }
        dh.vrRenderer = null;
        if (dh.menuWorldRenderer != null) {
            dh.menuWorldRenderer.completeDestroy();
            dh.menuWorldRenderer = null;
        }
        vrEnabled = false;
        vrInitialized = false;
        vrRunning = false;
        if (disableVRSetting) {
            dh.vrSettings.vrEnabled = false;
            dh.vrSettings.saveOptions();
        }
    }
}
