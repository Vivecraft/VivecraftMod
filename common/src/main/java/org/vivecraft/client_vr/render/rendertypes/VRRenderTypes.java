package org.vivecraft.client_vr.render.rendertypes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.vivecraft.client.extensions.RenderSetupExtension;
import org.vivecraft.client_vr.render.VRShaders;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class VRRenderTypes {

    private static final BiFunction<GpuTextureView, Boolean, RenderType> ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_LINEAR = Util.memoize(
        (gpuTexture, depthAlways) -> RenderType.create("entity_translucent_vr",
            setUndistorted(
                setGpuTextures(
                    RenderSetup.builder(depthAlways ? VRShaders.ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT :
                            VRShaders.ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT)
                        .setOitPipelines(depthAlways ? VRShaders.OIT_ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT :
                            VRShaders.OIT_ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .sortOnUpload()
                        .createRenderSetup(),
                    Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                        VRShaders::getGuiSampler))))));

    private static final BiFunction<GpuTextureView, Boolean, RenderType> ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR = Util.memoize(
        (gpuTexture, depthAlways) -> RenderType.create("entity_translucent_no_fog_vr",
            setUndistorted(
                setFogOverride(
                    setGpuTextures(
                        RenderSetup.builder(depthAlways ? VRShaders.ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT :
                                VRShaders.ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT)
                            .setOitPipelines(depthAlways ? VRShaders.OIT_ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT :
                                VRShaders.OIT_ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT)
                            .useLightmap()
                            .useOverlay()
                            .affectsCrumbling()
                            .sortOnUpload()
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

    private static final BiFunction<GpuTextureView, Boolean, RenderType> ENTITY_CUTOUT_NO_CARDINAL_LIGHT_LINEAR = Util.memoize(
        (gpuTexture, depthAlways) -> RenderType.create("entity_cutout_vr",
            setGpuTextures(
                RenderSetup.builder(depthAlways ? VRShaders.ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT :
                        VRShaders.ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .createRenderSetup(),
                Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                    VRShaders::getGuiSampler)))));

    private static final BiFunction<GpuTextureView, Boolean, RenderType> ENTITY_CUTOUT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR = Util.memoize(
        (gpuTexture, depthAlways) -> RenderType.create("entity_cutout_no_fog_vr",
            setFogOverride(
                setGpuTextures(
                    RenderSetup.builder(depthAlways ? VRShaders.ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT :
                            VRShaders.ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .createRenderSetup(),
                    Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                        VRShaders::getGuiSampler))),
                FogRenderer.FogMode.NONE)));

    private static final BiFunction<GpuTextureView, Boolean, RenderType> GUI_TEXTURED_VIEW = Util.memoize(
        (gpuTexture, depthAlways) -> RenderType.create("gui_textured_always_vr",
            setUndistorted(
                setGpuTextures(
                    RenderSetup.builder(depthAlways ? VRShaders.GUI_TEXTURED_ALWAYS : VRShaders.GUI_TEXTURED)
                        .createRenderSetup(),
                    Map.of(VRShaders.CORE_TEXTURE_SAMPLER, new RenderSetupExtension.GpuTextureBinding(gpuTexture,
                        VRShaders::getGuiSampler))))));

    private static final BiFunction<Identifier, Boolean, RenderType> GUI_TEXTURED = Util.memoize(
        (identifier, depthAlways) -> RenderType.create("gui_textured_vr",
            RenderSetup.builder(depthAlways ? VRShaders.GUI_TEXTURED_ALWAYS : VRShaders.GUI_TEXTURED)
                .setOitPipelines(depthAlways ? VRShaders.OIT_GUI_TEXTURED_ALWAYS : VRShaders.OIT_GUI_TEXTURED)
                .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, identifier)
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
                .setOitPipelines(depthAlways ? VRShaders.OIT_CROSSHAIR_WORLD_ALWAYS : VRShaders.OIT_CROSSHAIR_WORLD)
                .withTexture(VRShaders.CORE_TEXTURE_SAMPLER, identifier)
                .useLightmap()
                .useOverlay()
                .createRenderSetup()));

    private static final RenderType QUADS = RenderType.create("quads_vr",
        setUndistorted(
            RenderSetup.builder(VRShaders.QUADS)
                .setOitPipelines(VRShaders.OIT_QUADS)
                .createRenderSetup()));

    private static final RenderType QUADS_ALWAYS = RenderType.create("quads_always_vr",
        setUndistorted(
            RenderSetup.builder(VRShaders.QUADS_ALWAYS)
                .setOitPipelines(VRShaders.OIT_QUADS_ALWAYS)
                .createRenderSetup()));

    private static final RenderType LINE_STRIP = RenderType.create("line_strip_vr",
        RenderSetup.builder(VRShaders.LINE_STRIP)
            .setOitPipelines(VRShaders.OIT_LINE_STRIP)
            .createRenderSetup());

    private static final Function<Identifier, RenderType> TEXT_NO_CULL = Util.memoize(
        identifier -> RenderType.create("text_no_cull_vr",
            RenderSetup.builder(VRShaders.TEXT_NO_CULL)
                .setOitPipelines(VRShaders.OIT_TEXT_NO_CULL)
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
        return depthAlways ? QUADS_ALWAYS : QUADS;
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
            ENTITY_CUTOUT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR.apply(texture, depthAlways) :
            ENTITY_CUTOUT_NO_CARDINAL_LIGHT_LINEAR.apply(texture, depthAlways);
    }

    public static RenderType entityTranslucentNoCardinalLightLinear(
        GpuTextureView texture, boolean depthAlways, boolean noFog)
    {
        return noFog ?
            ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_NO_FOG_LINEAR.apply(texture, depthAlways) :
            ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT_LINEAR.apply(texture, depthAlways);
    }

    public static RenderType entityTranslucentHand(Identifier texture) {
        return RenderTypes.entityTranslucent(texture);
    }

    public static RenderType guiTextured(Identifier identifier) {
        return guiTextured(identifier, false);
    }

    public static RenderType guiTextured(Identifier identifier, boolean depthAlways) {
        return GUI_TEXTURED.apply(identifier, depthAlways);
    }

    public static RenderType guiTextured(GpuTextureView texture, boolean depthAlways) {
        return GUI_TEXTURED_VIEW.apply(texture, depthAlways);
    }

    public static RenderType textNoCull(Identifier identifier) {
        return TEXT_NO_CULL.apply(identifier);
    }
}
