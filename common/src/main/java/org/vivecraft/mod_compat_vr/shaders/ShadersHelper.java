package org.vivecraft.mod_compat_vr.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector3f;
import org.vivecraft.Xloader;
import org.vivecraft.api.client.data.RenderPass;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
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
     * @param resourceLocation ResourceLocation of the texture to bind
     */
    public static void bindTexture(ResourceLocation resourceLocation) {
        Minecraft.getInstance().getTextureManager().bindForSetup(resourceLocation);
        RenderSystem.setShaderTexture(0, resourceLocation);
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
        if (Xloader.isModLoadedSuccess()) {
            createMacro.accept("VIVECRAFT");
            String[] modVersion = Xloader.getModVersion().split("-", 3)[1].split("\\.");
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
                            Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
                    } else {
                        return MathUtils.ZERO;
                    }
                }));
        }
        return UNIFORMS;
    }
}
