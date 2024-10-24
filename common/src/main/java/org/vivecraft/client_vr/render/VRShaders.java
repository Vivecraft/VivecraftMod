package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;

import java.io.IOException;

public class VRShaders {
    // FSAA shader and its uniforms
    public static ShaderInstance lanczosShader;
    public static AbstractUniform _Lanczos_texelWidthOffsetUniform;
    public static AbstractUniform _Lanczos_texelHeightOffsetUniform;

    // mixed reality shader and its uniforms
    public static ShaderInstance mixedRealityShader;
    public static AbstractUniform _MixedReality_hmdViewPosition;
    public static AbstractUniform _MixedReality_hmdPlaneNormal;
    public static AbstractUniform _MixedReality_projectionMatrix;
    public static AbstractUniform _MixedReality_viewMatrix;
    public static AbstractUniform _MixedReality_firstPersonPassUniform;
    public static AbstractUniform _MixedReality_keyColorUniform;
    public static AbstractUniform _MixedReality_alphaModeUniform;

    // vr post shader and its uniforms
    public static ShaderInstance postProcessingShader;
    public static AbstractUniform _FOVReduction_RadiusUniform;
    public static AbstractUniform _FOVReduction_OffsetUniform;
    public static AbstractUniform _FOVReduction_BorderUniform;
    public static AbstractUniform _Overlay_HealthAlpha;
    public static AbstractUniform _Overlay_FreezeAlpha;
    public static AbstractUniform _Overlay_waterAmplitude;
    public static AbstractUniform _Overlay_portalAmplitutde;
    public static AbstractUniform _Overlay_pumpkinAmplitutde;
    public static AbstractUniform _Overlay_time;
    public static AbstractUniform _Overlay_BlackAlpha;
    public static AbstractUniform _Overlay_eye;

    // blit shader
    public static ShaderInstance blitVRShader;

    // end portal shaders
    public static ShaderInstance rendertypeEndPortalShaderVR;
    public static ShaderInstance rendertypeEndGatewayShaderVR;

    public static ShaderInstance getRendertypeEndPortalShaderVR() {
        return rendertypeEndPortalShaderVR;
    }

    public static ShaderInstance getRendertypeEndGatewayShaderVR() {
        return rendertypeEndGatewayShaderVR;
    }

    private VRShaders() {}

    public static void setupDepthMask() throws IOException {
        mixedRealityShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "mixedreality_vr", DefaultVertexFormat.POSITION_TEX);

        _MixedReality_hmdViewPosition = mixedRealityShader.safeGetUniform("hmdViewPosition");
        _MixedReality_hmdPlaneNormal = mixedRealityShader.safeGetUniform("hmdPlaneNormal");
        _MixedReality_projectionMatrix = mixedRealityShader.safeGetUniform("projectionMatrix");
        _MixedReality_viewMatrix = mixedRealityShader.safeGetUniform("viewMatrix");
        _MixedReality_firstPersonPassUniform = mixedRealityShader.safeGetUniform("firstPersonPass");
        _MixedReality_keyColorUniform = mixedRealityShader.safeGetUniform("keyColor");
        _MixedReality_alphaModeUniform = mixedRealityShader.safeGetUniform("alphaMode");
    }

    public static void setupFSAA() throws IOException {
        lanczosShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "lanczos_vr", DefaultVertexFormat.POSITION_TEX);

        _Lanczos_texelWidthOffsetUniform = lanczosShader.safeGetUniform("texelWidthOffset");
        _Lanczos_texelHeightOffsetUniform = lanczosShader.safeGetUniform("texelHeightOffset");
    }

    public static void setupFOVReduction() throws IOException {
        postProcessingShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "postprocessing_vr", DefaultVertexFormat.POSITION_TEX);

        _FOVReduction_RadiusUniform = postProcessingShader.safeGetUniform("circle_radius");
        _FOVReduction_OffsetUniform = postProcessingShader.safeGetUniform("circle_offset");
        _FOVReduction_BorderUniform = postProcessingShader.safeGetUniform("border");
        _Overlay_HealthAlpha = postProcessingShader.safeGetUniform("redalpha");
        _Overlay_FreezeAlpha = postProcessingShader.safeGetUniform("bluealpha");
        _Overlay_waterAmplitude = postProcessingShader.safeGetUniform("water");
        _Overlay_portalAmplitutde = postProcessingShader.safeGetUniform("portal");
        _Overlay_pumpkinAmplitutde = postProcessingShader.safeGetUniform("pumpkin");
        _Overlay_eye = postProcessingShader.safeGetUniform("eye");
        _Overlay_time = postProcessingShader.safeGetUniform("portaltime");
        _Overlay_BlackAlpha = postProcessingShader.safeGetUniform("blackalpha");
    }

    public static void setupBlitAspect() throws Exception {
        blitVRShader = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "blit_vr", DefaultVertexFormat.POSITION_TEX);
    }

    public static void setupPortalShaders() throws IOException {
        rendertypeEndPortalShaderVR = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "rendertype_end_portal_vr", DefaultVertexFormat.POSITION);
        rendertypeEndGatewayShaderVR = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "rendertype_end_gateway_vr", DefaultVertexFormat.POSITION);
    }
}
