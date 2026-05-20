package org.vivecraft.mod_compat_vr.shaders;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector3f;
import org.vivecraft.Xloader;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.opengl.OpenGLHelper;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * helper to wrap general shader related task in one class, independent if running Optifine or iris
 */
public class ShadersHelper {

    public static boolean SLOW_MODE = false;

    public static Vec3 SHADOW_CAMERA_POSITION = Vec3.ZERO;
    private static final EnumMap<RenderPass, Vector3f> WRAPPED_SHADOW_CAMERA_POSITION = new EnumMap<>(RenderPass.class);
    private static final EnumMap<RenderPass, Vector3f> PREVIOUS_WRAPPED_SHADOW_CAMERA_POSITION = new EnumMap<>(
        RenderPass.class);

    static {
        for (RenderPass pass : RenderPass.values()) {
            WRAPPED_SHADOW_CAMERA_POSITION.put(pass, new Vector3f());
            PREVIOUS_WRAPPED_SHADOW_CAMERA_POSITION.put(pass, new Vector3f());
        }
    }

    public enum UniformType {
        MATRIX4F,
        VECTOR3F,
        INTEGER,
        BOOLEAN
    }

    private static List<Triple<String, UniformType, Supplier<?>>> UNIFORMS;

    /**
     * gets the minimum light to apply to hand/gui, depending on if shaders are active or not
     *
     * @return minimum light to apply
     */
    public static int ShaderLight() {
        return isShaderActive() ? 8 : 4;
    }

    /**
     * binds the given texture to texture slot 0, only if shaders are active
     *
     * @param identifier Identifier of the texture to bind
     */
    public static void bindTexture(Identifier identifier) {
        if (isShaderActive()) {
            GpuTextureView view = RenderHelper.getGpuTexture(identifier);
            OpenGLHelper.bindTexture(0, view);
        }
    }

    /**
     * @return if a shaderpack is active
     */
    public static boolean isShaderActive() {
        return (IrisHelper.isLoaded() && IrisHelper.isShaderActive()) ||
            (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive());
    }

    /**
     * @return if the current shader implementation needs the same buffer sizes for all passes
     */
    public static boolean needsSameSizeBuffers() {
        return OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive();
    }

    /**
     * @return if shaders are active, and the shadow pass is currently rendered
     */
    public static boolean isRenderingShadows() {
        return (IrisHelper.isLoaded() && IrisHelper.isShaderActive() && IrisHelper.isRenderingShadows()) ||
            (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive() &&
                OptifineHelper.isRenderingShadows()
            );
    }

    /**
     * reloads shaders, if the shader implementation needs it
     */
    public static void maybeReloadShaders() {
        if (IrisHelper.isLoaded()) {
            IrisHelper.reload();
        }
    }

    /**
     * @return if the shadow pass is run for each pass, instead of just once
     */
    public static boolean isSlowMode() {
        return SLOW_MODE || ClientDataHolderVR.getInstance().vrSettings.disableShaderOptimization;
    }

    /**
     * updates the position of the camera during the shadow pass
     *
     * @param setAll sets the position for all passes
     * @param x      X position
     * @param y      Y position
     * @param z      Z position
     */
    public static void setShadowCameraPosition(boolean setAll, float x, float y, float z) {
        if (isSlowMode() && !setAll) {
            // set just for current
            RenderPass current = ClientDataHolderVR.getInstance().currentPass;
            PREVIOUS_WRAPPED_SHADOW_CAMERA_POSITION.get(current)
                .set(WRAPPED_SHADOW_CAMERA_POSITION.get(current));
            WRAPPED_SHADOW_CAMERA_POSITION.get(current).set(x, y, z);
        } else {
            for (RenderPass pass : RenderPass.values()) {
                PREVIOUS_WRAPPED_SHADOW_CAMERA_POSITION.get(pass).set(WRAPPED_SHADOW_CAMERA_POSITION.get(pass));
                WRAPPED_SHADOW_CAMERA_POSITION.get(pass).set(x, y, z);
            }
        }
    }

    /**
     * adds the vivecraft macros, using the provided consumers
     *
     * @param createMacro      a consumer that defines a name as existent
     * @param createValueMacro a consumer that defines a name with a value
     */
    public static void addMacros(Consumer<String> createMacro, BiConsumer<String, Integer> createValueMacro) {
        if (Xloader.INSTANCE.isModLoadedSuccess()) {
            createMacro.accept("VIVECRAFT");
            String[] modVersion = Xloader.INSTANCE.getModVersion().split("-", 3)[1].split("\\.");
            int version = Integer.parseInt(modVersion[0]) * 10000 +
                Integer.parseInt(modVersion[1]) * 100 +
                Integer.parseInt(modVersion[2]);
            createValueMacro.accept("VIVECRAFT_VERSION", version);
            for (RenderPass pass : RenderPass.values()) {
                createValueMacro.accept("VIVECRAFT_PASS_" + pass.toString(), pass.ordinal());
            }
        }
    }

