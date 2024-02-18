package org.vivecraft.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client.gui.screens.GarbageCollectorScreen;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.nullvr.NullVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;

public class VRState {

    public static boolean vrRunning = false;
    public static boolean vrEnabled = false;
    public static boolean vrInitialized = false;

    public static void initializeVR() {
        if (vrInitialized) {
            return;
        }
        try {
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isAntialiasing()) {
                throw new RenderConfigException(Component.translatable("vivecraft.messages.incompatiblesettings").getString(), Component.translatable("vivecraft.messages.optifineaa"));
            }

            vrInitialized = true;
            ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
            if (dh.vrSettings.stereoProviderPluginID == VRSettings.VRProvider.OPENVR) {
                dh.vr = new MCOpenVR(Minecraft.getInstance(), dh);
            } else {
                dh.vr = new NullVR(Minecraft.getInstance(), dh);
            }
            if (!dh.vr.init()) {
                throw new RenderConfigException("VR Init Error", Component.translatable("vivecraft.messages.rendersetupfailed", dh.vr.initStatus, dh.vr.getName()));
            }

            dh.vrRenderer = dh.vr.createVRRenderer();
            dh.vrRenderer.lastGuiScale = Minecraft.getInstance().options.guiScale().get();

            dh.vrRenderer.setupRenderConfiguration();
            RenderPassManager.setVanillaRenderPass();

            dh.vrPlayer = new VRPlayer();

            dh.vr.postinit();

            dh.menuWorldRenderer = new MenuWorldRenderer();

            dh.menuWorldRenderer.init();

            try {
                String garbageCollector = StringUtils.getCommonPrefix(ManagementFactory.getGarbageCollectorMXBeans().stream().map(MemoryManagerMXBean::getName).toArray(String[]::new)).trim();
                if (garbageCollector.isEmpty()) {
                    garbageCollector = ManagementFactory.getGarbageCollectorMXBeans().get(0).getName();
                }
                VRSettings.logger.info("Garbage collector: {}", garbageCollector);

                // Fully qualified name here to avoid any ambiguity
                com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                // Might as well log this stuff since we have it, could be useful for technical support
                VRSettings.logger.info("Available CPU threads: {}", Runtime.getRuntime().availableProcessors());
                VRSettings.logger.info("Total physical memory: {} GiB", String.format("%.01f", os.getTotalMemorySize() / 1073741824.0F));
                VRSettings.logger.info("Free physical memory: {} GiB", String.format("%.01f", os.getFreeMemorySize() / 1073741824.0F));

                if (!garbageCollector.equals("ZGC") && !ClientDataHolderVR.getInstance().vrSettings.disableGarbageCollectorMessage) {
                    // At least 12 GiB RAM (minus 256 MiB for possible reserved) and 8 CPU threads
                    if (os.getTotalMemorySize() >= 1073741824L * 12L - 1048576L * 256L && Runtime.getRuntime().availableProcessors() >= 6) {
                        Minecraft.getInstance().setScreen(new GarbageCollectorScreen(garbageCollector));
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } catch (RenderConfigException renderConfigException) {
            vrEnabled = false;
            destroyVR(true);
            Minecraft.getInstance().setScreen(new ErrorScreen(renderConfigException.title, renderConfigException.error));
        } catch (Throwable e) {
            vrEnabled = false;
            destroyVR(true);
            e.printStackTrace();
            MutableComponent component = Component.literal(e.getClass().getName() + (e.getMessage() == null ? "" : ": " + e.getMessage()));
            for (StackTraceElement element : e.getStackTrace()) {
                component.append(Component.literal("\n" + element.toString()));
            }
            Minecraft.getInstance().setScreen(new ErrorScreen("VR Init Error", component));
        }
    }

    public static void startVR() {
        GLFW.glfwSwapInterval(0);
    }

    public static void destroyVR(boolean disableVRSetting) {
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

    public static void pauseVR() {
        //        GLFW.glfwSwapInterval(bl ? 1 : 0);
    }
}
