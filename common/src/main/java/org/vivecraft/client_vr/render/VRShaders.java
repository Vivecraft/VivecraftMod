package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.IOException;

public class VRShaders {
    // FSAA shader and its uniforms
    public static ShaderInstance LANCZOS_SHADER;
    public static AbstractUniform LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM;
    public static AbstractUniform LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM;
    public static final String LANCZOS_TEXEL_WIDTH_OFFSET = "texelWidthOffset";
    public static final String LANCZOS_TEXEL_HEIGHT_OFFSET = "texelHeightOffset";
    public static final String LANCZOS_COLOR_SAMPLER = "Sampler0";
    public static final String LANCZOS_DEPTH_SAMPLER = "Sampler1";

    // mixed reality shader and its uniforms
    public static ShaderInstance MIXED_REALITY_SHADER;
    public static AbstractUniform MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM;
    public static AbstractUniform MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM;
    public static AbstractUniform MIXED_REALITY_PROJECTION_MATRIX_UNIFORM;
    public static AbstractUniform MIXED_REALITY_VIEW_MATRIX_UNIFORM;
    public static AbstractUniform MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM;
    public static AbstractUniform MIXED_REALITY_KEY_COLOR_UNIFORM;
    public static AbstractUniform MIXED_REALITY_ALPHA_MODE_UNIFORM;
    public static AbstractUniform MIXED_REALITY_GUI_MASK_UNIFORM;
    public static final String MIXED_REALITY_HMD_VIEW_POSITION = "hmdViewPosition";
    public static final String MIXED_REALITY_HMD_PLANE_NORMAL = "hmdPlaneNormal";
    public static final String MIXED_REALITY_PROJECTION_MATRIX = "projectionMatrix";
    public static final String MIXED_REALITY_VIEW_MATRIX = "viewMatrix";
    public static final String MIXED_REALITY_FIRST_PERSON_PASS = "firstPersonPass";
    public static final String MIXED_REALITY_KEY_COLOR = "keyColor";
    public static final String MIXED_REALITY_ALPHA_MODE = "alphaMode";
    public static final String MIXED_REALITY_GUI_MASK = "guiMask";
    public static final String MIXED_REALITY_FIRST_COLOR_SAMPLER = "firstPersonColor";
    public static final String MIXED_REALITY_THIRD_COLOR_SAMPLER = "thirdPersonColor";
    public static final String MIXED_REALITY_THIRD_DEPTH_SAMPLER = "thirdPersonDepth";
    public static final String MIXED_REALITY_GUI_COLOR_SAMPLER = "guiColor";
    public static final int MIXED_REALITY_GUI_FIRST = 1;
    public static final int MIXED_REALITY_GUI_THIRD = 2;
    public static final int MIXED_REALITY_GUI_SEPARATE = 4;

    // vr post shader and its uniforms
    public static ShaderInstance POST_PROCESSING_SHADER;
    public static AbstractUniform POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM;
    public static AbstractUniform POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM;
    public static AbstractUniform POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_TIME_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM;
    public static AbstractUniform POST_PROCESSING_OVERLAY_EYE_UNIFORM;
    public static final String POST_PROCESSING_FOV_REDUCTION_RADIUS = "circle_radius";
    public static final String POST_PROCESSING_FOV_REDUCTION_OFFSET = "circle_offset";
    public static final String POST_PROCESSING_FOV_REDUCTION_BORDER = "border";
    public static final String POST_PROCESSING_OVERLAY_BLACK_ALPHA = "blackalpha";
    public static final String POST_PROCESSING_OVERLAY_HEALTH_ALPHA = "redalpha";
    public static final String POST_PROCESSING_OVERLAY_FREEZE_ALPHA = "bluealpha";
    public static final String POST_PROCESSING_OVERLAY_WATER_AMPLITUDE = "water";
    public static final String POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE = "portal";
    public static final String POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE = "pumpkin";
    public static final String POST_PROCESSING_OVERLAY_TIME = "portaltime";
    public static final String POST_PROCESSING_OVERLAY_EYE = "eye";
    public static final String POST_PROCESSING_COLOR_SAMPLER = "Sampler0";

    // blit shader
    public static ShaderInstance BLIT_VR_SHADER;
    public static final String BLIT_VR_COLOR_SAMPLER = "DiffuseSampler";

    // end portal shaders
    public static ShaderInstance RENDERTYPE_END_PORTAL_VR_SHADER;
    public static ShaderInstance RENDERTYPE_END_GATEWAY_VR_SHADER;

    public static ShaderInstance getRendertypeEndPortalVrShader() {
        return RENDERTYPE_END_PORTAL_VR_SHADER;
    }

    public static ShaderInstance getRendertypeEndGatewayVrShader() {
        return RENDERTYPE_END_GATEWAY_VR_SHADER;
    }

    private static boolean READY = false;

    private VRShaders() {}

    public static boolean isReady() {
        return READY;
    }

