package org.vivecraft.client_vr.render.rendertypes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.vivecraft.client_vr.render.VRShaders;

import java.util.function.BiFunction;
import java.util.function.Function;

public class VRRenderTypes {

    private static final BiFunction<GpuTexture, Boolean, RenderType> ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT = Util.memoize(
        (gpuTexture, depthAlways) -> RenderType.create("entity_translucent_vr", 1536, true, true,
            depthAlways ? VRShaders.ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT :
                VRShaders.ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT,
            RenderType.CompositeState.builder()
                .setTextureState(getTextureState(gpuTexture))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false)));

    private static final Function<GpuTexture, RenderType> ENTITY_SOLID_NO_CARDINAL_LIGHT = Util.memoize(
        gpuTexture -> RenderType.create("entity_solid_vr", 1536, true, false,
            VRShaders.ENTITY_SOLID_NO_CARDINAL_LIGHT, RenderType.CompositeState.builder()
                .setTextureState(getTextureState(gpuTexture))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false)));

    private static final BiFunction<GpuTexture, Boolean, RenderType> ENTITY_CUTOUT_NO_CARDINAL_LIGHT = Util.memoize(
        (gpuTexture, depthAlways) -> RenderType.create("entity_cutout_vr", 1536, true, false,
            depthAlways ? VRShaders.ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT :
                VRShaders.ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT,
            RenderType.CompositeState.builder()
                .setTextureState(getTextureState(gpuTexture))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false)));

    private static final Function<GpuTexture, RenderType> GUI_TEXTURED_OVERLAY = Util.memoize(
        gpuTexture -> RenderType.create("gui_textured_overlay_vr", 1536, false, false,
            RenderPipelines.GUI_TEXTURED_OVERLAY, RenderType.CompositeState.builder()
                .setTextureState(getTextureState(gpuTexture))
                .createCompositeState(false)));

    private static final RenderType END_PORTAL_VR = RenderType.create("end_portal_vr", 1536, false, false,
        VRShaders.END_PORTAL_VR_PIPELINE, RenderType.CompositeState.builder()
            .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                .build())
            .createCompositeState(false));

    private static final RenderType END_GATEWAY_VR = RenderType.create("end_gateway_vr", 1536, false, false,
        VRShaders.END_GATEWAY_VR_PIPELINE, RenderType.CompositeState.builder()
            .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                .build())
            .createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> CROSSHAIR_MENU = Util.memoize(
        resourceLocation -> RenderType.create("crosshair_menu_vr", 1536, false, false,
            VRShaders.CROSSHAIR_MENU, RenderType.CompositeState.builder()
                .setTextureState(getTextureState(resourceLocation))
                .createCompositeState(false)));

    private static final BiFunction<ResourceLocation, Boolean, RenderType> CROSSHAIR_WORLD = Util.memoize(
        (resourceLocation, depthAlways) -> RenderType.create("crosshair_world_vr", 1536, false, false,
            depthAlways ? VRShaders.CROSSHAIR_WORLD_ALWAYS : VRShaders.CROSSHAIR_WORLD,
            RenderType.CompositeState.builder()
                .setTextureState(getTextureState(resourceLocation))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .createCompositeState(false)));

    private static final RenderType QUADS = RenderType.create("quads_vr", 1536, false, false,
        VRShaders.QUADS, RenderType.CompositeState.builder().createCompositeState(false));

    private static final RenderType QUADS_ALWAYS = RenderType.create("quads_always_vr", 1536, false, false,
        VRShaders.QUADS_ALWAYS, RenderType.CompositeState.builder().createCompositeState(false));

    private static final RenderType TRIANGLES_ALWAYS = RenderType.create("triangles_always_vr", 1536, false, false,
        VRShaders.TRIANGLES_ALWAYS, RenderType.CompositeState.builder().createCompositeState(false));

    private static final RenderType TRIANGLE_FAN_ALWAYS = RenderType.create("triangle_fan_always_vr", 1536, false,
        false, VRShaders.TRIANGLE_FAN_ALWAYS, RenderType.CompositeState.builder().createCompositeState(false));

    private static final Function<ResourceLocation, RenderType> TEXT_NO_CULL = Util.memoize(
        resourceLocation -> RenderType.create("text_no_cull_vr", 1536, false, false,
            VRShaders.TEXT_NO_CULL, RenderType.CompositeState.builder()
                .setTextureState(getTextureState(resourceLocation))
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .createCompositeState(false)));

    private static final Function<ResourceLocation, RenderType> WEATHER_NO_LIGHTMAP_CHANGE = Util.memoize(
        resourceLocation -> RenderType.create("weather_menuworld", 1536, false, false,
            RenderPipelines.WEATHER_NO_DEPTH_WRITE, RenderType.CompositeState.builder()
                .setTextureState(getTextureState(resourceLocation))
                .createCompositeState(false)));

    private static RenderStateShard.EmptyTextureStateShard getTextureState(GpuTexture texture) {
        return new RenderStateShard.EmptyTextureStateShard(() -> RenderSystem.setShaderTexture(0, texture), () -> {});
    }

    private static RenderStateShard.EmptyTextureStateShard getTextureState(ResourceLocation resourceLocation) {
        return new RenderStateShard.TextureStateShard(resourceLocation, TriState.FALSE, false);
    }

    public static RenderType crosshairMenu(ResourceLocation resourceLocation) {
        return CROSSHAIR_MENU.apply(resourceLocation);
    }

    public static RenderType crosshairWorld(ResourceLocation resourceLocation, boolean depthAlways) {
        return CROSSHAIR_WORLD.apply(resourceLocation, depthAlways);
    }

    public static RenderType quads(boolean depthAlways) {
        return depthAlways ? QUADS_ALWAYS : QUADS;
    }

    public static RenderType trianglesAlways() {
        return TRIANGLES_ALWAYS;
    }

    public static RenderType triangleFanAlways() {
        return TRIANGLE_FAN_ALWAYS;
    }

    public static RenderType endGateWayVR() {
        return END_GATEWAY_VR;
    }

    public static RenderType endPortalVR() {
        return END_PORTAL_VR;
    }

    public static RenderType entitySolidNoCardinalLight(GpuTexture texture) {
        return ENTITY_SOLID_NO_CARDINAL_LIGHT.apply(texture);
    }

    public static RenderType entityCutoutNoCardinalLight(GpuTexture texture, boolean depthAlways) {
        return ENTITY_CUTOUT_NO_CARDINAL_LIGHT.apply(texture, depthAlways);
    }

    public static RenderType entityTranslucentNoCardinalLight(GpuTexture texture, boolean depthAlways) {
        return ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT.apply(texture, depthAlways);
    }

    public static RenderType guiTextureOverlay(GpuTexture texture) {
        return GUI_TEXTURED_OVERLAY.apply(texture);
    }

    public static RenderType textNoCull(ResourceLocation resourceLocation) {
        return TEXT_NO_CULL.apply(resourceLocation);
    }

    public static RenderType weatherNoLightmapChange(ResourceLocation resourceLocation) {
        return WEATHER_NO_LIGHTMAP_CHANGE.apply(resourceLocation);
    }
}
