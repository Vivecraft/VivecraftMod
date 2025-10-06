package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
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
    public static final String MIXED_REALITY_HMD_VIEW_POSITION = "hmdViewPosition";
    public static final String MIXED_REALITY_HMD_PLANE_NORMAL = "hmdPlaneNormal";
    public static final String MIXED_REALITY_PROJECTION_MATRIX = "projectionMatrix";
    public static final String MIXED_REALITY_VIEW_MATRIX = "viewMatrix";
    public static final String MIXED_REALITY_FIRST_PERSON_PASS = "firstPersonPass";
    public static final String MIXED_REALITY_KEY_COLOR = "keyColor";
    public static final String MIXED_REALITY_ALPHA_MODE = "alphaMode";
    public static final String MIXED_REALITY_FIRST_COLOR_SAMPLER = "firstPersonColor";
    public static final String MIXED_REALITY_THIRD_COLOR_SAMPLER = "thirdPersonColor";
    public static final String MIXED_REALITY_THIRD_DEPTH_SAMPLER = "thirdPersonDepth";

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

    public static void reload() {
        READY = false;
        try {
            setupDepthMask();
            RenderHelper.checkGLError("init depth shader");
            setupFOVReduction();
            RenderHelper.checkGLError("init FOV shader");
            setupFSAA();
            RenderHelper.checkGLError("init fsaa shader");
            READY = true;
        } catch (NullPointerException e) {
            VRSettings.LOGGER.error("Vivecraft: Shader creation failed:", e);
        }
    }

    private static void setupDepthMask() throws NullPointerException {
        CompiledShaderProgram program = Minecraft.getInstance().getShaderManager().getProgram(MIXED_REALITY_SHADER);
        MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM = program.safeGetUniform(MIXED_REALITY_HMD_VIEW_POSITION);
        MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM = program.safeGetUniform(MIXED_REALITY_HMD_PLANE_NORMAL);
        MIXED_REALITY_PROJECTION_MATRIX_UNIFORM = program.safeGetUniform(MIXED_REALITY_PROJECTION_MATRIX);
        MIXED_REALITY_VIEW_MATRIX_UNIFORM = program.safeGetUniform(MIXED_REALITY_VIEW_MATRIX);
        MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM = program.safeGetUniform(MIXED_REALITY_FIRST_PERSON_PASS);
        MIXED_REALITY_KEY_COLOR_UNIFORM = program.safeGetUniform(MIXED_REALITY_KEY_COLOR);
        MIXED_REALITY_ALPHA_MODE_UNIFORM = program.safeGetUniform(MIXED_REALITY_ALPHA_MODE);
    }

    private static void setupFSAA() throws NullPointerException {
        CompiledShaderProgram program = Minecraft.getInstance().getShaderManager().getProgram(LANCZOS_SHADER);
        LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM = program.safeGetUniform(LANCZOS_TEXEL_WIDTH_OFFSET);
        LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM = program.safeGetUniform(LANCZOS_TEXEL_HEIGHT_OFFSET);
    }

    private static void setupFOVReduction() throws NullPointerException {
        CompiledShaderProgram program = Minecraft.getInstance().getShaderManager().getProgram(POST_PROCESSING_SHADER);
        POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM = program.safeGetUniform(POST_PROCESSING_FOV_REDUCTION_RADIUS);
        POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM = program.safeGetUniform(POST_PROCESSING_FOV_REDUCTION_OFFSET);
        POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM = program.safeGetUniform(POST_PROCESSING_FOV_REDUCTION_BORDER);
        POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM = program.safeGetUniform(POST_PROCESSING_OVERLAY_HEALTH_ALPHA);
        POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM = program.safeGetUniform(POST_PROCESSING_OVERLAY_FREEZE_ALPHA);
        POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM = program.safeGetUniform(
            POST_PROCESSING_OVERLAY_WATER_AMPLITUDE);
        POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM = program.safeGetUniform(
            POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE);
        POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM = program.safeGetUniform(
            POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE);
        POST_PROCESSING_OVERLAY_EYE_UNIFORM = program.safeGetUniform(POST_PROCESSING_OVERLAY_EYE);
        POST_PROCESSING_OVERLAY_TIME_UNIFORM = program.safeGetUniform(POST_PROCESSING_OVERLAY_TIME);
        POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM = program.safeGetUniform(POST_PROCESSING_OVERLAY_BLACK_ALPHA);
    }

    public static void setupBlitAspect() throws Exception {
        BLIT_VR_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "blit_vr",
            DefaultVertexFormat.POSITION_TEX);
    }

    public static void setupPortalShaders() throws IOException {
        RENDERTYPE_END_PORTAL_VR_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(),
            "rendertype_end_portal_vr", DefaultVertexFormat.POSITION);
        RENDERTYPE_END_GATEWAY_VR_SHADER = new ShaderInstance(Minecraft.getInstance().getResourceManager(),
            "rendertype_end_gateway_vr", DefaultVertexFormat.POSITION);
    }
}