    public static void reload(ResourceManager resourceManager) {
        close();
        try {
            setupDepthMask(resourceManager);
            RenderHelper.checkGLError("init depth shader");
            setupFOVReduction(resourceManager);
            RenderHelper.checkGLError("init FOV shader");
            setupFSAA(resourceManager);
            RenderHelper.checkGLError("FBO init fsaa shader");
            setupBlitAspect(resourceManager);
            RenderHelper.checkGLError("init blit shader");
            setupPortalShaders(resourceManager);
            RenderHelper.checkGLError("init portal shader");
            READY = true;
        } catch (IOException e) {
            VRSettings.LOGGER.error("error loading VR shaders", e);
            READY = false;
        }
    }

    private static void close() {
        if (BLIT_VR_SHADER != null) {
            BLIT_VR_SHADER.close();
            BLIT_VR_SHADER = null;
        }
        if (LANCZOS_SHADER != null) {
            LANCZOS_SHADER.close();
            LANCZOS_SHADER = null;
        }
        if (MIXED_REALITY_SHADER != null) {
            MIXED_REALITY_SHADER.close();
            MIXED_REALITY_SHADER = null;
        }
        if (POST_PROCESSING_SHADER != null) {
            POST_PROCESSING_SHADER.close();
            POST_PROCESSING_SHADER = null;
        }
        if (RENDERTYPE_END_GATEWAY_VR_SHADER != null) {
            RENDERTYPE_END_GATEWAY_VR_SHADER.close();
            RENDERTYPE_END_GATEWAY_VR_SHADER = null;
        }
        if (RENDERTYPE_END_PORTAL_VR_SHADER != null) {
            RENDERTYPE_END_PORTAL_VR_SHADER.close();
            RENDERTYPE_END_PORTAL_VR_SHADER = null;
        }
        READY = false;
    }

    private static void setupDepthMask(ResourceManager resourceManager) throws IOException {
        MIXED_REALITY_SHADER = new ShaderInstance(resourceManager, "mixedreality_vr",
            DefaultVertexFormat.POSITION_TEX);
        MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_HMD_VIEW_POSITION);
        MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_HMD_PLANE_NORMAL);
        MIXED_REALITY_PROJECTION_MATRIX_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_PROJECTION_MATRIX);
        MIXED_REALITY_VIEW_MATRIX_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_VIEW_MATRIX);
        MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_FIRST_PERSON_PASS);
        MIXED_REALITY_KEY_COLOR_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_KEY_COLOR);
        MIXED_REALITY_ALPHA_MODE_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_ALPHA_MODE);
        MIXED_REALITY_GUI_MASK_UNIFORM = MIXED_REALITY_SHADER.safeGetUniform(MIXED_REALITY_GUI_MASK);
    }

    private static void setupFSAA(ResourceManager resourceManager) throws IOException {
        LANCZOS_SHADER = new ShaderInstance(resourceManager, "lanczos_vr",
            DefaultVertexFormat.POSITION_TEX);
        LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM = LANCZOS_SHADER.safeGetUniform(LANCZOS_TEXEL_WIDTH_OFFSET);
        LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM = LANCZOS_SHADER.safeGetUniform(LANCZOS_TEXEL_HEIGHT_OFFSET);
    }

    private static void setupFOVReduction(ResourceManager resourceManager) throws IOException {
        POST_PROCESSING_SHADER = new ShaderInstance(resourceManager, "postprocessing_vr",
            DefaultVertexFormat.POSITION_TEX);
        POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_FOV_REDUCTION_RADIUS);
        POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_FOV_REDUCTION_OFFSET);
        POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_FOV_REDUCTION_BORDER);
        POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_OVERLAY_HEALTH_ALPHA);
        POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_OVERLAY_FREEZE_ALPHA);
        POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_OVERLAY_WATER_AMPLITUDE);
        POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE);
        POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE);
        POST_PROCESSING_OVERLAY_EYE_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(POST_PROCESSING_OVERLAY_EYE);
        POST_PROCESSING_OVERLAY_TIME_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(POST_PROCESSING_OVERLAY_TIME);
        POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM = POST_PROCESSING_SHADER.safeGetUniform(
            POST_PROCESSING_OVERLAY_BLACK_ALPHA);
    }

    private static void setupBlitAspect(ResourceManager resourceManager) throws IOException {
        BLIT_VR_SHADER = new ShaderInstance(resourceManager, "blit_vr",
            DefaultVertexFormat.POSITION_TEX);
    }

    private static void setupPortalShaders(ResourceManager resourceManager) throws IOException {
        RENDERTYPE_END_PORTAL_VR_SHADER = new ShaderInstance(resourceManager, "rendertype_end_portal_vr",
            DefaultVertexFormat.POSITION);
        RENDERTYPE_END_GATEWAY_VR_SHADER = new ShaderInstance(resourceManager, "rendertype_end_gateway_vr",
            DefaultVertexFormat.POSITION);
    }
}
