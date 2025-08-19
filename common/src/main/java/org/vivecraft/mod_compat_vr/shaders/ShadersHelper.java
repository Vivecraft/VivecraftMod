package org.vivecraft.mod_compat_vr.shaders;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.Triple;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * helper to wrap general shader related task in one class, independent if running Optifine or iris
 */
public class ShadersHelper {

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
     * adds the vivecraft macros, using the provided consumers
     *
     * @param createMacro      a consumer that defines a name as existent
     * @param createValueMacro a consumer that defines a name with a value
     */
    public static void addMacros(Consumer<String> createMacro, BiConsumer<String, Integer> createValueMacro) {
        createMacro.accept("VIVECRAFT");
        for (RenderPass pass : RenderPass.values()) {
            createValueMacro.accept("VIVECRAFT_PASS_" + pass.toString(), pass.ordinal());
        }

        createValueMacro.accept("VIVECRAFT_PASS_VANILLA", -1);
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
                    return MathUtils.subtractToVector3f(mc.gameRenderer.getMainCamera().getPosition(),
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
                    return MathUtils.subtractToVector3f(mc.gameRenderer.getMainCamera().getPosition(),
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
            UNIFORMS.add(Triple.of("vivecraftRenderpass", UniformType.INTEGER,
                () -> VRState.VR_RUNNING && ClientDataHolderVR.getInstance().currentPass != null ?
                    ClientDataHolderVR.getInstance().currentPass.ordinal() : -1));
        }
        return UNIFORMS;
    }

    /**
     * registers the vr RenderPipelines to be mapped to the shader ones
     */
    public static void registerPipelines() {
        BiConsumer<RenderPipeline, String> consumer = null;
        if (IrisHelper.isLoaded()) {
            consumer = IrisHelper::registerPipeline;
        } else if (OptifineHelper.isOptifineLoaded()) {
            // TODO 1.21.5+ check how optifine handles this, once it is done
            consumer = null;
        }
        if (consumer != null) {
            consumer.accept(VRShaders.CROSSHAIR_WORLD, "ENTITIES");
            consumer.accept(VRShaders.CROSSHAIR_WORLD_ALWAYS, "ENTITIES");

            consumer.accept(VRShaders.ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT, "ENTITIES_TRANSLUCENT");
            consumer.accept(VRShaders.ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT, "ENTITIES_TRANSLUCENT");
            consumer.accept(VRShaders.ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT, "ENTITIES");
            consumer.accept(VRShaders.ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT, "ENTITIES");
            consumer.accept(VRShaders.ENTITY_SOLID_NO_CARDINAL_LIGHT, "ENTITIES");

            consumer.accept(VRShaders.QUADS, "BASIC");
            consumer.accept(VRShaders.QUADS_ALWAYS, "BASIC");
            consumer.accept(VRShaders.TRIANGLES_ALWAYS, "BASIC");
            consumer.accept(VRShaders.TRIANGLE_FAN_ALWAYS, "BASIC");
            consumer.accept(VRShaders.TEXT_NO_CULL, "ENTITIES_TRANSLUCENT");
        }
    }
}