    /**
     * @return a list of uniform names and suppliers
     */
    public static List<Triple<String, UniformType, Supplier<?>>> getUniforms() {
        // only create that once, it doesn't change
        if (UNIFORMS == null) {
            UNIFORMS = new ArrayList<>();
            ClientDataHolderVR dh = ClientDataHolderVR.getInstance();
            Minecraft mc = Minecraft.getInstance();
            // main hand
            UNIFORMS.add(Triple.of("vivecraftRelativeMainHandPos", UniformType.VECTOR3F, () -> {
                if (VRState.VR_RUNNING) {
                    return MathUtils.subtractToVector3f(mc.gameRenderer.getMainCamera().position(),
                        RenderHelper.getControllerRenderPos(0));
                } else {
                    return MathUtils.ZERO;
                }
            }));
            UNIFORMS.add(Triple.of("vivecraftRelativeMainHandRot", UniformType.MATRIX4F, () -> {
                if (VRState.VR_RUNNING) {
                    return dh.vrPlayer.getVRDataWorld().getController(0).getMatrix();
                } else {
                    return MathUtils.IDENTITY;
                }
            }));

            // offhand
            UNIFORMS.add(Triple.of("vivecraftRelativeOffHandPos", UniformType.VECTOR3F, () -> {
                if (VRState.VR_RUNNING) {
                    return MathUtils.subtractToVector3f(mc.gameRenderer.getMainCamera().position(),
                        RenderHelper.getControllerRenderPos(1));
                } else {
                    return MathUtils.ZERO;
                }
            }));
            UNIFORMS.add(Triple.of("vivecraftRelativeOffHandRot", UniformType.MATRIX4F, () -> {
                if (VRState.VR_RUNNING) {
                    return dh.vrPlayer.getVRDataWorld().getController(1).getMatrix();
                } else {
                    return MathUtils.IDENTITY;
                }
            }));

            // vr toggle
            UNIFORMS.add(Triple.of("vivecraftIsVR", UniformType.BOOLEAN, () -> VRState.VR_RUNNING));

            // renderpass
            UNIFORMS.add(Triple.of("vivecraftRenderpass", UniformType.INTEGER, () -> dh.currentPass.ordinal()));

            // shadow camera position
            UNIFORMS.add(Triple.of("vivecraftShadowCameraPosition", UniformType.VECTOR3F,
                () -> WRAPPED_SHADOW_CAMERA_POSITION.get(dh.currentPass)));
            UNIFORMS.add(Triple.of("vivecraftPreviousShadowCameraPosition", UniformType.VECTOR3F,
                () -> PREVIOUS_WRAPPED_SHADOW_CAMERA_POSITION.get(dh.currentPass)));

            UNIFORMS.add(
                Triple.of("vivecraftShadowCameraOffset", UniformType.VECTOR3F, () -> {
                    if (VRState.VR_RUNNING) {
                        return MathUtils.subtractToVector3f(SHADOW_CAMERA_POSITION,
                            Minecraft.getInstance().gameRenderer.getMainCamera().position());
                    } else {
                        return MathUtils.ZERO;
                    }
                }));
        }
        return UNIFORMS;
    }

    /**
     * registers the vr RenderPipelines to be mapped to the shader ones
     */
    public static void registerPipelines() {
        BiConsumer<RenderPipeline, ShaderType> consumer = null;
        if (IrisHelper.isLoaded()) {
            consumer = IrisHelper::registerPipeline;
        }
        // optifine does this still automatically, based on the shader name of the pipeline
        if (consumer != null) {
            consumer.accept(VRShaders.CROSSHAIR_WORLD, ShaderType.ENTITIES_CUTOUT);
            consumer.accept(VRShaders.CROSSHAIR_WORLD_ALWAYS, ShaderType.ENTITIES_CUTOUT);

            consumer.accept(VRShaders.ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT, ShaderType.ENTITIES_TRANSLUCENT);
            consumer.accept(VRShaders.ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT, ShaderType.ENTITIES_TRANSLUCENT);
            consumer.accept(VRShaders.ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT, ShaderType.ENTITIES_CUTOUT);
            consumer.accept(VRShaders.ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT, ShaderType.ENTITIES_CUTOUT);
            consumer.accept(VRShaders.ENTITY_SOLID_NO_CARDINAL_LIGHT, ShaderType.ENTITIES_SOLID);

            consumer.accept(VRShaders.GUI_TEXTURED, ShaderType.TEXTURED_COLOR);
            consumer.accept(VRShaders.GUI_TEXTURED_ALWAYS, ShaderType.TEXTURED_COLOR);

            consumer.accept(VRShaders.LINE_STRIP, ShaderType.BASIC_COLOR);
            consumer.accept(VRShaders.QUADS, ShaderType.BASIC_COLOR);
            consumer.accept(VRShaders.QUADS_ALWAYS, ShaderType.BASIC_COLOR);
            consumer.accept(VRShaders.TRIANGLES_ALWAYS, ShaderType.BASIC_COLOR);
            consumer.accept(VRShaders.TRIANGLE_FAN_ALWAYS, ShaderType.BASIC_COLOR);
            consumer.accept(VRShaders.TEXT_NO_CULL, ShaderType.ENTITIES_TRANSLUCENT);
        }
    }
}
