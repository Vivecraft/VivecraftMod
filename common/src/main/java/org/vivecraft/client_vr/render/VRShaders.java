package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

public class VRShaders
{
    public static ShaderInstance lanczosShader;
    public static AbstractUniform _Lanczos_texelWidthOffsetUniform;
    public static AbstractUniform _Lanczos_texelHeightOffsetUniform;
    public static AbstractUniform _Lanczos_inputImageTextureUniform;
    public static AbstractUniform _Lanczos_inputDepthTextureUniform;
    public static AbstractUniform _Lanczos_projectionUniform;
    public static AbstractUniform _Lanczos_modelViewUniform;
    public static ShaderInstance depthMaskShader;
    public static AbstractUniform _DepthMask_resolutionUniform;
    public static AbstractUniform _DepthMask_positionUniform;
    public static AbstractUniform _DepthMask_scaleUniform;
    public static AbstractUniform _DepthMask_hmdViewPosition;
    public static AbstractUniform _DepthMask_hmdPlaneNormal;
    public static AbstractUniform _DepthMask_projectionMatrix;
    public static AbstractUniform _DepthMask_viewMatrix;
    public static AbstractUniform _DepthMask_passUniform;
    public static AbstractUniform _DepthMask_keyColorUniform;
    public static AbstractUniform _DepthMask_alphaModeUniform;
    public static int _FOVReduction_Enabled;
    public static AbstractUniform _FOVReduction_RadiusUniform;
    public static AbstractUniform _FOVReduction_OffsetUniform;
    public static AbstractUniform _FOVReduction_BorderUniform;
    public static ShaderInstance fovReductionShader;
    public static AbstractUniform _Overlay_HealthAlpha;
    public static AbstractUniform _Overlay_FreezeAlpha;
    public static AbstractUniform _Overlay_waterAmplitude;
    public static AbstractUniform _Overlay_portalAmplitutde;
    public static AbstractUniform _Overlay_pumpkinAmplitutde;
    public static AbstractUniform _Overlay_time;
    public static AbstractUniform _Overlay_BlackAlpha;
    public static AbstractUniform _Overlay_eye;

    public static ShaderInstance rendertypeEndPortalShaderVR;
    public static ShaderInstance rendertypeEndGatewayShaderVR;

    public static ShaderInstance getRendertypeEndPortalShaderVR(){
        return rendertypeEndPortalShaderVR;
    }
    public static ShaderInstance getRendertypeEndGatewayShaderVR(){
        return rendertypeEndGatewayShaderVR;
    }

    private VRShaders()
    {
    }

    public static void setupDepthMask() throws Exception
    {
        depthMaskShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "mixedreality", DefaultVertexFormat.POSITION_TEX);

        
            _DepthMask_resolutionUniform = depthMaskShader.safeGetUniform( "resolution");
            _DepthMask_positionUniform = depthMaskShader.safeGetUniform( "position");
            _DepthMask_hmdViewPosition = depthMaskShader.safeGetUniform( "hmdViewPosition");
            _DepthMask_hmdPlaneNormal = depthMaskShader.safeGetUniform( "hmdPlaneNormal");
            _DepthMask_projectionMatrix = depthMaskShader.safeGetUniform( "projectionMatrix");
            _DepthMask_viewMatrix = depthMaskShader.safeGetUniform( "viewMatrix");
            _DepthMask_passUniform = depthMaskShader.safeGetUniform( "pass");
            _DepthMask_keyColorUniform = depthMaskShader.safeGetUniform( "keyColor");
            _DepthMask_alphaModeUniform = depthMaskShader.safeGetUniform( "alphaMode");
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
        fovReductionShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "fovreduction", DefaultVertexFormat.POSITION_TEX);


            _FOVReduction_RadiusUniform = fovReductionShader.safeGetUniform("circle_radius");
            _FOVReduction_OffsetUniform = fovReductionShader.safeGetUniform("circle_offset");
            _FOVReduction_BorderUniform = fovReductionShader.safeGetUniform("border");
            _Overlay_HealthAlpha = fovReductionShader.safeGetUniform("redalpha");
            _Overlay_FreezeAlpha = fovReductionShader.safeGetUniform("bluealpha");
            _Overlay_waterAmplitude = fovReductionShader.safeGetUniform("water");
            _Overlay_portalAmplitutde = fovReductionShader.safeGetUniform("portal");
            _Overlay_pumpkinAmplitutde = fovReductionShader.safeGetUniform("pumpkin");
            _Overlay_eye = fovReductionShader.safeGetUniform("eye");
            _Overlay_time = fovReductionShader.safeGetUniform("portaltime");
            _Overlay_BlackAlpha = fovReductionShader.safeGetUniform("blackalpha");
    }

    public static void setupPortalShaders() throws Exception
    {
        rendertypeEndPortalShaderVR = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "rendertype_end_portal_vr", DefaultVertexFormat.POSITION);
        rendertypeEndGatewayShaderVR = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "rendertype_end_gateway_vr", DefaultVertexFormat.POSITION);
    }
}
