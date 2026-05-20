package org.vivecraft.mod_compat_vr.iris;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.vivecraft.Xloader;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.common.utils.ClassUtils;
import org.vivecraft.mod_compat_vr.shaders.ShaderType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class IrisHelper {

    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Method Iris_reload;
    private static Method Iris_getPipelineManager;
    private static Method PipelineManager_getPipeline;
    private static Method WorldRenderingPipeline_shouldRenderUnderwaterOverlay;

    private static Field ImmediateState_skipExtension;
    private static Method ImmediateState_skipExtension_set;
    private static Method ImmediateState_skipExtension_get;

    private static Class IrisRenderingPipeline;
    private static Field IrisRenderingPipeline_shaderStorageBufferHolder;
    private static Method ShaderStorageBufferHolder_setupBuffers;
    private static RenderPass lastSSBOPass;

    private static Method IrisPipelines_assignPipeline;
    private static Object ShaderKey_ENTITIES_SOLID;
    private static Object ShaderKey_ENTITIES_CUTOUT;
    private static Object ShaderKey_ENTITIES_TRANSLUCENT;
    private static Object ShaderKey_TEXTURED_COLOR;
    private static Object ShaderKey_BASIC_COLOR;

    // for iris/dh compat
    private static boolean DH_PRESENT = false;
    private static Object dhOverrideInjector;
    private static Method OverrideInjector_unbind;

    private static Class<?> IDhApiFramebuffer;
    private static Method Pipeline_getDHCompat;
    private static Method DHCompatInternal_getInstance;
    private static Method DHCompatInternal_getShadowFBWrapper;
    private static Method DHCompatInternal_getSolidFBWrapper;

    // DH 2.2+
    private static Class<?> IDhApiGenericObjectShaderProgram;
    private static Method DHCompatInternal_getGenericShader;

    private static Method CapturedRenderingState_getGbufferProjection;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("iris") || Xloader.INSTANCE.isModLoaded("oculus");
    }

    /**
     * @return if a shaderpack is in use
     */
    public static boolean isShaderActive() {
        return IrisApi.getInstance().isShaderPackInUse();
    }

    /**
     * @return if shaders are currently rendering the shadow pass
     */
    public static boolean isRenderingShadows() {
        return IrisApi.getInstance().isRenderingShadowPass();
    }

    /**
     * enabled or disables shaders
     *
     * @param enabled if shaders should be on or off
     */
    public static void setShadersActive(boolean enabled) {
        IrisApi.getInstance().getConfig().setShadersEnabledAndApply(enabled);
    }

    /**
     * @return if the currently loaded iris version has issues with building menuworlds while shaders are enabled
     */
    public static boolean hasIssuesWithMenuWorld() {
        return false;
    }

    /**
     * triggers a shader reload
     */
    public static void reload() {
        RenderPassManager.setVanillaRenderPass();
        if (init()) {
            try {
                // Iris.reload();
                Iris_reload.invoke(null);
            } catch (Exception e) {
                // catch Exception, because that call can throw an IOException
                VRSettings.LOGGER.error("Vivecraft: Error reloading Iris shaders on Frame Buffer reinit:", e);
            }
        }
    }

    public static Optional<?> getPipeline() {
        if (init()) {
            try {
                return (Optional<?>) PipelineManager_getPipeline.invoke(Iris_getPipelineManager.invoke(null));
            } catch (InvocationTargetException | IllegalAccessException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't get iris pipeline:", e);
            }
        }
        return Optional.empty();
    }

    public static Object getPipelineManager() {
        if (init()) {
            try {
                return Iris_getPipelineManager.invoke(null);
            } catch (InvocationTargetException | IllegalAccessException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't get iris pipeline manager:", e);
            }
        }
        return null;
    }

    /**
     * @return if the active shader has the vanilla water overlay enabled or disabled
     */
    public static boolean hasWaterEffect() {
        if (init()) {
            // Iris.getPipelineManager().getPipeline().map(WorldRenderingPipeline::shouldRenderUnderwaterOverlay).orElse(true);
            return (boolean) getPipeline().map(o -> {
                try {
                    return WorldRenderingPipeline_shouldRenderUnderwaterOverlay.invoke(o);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    VRSettings.LOGGER.error("Vivecraft: Iris water effect check failed:", e);
                    return true;
                }
            }).orElse(true);
        }
        return true;
    }

    /**
     * removes the DH overrides from the given {@code pipeline}
     * this is here, because iris doesn't do that on pipeline changes
     *
     * @param pipeline Rendering pileple to uinregister the overrides for
     */
    public static void unregisterDHIfThere(Object pipeline) {
        if (init() && DH_PRESENT) {
            try {
                Object dhCompat = Pipeline_getDHCompat.invoke(pipeline);
                // check if the shader even has a dh part
                if (dhCompat != null) {
                    Object dhCompatInstance = DHCompatInternal_getInstance.invoke(dhCompat);
                    if (dhCompatInstance != null) {
                        // now disable the overrides
                        OverrideInjector_unbind.invoke(dhOverrideInjector, IDhApiFramebuffer,
                            DHCompatInternal_getShadowFBWrapper.invoke(dhCompatInstance));
                        OverrideInjector_unbind.invoke(dhOverrideInjector, IDhApiFramebuffer,
                            DHCompatInternal_getSolidFBWrapper.invoke(dhCompatInstance));
                        // generic override for DH 2.2+
                        if (DHCompatInternal_getGenericShader != null) {
                            OverrideInjector_unbind.invoke(dhOverrideInjector, IDhApiGenericObjectShaderProgram,
                                DHCompatInternal_getGenericShader.invoke(dhCompatInstance));
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("Vivecraft: Iris DH reset failed", e);
            }
        }
    }

    /**
     * needed, because some Iris versions return a Matrix4f and others a Matrix4fc, which causes a runtime exception
     *
     * @param source CapturedRenderingState INSTANCE to call this on
     * @return Matrix4fc current projection matrix
     */
    public static Matrix4fc getGbufferProjection(Object source) {
        if (init() && DH_PRESENT) {
            try {
                return (Matrix4fc) CapturedRenderingState_getGbufferProjection.invoke(source);
            } catch (IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't get iris gbuffer projection matrix:", e);
            }
        }
        return new Matrix4f();
    }

    public static void swapSSBOs(Object newPipeline, RenderPass newPass) {
        if (init() && IrisRenderingPipeline_shaderStorageBufferHolder != null &&
            ShaderStorageBufferHolder_setupBuffers != null && IrisRenderingPipeline != null &&
            IrisRenderingPipeline.isInstance(newPipeline) && newPass != lastSSBOPass)
        {
            try {
                Object ssbos = IrisRenderingPipeline_shaderStorageBufferHolder.get(newPipeline);
                if (ssbos != null) {
                    ShaderStorageBufferHolder_setupBuffers.invoke(ssbos);
                }
                lastSSBOPass = newPass;
            } catch (IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't swap iris ssbos:", e);
            }
        }
    }

    public static void registerPipeline(RenderPipeline pipeline, ShaderType type) {
        if (init() && IrisPipelines_assignPipeline != null) {
            try {
                IrisPipelines_assignPipeline.invoke(null, pipeline,
                    switch (type) {
                        case ENTITIES_CUTOUT -> ShaderKey_ENTITIES_CUTOUT;
                        case ENTITIES_SOLID -> ShaderKey_ENTITIES_SOLID;
                        case ENTITIES_TRANSLUCENT -> ShaderKey_ENTITIES_TRANSLUCENT;
                        case BASIC_COLOR -> ShaderKey_BASIC_COLOR;
                        case TEXTURED_COLOR -> ShaderKey_TEXTURED_COLOR;
                    });
                return;
            } catch (IllegalAccessException | InvocationTargetException ignore) {
                VRSettings.LOGGER.error("Vivecraft: couldn't assign pipeline {} to type '{}'", pipeline, type);
            }
        }

        // fallback to regular api if something failed
        IrisProgram program = IrisProgram.valueOf(type.name());
        IrisApi.getInstance().assignPipeline(pipeline, program);
    }

    public static void setSkipBufferExtension(boolean enabled) {
        if (init()) {
            try {
                ImmediateState_skipExtension_set.invoke(ImmediateState_skipExtension.get(null), enabled);
            } catch (IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't set iris buffer extension:", e);
            }
        }
    }

    public static boolean getSkipBufferExtension() {
        if (init()) {
            try {
                return (boolean) ImmediateState_skipExtension_get.invoke(ImmediateState_skipExtension.get(null));
            } catch (IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("Vivecraft: couldn't set iris buffer extension:", e);
            }
        }
        return false;
    }

    /**
     * initializes all Reflections
     *
     * @return if init was successful
     */
    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_FAILED;
        }
        try {
            Class<?> iris = ClassUtils.getClassWithAlternative(
                "net.coderbot.iris.Iris",
                "net.irisshaders.iris.Iris");
            Iris_reload = iris.getMethod("reload");
            Iris_getPipelineManager = iris.getMethod("getPipelineManager");

            Class<?> pipelineManager = ClassUtils.getClassWithAlternative(
                "net.coderbot.iris.pipeline.PipelineManager",
                "net.irisshaders.iris.pipeline.PipelineManager");

            PipelineManager_getPipeline = pipelineManager.getMethod("getPipeline");

            Class<?> worldRenderingPipeline = ClassUtils.getClassWithAlternative(
                "net.coderbot.iris.pipeline.WorldRenderingPipeline",
                "net.irisshaders.iris.pipeline.WorldRenderingPipeline");

            WorldRenderingPipeline_shouldRenderUnderwaterOverlay = worldRenderingPipeline.getMethod(
                "shouldRenderUnderwaterOverlay");

            Class<?> immediateState = Class.forName("net.irisshaders.iris.vertices.ImmediateState");
            ImmediateState_skipExtension = immediateState.getField("skipExtension");
            ImmediateState_skipExtension_set = ImmediateState_skipExtension.get(null).getClass()
                .getMethod("set", Object.class);
            ImmediateState_skipExtension_get = ImmediateState_skipExtension.get(null).getClass()
                .getMethod("get");
            try {
                // not all iris versions have ssbos so try them separately
                IrisRenderingPipeline = ClassUtils.getClassWithAlternative(
                    "net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline",
                    "net.irisshaders.iris.pipeline.IrisRenderingPipeline");
                IrisRenderingPipeline_shaderStorageBufferHolder = IrisRenderingPipeline.getDeclaredField(
                    "shaderStorageBufferHolder");
                IrisRenderingPipeline_shaderStorageBufferHolder.setAccessible(true);

                Class<?> shaderStorageBufferHolder = ClassUtils.getClassWithAlternative(
                    "net.coderbot.iris.gl.buffer.ShaderStorageBufferHolder",
                    "net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder");
                ShaderStorageBufferHolder_setupBuffers = shaderStorageBufferHolder.getMethod("setupBuffers");
            } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
                VRSettings.LOGGER.info("Vivecraft: iris has no SSBO support");
            }

            // distant horizon compat
            if (Xloader.INSTANCE.isModLoaded("distanthorizons")) {
                try {
                    Class<?> OverrideInjector = Class.forName(
                        "com.seibel.distanthorizons.coreapi.DependencyInjection.OverrideInjector");
                    dhOverrideInjector = OverrideInjector.getDeclaredField("INSTANCE").get(null);

                    OverrideInjector_unbind = OverrideInjector.getMethod("unbind", Class.class,
                        Class.forName("com.seibel.distanthorizons.api.interfaces.override.IDhApiOverrideable"));

                    IDhApiFramebuffer = Class.forName(
                        "com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiFramebuffer");

                    Pipeline_getDHCompat = Class.forName("net.irisshaders.iris.pipeline.WorldRenderingPipeline")
                        .getMethod("getDHCompat");

                    DHCompatInternal_getInstance = Class.forName("net.irisshaders.iris.compat.dh.DHCompat")
                        .getMethod("getInstance");
                    Class<?> DHCompatInternal = Class.forName("net.irisshaders.iris.compat.dh.DHCompatInternal");
                    DHCompatInternal_getShadowFBWrapper = DHCompatInternal.getMethod("getShadowFBWrapper");
                    DHCompatInternal_getSolidFBWrapper = DHCompatInternal.getMethod("getSolidFBWrapper");

                    // DH 2.2+
                    try {
                        IDhApiGenericObjectShaderProgram = Class.forName(
                            "com.seibel.distanthorizons.api.interfaces.override.rendering.IDhApiGenericObjectShaderProgram");
                        DHCompatInternal_getGenericShader = DHCompatInternal.getMethod("getGenericShader");
                    } catch (ClassNotFoundException | NoSuchMethodException ignored) {}

                    Class<?> CapturedRenderingState = Class.forName(
                        "net.irisshaders.iris.uniforms.CapturedRenderingState");
                    CapturedRenderingState_getGbufferProjection = CapturedRenderingState.getMethod(
                        "getGbufferProjection");
                    DH_PRESENT = true;
                } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
                    VRSettings.LOGGER.error("Vivecraft: DH present but compat init failed:", e);
                    DH_PRESENT = false;
                }
            }
            try {
                Class<?> ShaderKey = Class.forName("net.irisshaders.iris.pipeline.programs.ShaderKey");
                IrisPipelines_assignPipeline = Class.forName("net.irisshaders.iris.pipeline.IrisPipelines")
                    .getMethod("assignPipeline", RenderPipeline.class, ShaderKey);
                ShaderKey_ENTITIES_SOLID = ShaderKey.getField("ENTITIES_SOLID").get(null);
                ShaderKey_ENTITIES_CUTOUT = ShaderKey.getField("ENTITIES_CUTOUT").get(null);
                ShaderKey_ENTITIES_TRANSLUCENT = ShaderKey.getField("ENTITIES_TRANSLUCENT").get(null);
                ShaderKey_BASIC_COLOR = ShaderKey.getField("BASIC_COLOR").get(null);
                ShaderKey_TEXTURED_COLOR = ShaderKey.getField("TEXTURED_COLOR").get(null);
            } catch (NoSuchMethodException | NoSuchFieldException | NullPointerException e) {
                VRSettings.LOGGER.error("Vivecraft: Failed to init iris pipeline compat, falling back to official API.",
                    e);
                IrisPipelines_assignPipeline = null;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException e) {
            INIT_FAILED = true;
            VRSettings.LOGGER.error("Vivecraft: Failed to initialize Iris compat", e);
        }

        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
