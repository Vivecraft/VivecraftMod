package org.vivecraft.client_vr.render.rendertypes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.function.TriFunction;
import org.vivecraft.client.extensions.RenderSetupExtension;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.render.VRShaders;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class VRRenderTypes {

    public static final OutputTarget HANDS_TARGET = new OutputTarget("vrhands_target",
        () -> ((LevelRendererExtension) Minecraft.getInstance().levelRenderer).vivecraft$getHandsTarget());
    public static final OutputTarget OCCLUDED_TARGET = new OutputTarget("vroccluded_target",
        () -> ((LevelRendererExtension) Minecraft.getInstance().levelRenderer).vivecraft$getVrOccludedTarget());
    public static final OutputTarget UNOCCLUDED_TARGET = new OutputTarget("vrunoccluded_target",
        () -> ((LevelRendererExtension) Minecraft.getInstance().levelRenderer).vivecraft$getVrUnoccludedTarget());

    public static OutputTarget VR_OUTPUT_TARGET = OutputTarget.MAIN_TARGET;

    private static final TriFunction<GpuTextureView, Boolean, OutputTarget, RenderType> ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_LINEAR = ClientUtils.memoize(
        (gpuTexture, depthAlways, output) -> RenderType.create("entity_translucent_vr",
            setUndistorted(
                setGpuTextures(
                    RenderSetup.builder(depthAlways ? VRShaders.ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT :
                            VRShaders.ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .sortOnUpload()
                        .setOutputTarget(output)
                        .createRenderSetup(),
                    Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                        VRShaders::getGuiSampler))))));

    private static final BiFunction<Identifier, OutputTarget, RenderType> ENTITY_TRANSLUCENT_HAND = Util.memoize(
        (texture, output) -> RenderType.create("entity_translucent_vr",
            setUndistorted(
                RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                    .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, texture)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setOutputTarget(output)
                    .createRenderSetup())));

    private static final TriFunction<GpuTextureView, Boolean, OutputTarget, RenderType> ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR = ClientUtils.memoize(
        (gpuTexture, depthAlways, output) -> RenderType.create("entity_translucent_no_fog_vr",
            setUndistorted(
                setFogOverride(
                    setGpuTextures(
                        RenderSetup.builder(depthAlways ? VRShaders.ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT :
                                VRShaders.ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT)
                            .useLightmap()
                            .useOverlay()
                            .affectsCrumbling()
                            .sortOnUpload()
                            .setOutputTarget(output)
                            .createRenderSetup(),
                        Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                            VRShaders::getGuiSampler))),
                    FogRenderer.FogMode.NONE))));

    private static final BiFunction<GpuTextureView, Boolean, RenderType> ENTITY_SOLID_NO_CARDINAL_LIGHT = Util.memoize(
        (gpuTexture, linear) -> RenderType.create("entity_solid_vr",
            setGpuTextures(
                RenderSetup.builder(VRShaders.ENTITY_SOLID_NO_CARDINAL_LIGHT)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .createRenderSetup(),
                Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                    () -> RenderSystem.getSamplerCache()
                        .getClampToEdge(linear ? FilterMode.LINEAR : FilterMode.NEAREST))))));

    private static final TriFunction<GpuTextureView, Boolean, OutputTarget, RenderType> ENTITY_CUTOUT_NO_CARDINAL_LIGHT_LINEAR = ClientUtils.memoize(
        (gpuTexture, depthAlways, output) -> RenderType.create("entity_cutout_vr",
            setGpuTextures(
                RenderSetup.builder(depthAlways ? VRShaders.ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT :
                        VRShaders.ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutputTarget(output)
                    .createRenderSetup(),
                Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                    VRShaders::getGuiSampler)))));

    private static final TriFunction<GpuTextureView, Boolean, OutputTarget, RenderType> ENTITY_CUTOUT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR = ClientUtils.memoize(
        (gpuTexture, depthAlways, output) -> RenderType.create("entity_cutout_no_fog_vr",
            setFogOverride(
                setGpuTextures(
                    RenderSetup.builder(depthAlways ? VRShaders.ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT :
                            VRShaders.ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .setOutputTarget(output)
                        .createRenderSetup(),
                    Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                        VRShaders::getGuiSampler))),
                FogRenderer.FogMode.NONE)));

    private static final TriFunction<GpuTextureView, Boolean, OutputTarget, RenderType> GUI_TEXTURED_VIEW = ClientUtils.memoize(
        (gpuTexture, depthAlways, output) -> RenderType.create("gui_textured_always_vr",
            setUndistorted(
                setGpuTextures(
                    RenderSetup.builder(depthAlways ? VRShaders.GUI_TEXTURED_ALWAYS : VRShaders.GUI_TEXTURED)
                        .setOutputTarget(output)
                        .createRenderSetup(),
                    Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                        VRShaders::getGuiSampler))))));

    private static final TriFunction<Identifier, Boolean, OutputTarget, RenderType> GUI_TEXTURED = ClientUtils.memoize(
        (identifier, depthAlways, output) -> RenderType.create("gui_textured_vr",
            RenderSetup.builder(depthAlways ? VRShaders.GUI_TEXTURED_ALWAYS : VRShaders.GUI_TEXTURED)
                .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, identifier)
                .setOutputTarget(output)
                .createRenderSetup()));

    private static final RenderType END_PORTAL_VR = RenderType.create("end_portal_vr",
        RenderSetup.builder(VRShaders.END_PORTAL_VR_PIPELINE)
            .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, AbstractEndPortalRenderer.END_SKY_LOCATION)
            .withTexture(VRShaders.CORE_OVERLAY_SAMPLER, AbstractEndPortalRenderer.END_PORTAL_LOCATION)
            .createRenderSetup());

    private static final RenderType END_GATEWAY_VR = RenderType.create("end_gateway_vr",
        RenderSetup.builder(VRShaders.END_GATEWAY_VR_PIPELINE)
            .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, AbstractEndPortalRenderer.END_SKY_LOCATION)
            .withTexture(VRShaders.CORE_OVERLAY_SAMPLER, AbstractEndPortalRenderer.END_PORTAL_LOCATION)
            .createRenderSetup());

    private static final BiFunction<Identifier, Boolean, RenderType> CROSSHAIR_WORLD = Util.memoize(
        (identifier, depthAlways) -> RenderType.create("crosshair_world_vr",
            RenderSetup.builder(depthAlways ? VRShaders.CROSSHAIR_WORLD_ALWAYS : VRShaders.CROSSHAIR_WORLD)
                .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, identifier)
                .useLightmap()
                .useOverlay()
                .createRenderSetup()));

    private static final Function<OutputTarget, RenderType> QUADS = Util.memoize(
        output -> RenderType.create("quads_vr",
            setUndistorted(
                RenderSetup.builder(VRShaders.QUADS)
                    .setOutputTarget(output)
                    .createRenderSetup())));

    private static final Function<OutputTarget, RenderType> QUADS_ALWAYS = Util.memoize(
        output -> RenderType.create("quads_always_vr",
            setUndistorted(
                RenderSetup.builder(VRShaders.QUADS_ALWAYS)
                    .setOutputTarget(output)
                    .createRenderSetup())));

    private static final RenderType LINE_STRIP = RenderType.create("line_strip_vr",
        RenderSetup.builder(VRShaders.LINE_STRIP)
            .createRenderSetup());

    private static final Function<Identifier, RenderType> TEXT_NO_CULL = Util.memoize(
        identifier -> RenderType.create("text_no_cull_vr",
            RenderSetup.builder(VRShaders.TEXT_NO_CULL)
                .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, identifier)
                .useLightmap()
                .createRenderSetup()));

    private static RenderSetup setGpuTextures(
        RenderSetup renderSetup, Map<String, RenderSetupExtension.GpuTextureBinding> gpuTextures)
    {
        return ((RenderSetupExtension) (Object) renderSetup).vivecraft$setGpuTextures(gpuTextures);
    }

    private static RenderSetup setFogOverride(RenderSetup renderSetup, FogRenderer.FogMode override) {
        return ((RenderSetupExtension) (Object) renderSetup).vivecraft$setFogOverride(override);
    }

    private static RenderSetup setUndistorted(RenderSetup renderSetup) {
        return ((RenderSetupExtension) (Object) renderSetup).vivecraft$setUndistorted();
    }

    public static RenderType crosshairWorld(Identifier identifier, boolean depthAlways) {
        return CROSSHAIR_WORLD.apply(identifier, depthAlways);
    }

    public static RenderType linesStrip() {
        return LINE_STRIP;
    }

    public static RenderType quads(boolean depthAlways) {
        return depthAlways ? QUADS_ALWAYS.apply(VR_OUTPUT_TARGET) : QUADS.apply(VR_OUTPUT_TARGET);
    }

    public static RenderType endGateWayVR() {
        return END_GATEWAY_VR;
    }

    public static RenderType endPortalVR() {
        return END_PORTAL_VR;
    }

    public static RenderType entitySolidNoCardinalLight(GpuTextureView texture, boolean linearFilter) {
        return ENTITY_SOLID_NO_CARDINAL_LIGHT.apply(texture, linearFilter);
    }

    public static RenderType entityCutoutNoCardinalLightLinear(
        GpuTextureView texture, boolean depthAlways, boolean noFog)
    {
        return noFog ?
            ENTITY_CUTOUT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR.apply(texture, depthAlways, VR_OUTPUT_TARGET) :
            ENTITY_CUTOUT_NO_CARDINAL_LIGHT_LINEAR.apply(texture, depthAlways, VR_OUTPUT_TARGET);
    }

    public static RenderType entityTranslucentNoCardinalLightLinear(
        GpuTextureView texture, boolean depthAlways, boolean noFog)
    {
        return noFog ?
            ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR.apply(texture, depthAlways, VR_OUTPUT_TARGET) :
            ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_LINEAR.apply(texture, depthAlways, VR_OUTPUT_TARGET);
    }

    public static RenderType entityTranslucentHand(Identifier texture) {
        return ENTITY_TRANSLUCENT_HAND.apply(texture, VR_OUTPUT_TARGET);
    }

    public static RenderType guiTextured(Identifier identifier) {
        return guiTextured(identifier, false);
    }

    public static RenderType guiTextured(Identifier identifier, boolean depthAlways) {
        return GUI_TEXTURED.apply(identifier, depthAlways, VR_OUTPUT_TARGET);
    }

    public static RenderType guiTextured(GpuTextureView texture, boolean depthAlways) {
        return GUI_TEXTURED_VIEW.apply(texture, depthAlways, VR_OUTPUT_TARGET);
    }

    public static RenderType textNoCull(Identifier identifier) {
        return TEXT_NO_CULL.apply(identifier);
    }
}
