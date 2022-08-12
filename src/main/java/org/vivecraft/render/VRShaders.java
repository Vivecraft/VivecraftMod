package org.vivecraft.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.lwjgl.opengl.GL43C;
import org.vivecraft.utils.Utils;

public class VRShaders
{
    public static ShaderInstance lanczosShader;
    public static AbstractUniform _Lanczos_texelWidthOffsetUniform;
    public static AbstractUniform _Lanczos_texelHeightOffsetUniform;
    public static AbstractUniform _Lanczos_inputImageTextureUniform;
    public static AbstractUniform _Lanczos_inputDepthTextureUniform;
    public static AbstractUniform _Lanczos_projectionUniform;
    public static AbstractUniform _Lanczos_modelViewUniform;
    public static int _DepthMask_shaderProgramId = -1;
    public static int _DepthMask_resolutionUniform = -1;
    public static int _DepthMask_positionUniform = -1;
    public static int _DepthMask_scaleUniform = -1;
    public static int _DepthMask_colorTexUniform = -1;
    public static int _DepthMask_depthTexUniform = -1;
    public static int _DepthMask_hmdViewPosition = -1;
    public static int _DepthMask_hmdPlaneNormal = -1;
    public static int _DepthMask_projectionMatrix = -1;
    public static int _DepthMask_viewMatrix = -1;
    public static int _DepthMask_passUniform = -1;
    public static int _DepthMask_keyColorUniform = -1;
    public static int _DepthMask_alphaModeUniform = -1;
    public static int _FOVReduction_Enabled = -1;
    public static int _FOVReduction_RadiusUniform = -1;
    public static int _FOVReduction_OffsetUniform = -1;
    public static int _FOVReduction_BorderUniform = -1;
    public static int _FOVReduction_TextureUniform = -1;
    public static int _FOVReduction_shaderProgramId = -1;
    public static int _Overlay_HealthAlpha = -1;
    public static int _Overlay_FreezeAlpha = -1;
    public static int _Overlay_waterAmplitude = -1;
    public static int _Overlay_portalAmplitutde = -1;
    public static int _Overlay_pumpkinAmplitutde = -1;
    public static int _Overlay_time = -1;
    public static int _Overlay_BlackAlpha = -1;
    public static int _Overlay_eye = -1;
    public static final String PASSTHRU_VERTEX_SHADER = Utils.loadAssetAsString("shaders/passthru.vsh", true);
    public static final String DEPTH_MASK_FRAGMENT_SHADER = Utils.loadAssetAsString("shaders/mixedreality.fsh", true);
    public static final String LANCZOS_SAMPLER_VERTEX_SHADER = Utils.loadAssetAsString("shaders/lanczos.vsh", true);
    public static final String LANCZOS_SAMPLER_FRAGMENT_SHADER = Utils.loadAssetAsString("shaders/lanczos.fsh", true);
    public static final String FOV_REDUCTION_FRAGMENT_SHADER = Utils.loadAssetAsString("shaders/fovreduction.fsh", true);

    private VRShaders()
    {
    }

    public static void setupDepthMask() throws Exception
    {
        _DepthMask_shaderProgramId = ShaderHelper.initShaders(PASSTHRU_VERTEX_SHADER, DEPTH_MASK_FRAGMENT_SHADER, true);

        if (_DepthMask_shaderProgramId == 0)
        {
            System.out.println("Failed to validate depth mask shader! Mixed reality will not function!");
        }
        else
        {
            _DepthMask_resolutionUniform = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "resolution");
            _DepthMask_positionUniform = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "position");
            _DepthMask_colorTexUniform = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "colorTex");
            _DepthMask_depthTexUniform = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "depthTex");
            _DepthMask_hmdViewPosition = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "hmdViewPosition");
            _DepthMask_hmdPlaneNormal = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "hmdPlaneNormal");
            _DepthMask_projectionMatrix = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "projectionMatrix");
            _DepthMask_viewMatrix = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "viewMatrix");
            _DepthMask_passUniform = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "pass");
            _DepthMask_keyColorUniform = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "keyColor");
            _DepthMask_alphaModeUniform = GL43C.glGetUniformLocation(_DepthMask_shaderProgramId, "alphaMode");
        }
    }

    public static void setupFSAA() throws Exception
    {
        lanczosShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "lanczos", DefaultVertexFormat.POSITION_TEX);


            _Lanczos_texelWidthOffsetUniform = lanczosShader.safeGetUniform( "texelWidthOffset");
            _Lanczos_texelHeightOffsetUniform = lanczosShader.safeGetUniform( "texelHeightOffset");
            _Lanczos_inputImageTextureUniform = lanczosShader.safeGetUniform( "inputImageTexture");
            _Lanczos_inputDepthTextureUniform = lanczosShader.safeGetUniform( "inputDepthTexture");
            _Lanczos_projectionUniform = lanczosShader.safeGetUniform("projection");
            _Lanczos_modelViewUniform = lanczosShader.safeGetUniform( "modelView");
    }

    public static void setupFOVReduction() throws Exception
    {
        _FOVReduction_shaderProgramId = ShaderHelper.initShaders(PASSTHRU_VERTEX_SHADER, FOV_REDUCTION_FRAGMENT_SHADER, true);

        if (_FOVReduction_shaderProgramId == 0)
        {
            throw new Exception("Failed to validate FOV shader!");
        }
        else
        {
            _FOVReduction_RadiusUniform = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "circle_radius");
            _FOVReduction_OffsetUniform = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "circle_offset");
            _FOVReduction_BorderUniform = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "border");
            _FOVReduction_TextureUniform = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "tex0");
            _Overlay_HealthAlpha = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "redalpha");
            _Overlay_FreezeAlpha = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "bluealpha");
            _Overlay_waterAmplitude = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "water");
            _Overlay_portalAmplitutde = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "portal");
            _Overlay_pumpkinAmplitutde = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "pumpkin");
            _Overlay_eye = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "eye");
            _Overlay_time = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "portaltime");
            _Overlay_BlackAlpha = GL43C.glGetUniformLocation(_FOVReduction_shaderProgramId, "blackalpha");
        }
    }
}
