package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class VRShaders {
    // FSAA shader and its uniforms
    public static final String LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM = "texelWidthOffset";
    public static final String LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM = "texelHeightOffset";
    public static final String LANCZOS_COLOR_SAMPLER = "Sampler0";
    public static final String LANCZOS_DEPTH_SAMPLER = "Sampler1";

    public static final RenderPipeline LANCZOS_PIPELINE = RenderPipeline.builder()
        .withLocation("pipeline/vivecraft_lanczos")
        .withVertexShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/lanczos_vr"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/lanczos_vr"))
        .withUniform(LANCZOS_TEXEL_WIDTH_OFFSET_UNIFORM, UniformType.FLOAT)
        .withUniform(LANCZOS_TEXEL_HEIGHT_OFFSET_UNIFORM, UniformType.FLOAT)
        .withSampler(LANCZOS_COLOR_SAMPLER)
        .withSampler(LANCZOS_DEPTH_SAMPLER)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build();

    // mixed reality shader and its uniforms
    public static final String MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM = "hmdViewPosition";
    public static final String MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM = "hmdPlaneNormal";
    public static final String MIXED_REALITY_PROJECTION_MATRIX_UNIFORM = "projectionMatrix";
    public static final String MIXED_REALITY_VIEW_MATRIX_UNIFORM = "viewMatrix";
    public static final String MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM = "firstPersonPass";
    public static final String MIXED_REALITY_KEY_COLOR_UNIFORM = "keyColor";
    public static final String MIXED_REALITY_ALPHA_MODE_UNIFORM = "alphaMode";
    public static final String MIXED_REALITY_FIRST_COLOR_SAMPLER = "firstPersonColor";
    public static final String MIXED_REALITY_THIRD_COLOR_SAMPLER = "thirdPersonColor";
    public static final String MIXED_REALITY_THIRD_DEPTH_SAMPLER = "thirdPersonDepth";

    public static final RenderPipeline MIXED_REALITY_PIPELINE = RenderPipeline.builder()
        .withLocation("pipeline/vivecraft_mixed_reality")
        .withVertexShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/passthrough_vr"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/mixedreality_vr"))
        .withUniform(MIXED_REALITY_HMD_VIEW_POSITION_UNIFORM, UniformType.VEC3)
        .withUniform(MIXED_REALITY_HMD_PLANE_NORMAL_UNIFORM, UniformType.VEC3)
        .withUniform(MIXED_REALITY_PROJECTION_MATRIX_UNIFORM, UniformType.MATRIX4X4)
        .withUniform(MIXED_REALITY_VIEW_MATRIX_UNIFORM, UniformType.MATRIX4X4)
        .withUniform(MIXED_REALITY_FIRST_PERSON_PASS_UNIFORM, UniformType.INT)
        .withUniform(MIXED_REALITY_KEY_COLOR_UNIFORM, UniformType.VEC3)
        .withUniform(MIXED_REALITY_ALPHA_MODE_UNIFORM, UniformType.INT)
        .withSampler(MIXED_REALITY_FIRST_COLOR_SAMPLER)
        .withSampler(MIXED_REALITY_THIRD_COLOR_SAMPLER)
        .withSampler(MIXED_REALITY_THIRD_DEPTH_SAMPLER)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build();

    // vr post shader and its uniforms
    public static final String POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM = "circle_radius";
    public static final String POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM = "circle_offset";
    public static final String POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM = "border";
    public static final String POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM = "blackalpha";
    public static final String POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM = "redalpha";
    public static final String POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM = "bluealpha";
    public static final String POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM = "water";
    public static final String POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM = "portal";
    public static final String POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM = "pumpkin";
    public static final String POST_PROCESSING_OVERLAY_TIME_UNIFORM = "portaltime";
    public static final String POST_PROCESSING_OVERLAY_EYE_UNIFORM = "eye";
    public static final String POST_PROCESSING_COLOR_SAMPLER = "Sampler0";

    public static final RenderPipeline POST_PROCESSING_PIPELINE = RenderPipeline.builder()
        .withLocation("pipeline/vivecraft_post_processing")
        .withVertexShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/passthrough_vr"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/postprocessing_vr"))
        .withUniform(POST_PROCESSING_FOV_REDUCTION_RADIUS_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_FOV_REDUCTION_OFFSET_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_FOV_REDUCTION_BORDER_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_BLACK_ALPHA_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_HEALTH_ALPHA_UNiFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_FREEZE_ALPHA_UNiFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_WATER_AMPLITUDE_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_PORTAL_AMPLITUDE_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_PUMPKIN_AMPLITUDE_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_TIME_UNIFORM, UniformType.FLOAT)
        .withUniform(POST_PROCESSING_OVERLAY_EYE_UNIFORM, UniformType.INT)
        .withSampler(POST_PROCESSING_COLOR_SAMPLER)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build();

    // blit shader
    public static final String BLIT_VR_COLOR_SAMPLER = "DiffuseSampler";

    public static final RenderPipeline BLIT_VR_PIPELINE = RenderPipeline.builder()
        .withLocation("pipeline/vivecraft_blit")
        .withVertexShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/passthrough_vr"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/blit_vr"))
        .withSampler(BLIT_VR_COLOR_SAMPLER)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
        .withDepthWrite(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .build();

    // end portal shaders
    private static final RenderPipeline.Snippet END_PORTAL_SNIPPET = RenderPipeline.builder(
            RenderPipelines.END_PORTAL_SNIPPET)
        .withVertexShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/rendertype_end_portal_vr"))
        .withFragmentShader(ResourceLocation.fromNamespaceAndPath("vivecraft", "core/rendertype_end_portal_vr"))
        .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS).buildSnippet();

    public static final RenderPipeline END_PORTAL_VR_PIPELINE = RenderPipeline.builder(END_PORTAL_SNIPPET)
        .withLocation("pipeline/end_portal_vr")
        .withShaderDefine("PORTAL_LAYERS", 15).build();
    public static final RenderPipeline END_GATEWAY_VR_PIPELINE = RenderPipeline.builder(END_PORTAL_SNIPPET)
        .withLocation("pipeline/end_gateway_vr")
        .withShaderDefine("PORTAL_LAYERS", 16).build();

    // panorama with alpha color mask
    public static final RenderPipeline SOLID_PANORAMA = RenderPipeline.builder(
            RenderPipelines.MATRICES_COLOR_SNIPPET)
        .withLocation("pipeline/panorama")
        .withVertexShader("core/position_tex")
        .withFragmentShader("core/position_tex")
        .withSampler("Sampler0")
        .withDepthWrite(false)
        .withBlend(BlendFunction.PANORAMA)
        .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS).build();

    public static final RenderPipeline CROSSHAIR_MENU = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation("pipeline/crosshair_menu_vr")
        .withBlend(new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO,
            SourceFactor.ONE, DestFactor.ONE))
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();

    private static final RenderPipeline.Snippet ENTITY_SNIPPET = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
        .withSampler("Sampler1")
        .withCull(false).buildSnippet();

    public static final RenderPipeline CROSSHAIR_WORLD = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation("pipeline/crosshair_world_vr")
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withBlend(new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO,
            SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA)).build();

    // all those NO_DEPTH_TEST should be ALWAYS_DEPTH_TEST, to also be able to write depth
    // but 1.21.5 doesn't have that
    public static final RenderPipeline CROSSHAIR_WORLD_ALWAYS = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation("pipeline/crosshair_world_always_vr")
        .withBlend(new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO,
            SourceFactor.ONE, DestFactor.ONE_MINUS_SRC_ALPHA))
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();

    public static final RenderPipeline ENTITY_SOLID_NO_CARDINAL_LIGHT = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation("pipeline/entity_solid_no_cardinal_light_vr")
        .withShaderDefine("NO_CARDINAL_LIGHTING").build();

    public static final RenderPipeline ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation("pipeline/entity_translucent_no_cardinal_light_vr")
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withBlend(BlendFunction.TRANSLUCENT).build();

    public static final RenderPipeline ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT = RenderPipeline.builder(
            ENTITY_SNIPPET)
        .withLocation("pipeline/entity_translucent_always_no_cardinal_light_vr")
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withBlend(BlendFunction.TRANSLUCENT)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();

    public static final RenderPipeline ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation("pipeline/entity_cutout_no_cull_no_cardinal_light_vr")
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F).build();

    public static final RenderPipeline ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT = RenderPipeline.builder(
            ENTITY_SNIPPET)
        .withLocation("pipeline/entity_cutout_no_cull_always_no_cardinal_light_vr")
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();

    public static final RenderPipeline QUADS = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation("pipeline/quads_vr")
        .withCull(false)
        .build();

    public static final RenderPipeline QUADS_ALWAYS = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation("pipeline/quads_always_vr")
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();

    public static final RenderPipeline TRIANGLES_ALWAYS = RenderPipeline.builder(
            RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation("pipeline/debug_triangles_vr")
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();

    public static final RenderPipeline TRIANGLE_FAN_ALWAYS = RenderPipeline.builder(
            RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation("pipeline/debug_triangle_fan_vr")
        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
        .withCull(false)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).build();

    public static final RenderPipeline TEXT_NO_CULL = RenderPipeline.builder(
            RenderPipelines.TEXT_SNIPPET, RenderPipelines.FOG_SNIPPET)
        .withLocation("pipeline/text_no_cull_vr")
        .withVertexShader("core/rendertype_text")
        .withFragmentShader("core/rendertype_text")
        .withSampler("Sampler0")
        .withSampler("Sampler2")
        .withCull(false).build();

    public static final Set<RenderPipeline> DEPTH_ALWAYS_PIPELINES = new HashSet<>(
        Set.of(CROSSHAIR_WORLD_ALWAYS, ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT,
            ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT, QUADS_ALWAYS, TRIANGLES_ALWAYS));

    private VRShaders() {}
}
