package org.vivecraft.mod_compat_vr.iris;

import net.irisshaders.iris.api.v0.IrisApi;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class IrisHelper {

    private static boolean initialized = false;
    private static boolean initFailed = false;

    private static Method Iris_reload;
    private static Method Iris_getPipelineManager;
    private static Method PipelineManager_getPipeline;
    private static Method WorldRenderingPipeline_shouldRenderUnderwaterOverlay;

    public static void setShadersActive(boolean bl) {
        IrisApi.getInstance().getConfig().setShadersEnabledAndApply(bl);
    }

    public static void reload() {
        RenderPassManager.setVanillaRenderPass();
        if (init()) {
            try {
                // Iris.reload();
                Iris_reload.invoke(null);
            } catch (Exception e) {
                // catch Exception, because that call can throw an IOException
                VRSettings.logger.error("Vivecraft: Error reloading Iris shaders on Frame Buffer reinit: {}", e.getMessage());
            }
        }
    }

    public static boolean hasWaterEffect() {
        if (init()) {
            try {
                // Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderUnderwaterOverlay).orElse(true);
                return (boolean) ((Optional<?>) PipelineManager_getPipeline.invoke(Iris_getPipelineManager.invoke(null))).map(o -> {
                    try {
                        return WorldRenderingPipeline_shouldRenderUnderwaterOverlay.invoke(o);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        VRSettings.logger.error("Vivecraft: Iris water effect check failed: {}", e.getMessage());
                        return true;
                    }
                }).orElse(true);
            } catch (InvocationTargetException | IllegalAccessException e) {
                VRSettings.logger.error("Vivecraft: Iris water effect check failed: {}", e.getMessage());
            }
        }
        return true;
    }

    public static boolean isShaderActive() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    private static boolean init() {
        if (initialized) {
            return !initFailed;
        }
        try {
            Class<?> iris = getClassWithAlternative(
                "net.coderbot.iris.Iris",
                "net.irisshaders.iris.Iris");
            Iris_reload = iris.getMethod("reload");
            Iris_getPipelineManager = iris.getMethod("getPipelineManager");

            Class<?> pipelineManager = getClassWithAlternative(
                "net.coderbot.iris.pipeline.PipelineManager",
                "net.irisshaders.iris.pipeline.PipelineManager");

            PipelineManager_getPipeline = pipelineManager.getMethod("getPipeline");

            Class<?> worldRenderingPipeline = getClassWithAlternative(
                "net.coderbot.iris.pipeline.WorldRenderingPipeline",
                "net.irisshaders.iris.pipeline.WorldRenderingPipeline");

            WorldRenderingPipeline_shouldRenderUnderwaterOverlay = worldRenderingPipeline.getMethod("shouldRenderUnderwaterOverlay");
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            initFailed = true;
        }

        initialized = true;
        return !initFailed;
    }

    private static Class<?> getClassWithAlternative(String class1, String class2) throws ClassNotFoundException {
        try {
            return Class.forName(class1);
        } catch (ClassNotFoundException e) {
            return Class.forName(class2);
        }
    }
}
