package org.vivecraft.render;

import org.lwjgl.opengl.ARBShaderObjects;
import org.vivecraft.utils.Utils;

public class VRShaders
{
    public static int _Lanczos_shaderProgramId = -1;
    public static int _Lanczos_texelWidthOffsetUniform = -1;
    public static int _Lanczos_texelHeightOffsetUniform = -1;
    public static int _Lanczos_inputImageTextureUniform = -1;
    public static int _Lanczos_inputDepthTextureUniform = -1;
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
            _DepthMask_resolutionUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "resolution");
            _DepthMask_positionUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "position");
            _DepthMask_colorTexUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "colorTex");
            _DepthMask_depthTexUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "depthTex");
            _DepthMask_hmdViewPosition = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "hmdViewPosition");
            _DepthMask_hmdPlaneNormal = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "hmdPlaneNormal");
            _DepthMask_projectionMatrix = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "projectionMatrix");
            _DepthMask_viewMatrix = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "viewMatrix");
            _DepthMask_passUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "pass");
            _DepthMask_keyColorUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "keyColor");
            _DepthMask_alphaModeUniform = ARBShaderObjects.glGetUniformLocationARB(_DepthMask_shaderProgramId, "alphaMode");
        }
    }

    public static void setupFSAA() throws Exception
    {
        _Lanczos_shaderProgramId = ShaderHelper.initShaders(LANCZOS_SAMPLER_VERTEX_SHADER, LANCZOS_SAMPLER_FRAGMENT_SHADER, true);

        if (_Lanczos_shaderProgramId == 0)
        {
            throw new Exception("Failed to validate FSAA shader!");
        }
        else
        {
            _Lanczos_texelWidthOffsetUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelWidthOffset");
            _Lanczos_texelHeightOffsetUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "texelHeightOffset");
            _Lanczos_inputImageTextureUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "inputImageTexture");
            _Lanczos_inputDepthTextureUniform = ARBShaderObjects.glGetUniformLocationARB(_Lanczos_shaderProgramId, "inputDepthTexture");
        }
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
            _FOVReduction_RadiusUniform = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "circle_radius");
            _FOVReduction_OffsetUniform = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "circle_offset");
            _FOVReduction_BorderUniform = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "border");
            _FOVReduction_TextureUniform = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "tex0");
            _Overlay_HealthAlpha = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "redalpha");
            _Overlay_FreezeAlpha = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "bluealpha");
            _Overlay_waterAmplitude = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "water");
            _Overlay_portalAmplitutde = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "portal");
            _Overlay_pumpkinAmplitutde = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "pumpkin");
            _Overlay_eye = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "eye");
            _Overlay_time = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "portaltime");
            _Overlay_BlackAlpha = ARBShaderObjects.glGetUniformLocationARB(_FOVReduction_shaderProgramId, "blackalpha");
        }
    }
}
