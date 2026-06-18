package org.vivecraft.client_vr.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.*;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.render.helpers.ShaderHelper;
import org.vivecraft.client_vr.render.ubos.LanczosUBO;
import org.vivecraft.client_vr.render.ubos.MixedRealityUBO;
import org.vivecraft.client_vr.render.ubos.PostProcessUBO;

import java.util.Optional;
import java.util.OptionalDouble;

public class VRShaders {

    public static final String CORE_TEXTURE_SAMPLER = "Sampler0";
    private static final BindGroupLayout CORE_TEXTURE_LAYOUT = BindGroupLayouts.SAMPLER0;
    public static final String CORE_OVERLAY_SAMPLER = "Sampler1";
    private static final BindGroupLayout CORE_OVERLAY_LAYOUT = BindGroupLayouts.SAMPLER1;
    public static final String CORE_LIGHTMAP_SAMPLER = "Sampler2";
    private static final BindGroupLayout CORE_LIGHTMAP_LAYOUT = BindGroupLayouts.SAMPLER2;

    // FSAA shader and its uniforms
    public static LanczosUBO LANCZOS_UBO;
    public static final String LANCZOS_COLOR_SAMPLER = "Sampler0";
    public static final String LANCZOS_DEPTH_SAMPLER = "Sampler1";
    public static final BindGroupLayout LANCZOS_BIND_GROUP = BindGroupLayout.builder()
        .withUniform(LanczosUBO.UBO_NAME, UniformType.UNIFORM_BUFFER)
        .withSampler(LANCZOS_COLOR_SAMPLER)
        .withSampler(LANCZOS_DEPTH_SAMPLER).build();

    public static final RenderPipeline LANCZOS_PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/vivecraft_lanczos"))
        .withVertexShader(Identifier.fromNamespaceAndPath("vivecraft", "core/lanczos_vr"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("vivecraft", "core/lanczos_vr"))
        .withBindGroupLayout(LANCZOS_BIND_GROUP)
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .build();

    // mixed reality shader and its uniforms
    public static MixedRealityUBO MIXED_REALITY_UBO;
    public static final String MIXED_REALITY_FIRST_COLOR_SAMPLER = "firstPersonColor";
    public static final String MIXED_REALITY_THIRD_COLOR_SAMPLER = "thirdPersonColor";
    public static final String MIXED_REALITY_THIRD_DEPTH_SAMPLER = "thirdPersonDepth";
    public static final String MIXED_REALITY_GUI_COLOR_SAMPLER = "guiColor";
    public static final int MIXED_REALITY_GUI_FIRST = 1;
    public static final int MIXED_REALITY_GUI_THIRD = 2;
    public static final int MIXED_REALITY_GUI_SEPARATE = 4;
    public static final BindGroupLayout MIXED_REALITY_BIND_GROUP = BindGroupLayout.builder()
        .withUniform(MixedRealityUBO.UBO_NAME, UniformType.UNIFORM_BUFFER)
        .withSampler(MIXED_REALITY_FIRST_COLOR_SAMPLER)
        .withSampler(MIXED_REALITY_THIRD_COLOR_SAMPLER)
        .withSampler(MIXED_REALITY_THIRD_DEPTH_SAMPLER)
        .withSampler(MIXED_REALITY_GUI_COLOR_SAMPLER).build();

    public static final RenderPipeline MIXED_REALITY_PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/vivecraft_mixed_reality"))
        .withVertexShader(Identifier.fromNamespaceAndPath("vivecraft", "core/passthrough_vr"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("vivecraft", "core/mixedreality_vr"))
        .withBindGroupLayout(MIXED_REALITY_BIND_GROUP)
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .build();

    // vr post shader and its uniforms
    public static PostProcessUBO POST_PROCESS_UBO;
    public static final String POST_PROCESSING_COLOR_SAMPLER = "Sampler0";
    public static final BindGroupLayout POST_PROCESS_BIND_GROUP = BindGroupLayout.builder()
        .withUniform(PostProcessUBO.UBO_NAME, UniformType.UNIFORM_BUFFER)
        .withSampler(POST_PROCESSING_COLOR_SAMPLER).build();

    public static final RenderPipeline POST_PROCESSING_PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/vivecraft_post_processing"))
        .withVertexShader(Identifier.fromNamespaceAndPath("vivecraft", "core/passthrough_vr"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("vivecraft", "core/postprocessing_vr"))
        .withBindGroupLayout(POST_PROCESS_BIND_GROUP)
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .build();

    // blit shader
    public static final String BLIT_VR_COLOR_SAMPLER = "DiffuseSampler";
    public static final BindGroupLayout BLIT_VR_BIND_GROUP = BindGroupLayout.builder()
        .withSampler(BLIT_VR_COLOR_SAMPLER).build();

    public static final RenderPipeline BLIT_VR_PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/vivecraft_blit"))
        .withVertexShader(Identifier.fromNamespaceAndPath("vivecraft", "core/passthrough_vr"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("vivecraft", "core/blit_vr"))
        .withBindGroupLayout(BLIT_VR_BIND_GROUP)
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withColorTargetState(
            new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_COLOR))
        .build();

    public static final RenderPipeline BLIT_VR_BLEND_PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/vivecraft_blit"))
        .withVertexShader(Identifier.fromNamespaceAndPath("vivecraft", "core/passthrough_vr"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("vivecraft", "core/blit_vr"))
        .withBindGroupLayout(BLIT_VR_BIND_GROUP)
        .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .build();

    public static final RenderPipeline SOLID_ALPHA_PIPELINE = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/vivecraft_solid_alpha"))
        .withVertexShader(Identifier.fromNamespaceAndPath("vivecraft", "core/black_vr"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("vivecraft", "core/black_vr"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .withColorTargetState(
            new ColorTargetState(Optional.empty(), GpuFormat.RGBA8_UNORM, ColorTargetState.WRITE_ALPHA))
        .build();

    // end portal shaders
    private static final RenderPipeline.Snippet END_PORTAL_SNIPPET = RenderPipeline.builder(
            RenderPipelines.END_PORTAL_SNIPPET)
        .withVertexShader(Identifier.fromNamespaceAndPath("vivecraft", "core/rendertype_end_portal_vr"))
        .withFragmentShader(Identifier.fromNamespaceAndPath("vivecraft", "core/rendertype_end_portal_vr"))
        .buildSnippet();

    public static final RenderPipeline END_PORTAL_VR_PIPELINE = RenderPipeline.builder(END_PORTAL_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/end_portal_vr"))
        .withShaderDefine("PORTAL_LAYERS", 15).build();
    public static final RenderPipeline END_GATEWAY_VR_PIPELINE = RenderPipeline.builder(END_PORTAL_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/end_gateway_vr"))
        .withShaderDefine("PORTAL_LAYERS", 16).build();

    // panorama with alpha color mask
    public static final RenderPipeline SOLID_PANORAMA = RenderPipeline.builder()
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/panorama"))
        .withVertexShader("core/panorama")
        .withFragmentShader("core/panorama")
        .withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION)
        .withBindGroupLayout(CORE_TEXTURE_LAYOUT)
        .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false))
        .withVertexBinding(0, DefaultVertexFormat.POSITION)
        .withPrimitiveTopology(PrimitiveTopology.QUADS).build();

    public static final RenderPipeline GUI_TEXTURED = RenderPipeline.builder(
            RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/gui_textured_vr"))
        .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true)).build();

    public static final RenderPipeline GUI_TEXTURED_ALWAYS = RenderPipeline.builder(
            RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/gui_textured_always_vr"))
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    public static final RenderPipeline CROSSHAIR_MENU = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/crosshair_menu_vr"))
        .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE_MINUS_DST_COLOR, BlendFactor.ZERO,
            BlendFactor.ONE, BlendFactor.ONE)))
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    private static final RenderPipeline.Snippet ENTITY_SNIPPET = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
        .withBindGroupLayout(CORE_OVERLAY_LAYOUT)
        .withCull(false).buildSnippet();

    public static final RenderPipeline CROSSHAIR_WORLD = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/crosshair_world_vr"))
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE_MINUS_DST_COLOR, BlendFactor.ZERO,
            BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA))).build();

    // all those NO_DEPTH_TEST should be ALWAYS_DEPTH_TEST, to also be able to write depth
    // but 1.21.5 doesn't have that
    public static final RenderPipeline CROSSHAIR_WORLD_ALWAYS = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/crosshair_world_always_vr"))
        .withColorTargetState(new ColorTargetState(new BlendFunction(BlendFactor.ONE_MINUS_DST_COLOR, BlendFactor.ZERO,
            BlendFactor.ONE, BlendFactor.ONE_MINUS_SRC_ALPHA)))
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    public static final RenderPipeline ENTITY_SOLID_NO_CARDINAL_LIGHT = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/entity_solid_no_cardinal_light_vr"))
        .withShaderDefine("NO_CARDINAL_LIGHTING").build();

    public static final RenderPipeline ENTITY_TRANSLUCENT_NO_CARDINAL_LIGHT = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/entity_translucent_no_cardinal_light_vr"))
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT)).build();

    public static final RenderPipeline ENTITY_TRANSLUCENT_ALWAYS_NO_CARDINAL_LIGHT = RenderPipeline.builder(
            ENTITY_SNIPPET)
        .withLocation(
            Identifier.fromNamespaceAndPath("vivecraft", "pipeline/entity_translucent_always_no_cardinal_light_vr"))
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    public static final RenderPipeline ENTITY_CUTOUT_NO_CULL_NO_CARDINAL_LIGHT = RenderPipeline.builder(ENTITY_SNIPPET)
        .withLocation(
            Identifier.fromNamespaceAndPath("vivecraft", "pipeline/entity_cutout_no_cull_no_cardinal_light_vr"))
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F).build();

    public static final RenderPipeline ENTITY_CUTOUT_NO_CULL_ALWAYS_NO_CARDINAL_LIGHT = RenderPipeline.builder(
            ENTITY_SNIPPET)
        .withLocation(
            Identifier.fromNamespaceAndPath("vivecraft", "pipeline/entity_cutout_no_cull_always_no_cardinal_light_vr"))
        .withShaderDefine("NO_CARDINAL_LIGHTING")
        .withShaderDefine("ALPHA_CUTOUT", 0.1F)
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    public static final RenderPipeline LINE_STRIP = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/debug_line_strip_vr"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.DEBUG_LINE_STRIP)
        .withCull(false)
        .withDepthStencilState(DepthStencilState.DEFAULT)
        .build();

    public static final RenderPipeline QUADS = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/quads_vr"))
        .withCull(false)
        .withDepthStencilState(DepthStencilState.DEFAULT)
        .build();

    public static final RenderPipeline QUADS_ALWAYS = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/quads_always_vr"))
        .withCull(false)
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    public static final RenderPipeline TRIANGLES_ALWAYS = RenderPipeline.builder(
            RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/debug_triangles_vr"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLES)
        .withCull(false)
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    public static final RenderPipeline TRIANGLE_FAN_ALWAYS = RenderPipeline.builder(
            RenderPipelines.DEBUG_FILLED_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/debug_triangle_fan_vr"))
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.TRIANGLE_FAN)
        .withCull(false)
        .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true)).build();

    public static final RenderPipeline TEXT_NO_CULL = RenderPipeline.builder(RenderPipelines.WORLD_TEXT_SNIPPET)
        .withLocation(Identifier.fromNamespaceAndPath("vivecraft", "pipeline/text_no_cull_vr"))
        .withVertexShader("core/text")
        .withFragmentShader("core/text")
        .withCull(false).build();

    private static GpuSampler GUI_SAMPLER;
    private static GpuSampler GUI_SAMPLER_AF;

    private static ProjectionMatrixBuffer UNDISTORTED_PROJ;
    public static GpuBufferSlice UNDISTORTED_PROJ_BUFFER;

    public static GpuBuffer BLIT_QUAD_BUFFER;

    public static GpuSampler getGuiSampler() {
        if (GUI_SAMPLER == null || GUI_SAMPLER_AF == null) {
            updateGuiSampler();
        }
        return ClientDataHolderVR.getInstance().vrSettings.guiAnisotropicFiltering ? GUI_SAMPLER_AF : GUI_SAMPLER;
    }

    public static void updateGuiSampler() {
        if (GUI_SAMPLER != null) {
            GUI_SAMPLER.close();
            GUI_SAMPLER = null;
        }
        if (GUI_SAMPLER_AF != null) {
            GUI_SAMPLER_AF.close();
            GUI_SAMPLER_AF = null;
        }
        GUI_SAMPLER = RenderSystem.getDevice()
            .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR,
                1, OptionalDouble.empty());
        GUI_SAMPLER_AF = RenderSystem.getDevice()
            .createSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.LINEAR,
                RenderSystem.getDevice().getDeviceInfo().limits().maxAnisotropy(), OptionalDouble.empty());
    }

    public static void setUndistortedProj(Matrix4f proj) {
        UNDISTORTED_PROJ_BUFFER = UNDISTORTED_PROJ.getBuffer(proj);
    }

    private VRShaders() {}

    public static void init() {
        MIXED_REALITY_UBO = new MixedRealityUBO();
        POST_PROCESS_UBO = new PostProcessUBO();
        LANCZOS_UBO = new LanczosUBO();
        UNDISTORTED_PROJ = new ProjectionMatrixBuffer("undistorted");
        BLIT_QUAD_BUFFER = RenderSystem.getDevice()
            .createBuffer(() -> "vr blit quad vertex buffer", GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                6L * DefaultVertexFormat.POSITION_TEX.getVertexSize());
    }

    public static void close() {
        if (MIXED_REALITY_UBO != null) {
            MIXED_REALITY_UBO.close();
            MIXED_REALITY_UBO = null;
        }
        if (POST_PROCESS_UBO != null) {
            POST_PROCESS_UBO.close();
            POST_PROCESS_UBO = null;
        }
        if (LANCZOS_UBO != null) {
            LANCZOS_UBO.close();
            LANCZOS_UBO = null;
        }
        if (GUI_SAMPLER != null) {
            GUI_SAMPLER.close();
            GUI_SAMPLER = null;
        }
        if (GUI_SAMPLER_AF != null) {
            GUI_SAMPLER_AF.close();
            GUI_SAMPLER_AF = null;
        }
        if (UNDISTORTED_PROJ != null) {
            UNDISTORTED_PROJ.close();
            UNDISTORTED_PROJ = null;
            UNDISTORTED_PROJ_BUFFER = null;
        }
        if (BLIT_QUAD_BUFFER != null) {
            BLIT_QUAD_BUFFER.close();
            BLIT_QUAD_BUFFER = null;
        }
        ShaderHelper.close();
    }
}
