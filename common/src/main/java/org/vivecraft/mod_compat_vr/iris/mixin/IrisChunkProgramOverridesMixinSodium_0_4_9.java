package org.vivecraft.mod_compat_vr.iris.mixin;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.vertex.type.ChunkVertexType;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisTerrainPass;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;

@Mixin(IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixinSodium_0_4_9 {

    private final EnumMap<RenderPass, EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>>> pipelinePrograms = new EnumMap<>(RenderPass.class);
    @Shadow(remap = false)
    private GlProgram<IrisChunkShaderInterface> createShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline){
        return null;
    }

    @Inject(method = "createShaders(Lnet/coderbot/iris/pipeline/SodiumTerrainPipeline;Lme/jellysquid/mods/sodium/client/render/vertex/type/ChunkVertexType;)V", at = @At("HEAD"), remap = false)
    public void createAllPipelinesShadersSodium_0_4_9(SodiumTerrainPipeline pipeline, ChunkVertexType vertexType, CallbackInfo ci){
        if (VRState.vrInitialized) {
            RenderPassManager.renderPassType = RenderPassType.WORLD_ONLY;
            for (RenderPass renderPass : RenderPass.values()) {
                Iris.logger.info("Creating VR sodium shaders for RenderPass {}", renderPass);

                WorldRenderingPipeline worldPipeline = ((PipelineManagerExtension) Iris.getPipelineManager()).getVRPipeline(renderPass);
                // GUI and unused renderPasses don't have a pipeline
                if (worldPipeline != null) {
                    SodiumTerrainPipeline sodiumPipeline = worldPipeline.getSodiumTerrainPipeline();
                    EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> renderPassShaders = new EnumMap<>(IrisTerrainPass.class);
                    pipelinePrograms.put(renderPass, renderPassShaders);
                    if (sodiumPipeline != null) {
                        try {
                            sodiumPipeline.getClass().getMethod("patchShaders", ChunkVertexType.class).invoke(sodiumPipeline, vertexType);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            // this shouldn't happen if everything worked correctly
                            throw new RuntimeException("couldn't find 'patchShaders'");
                        }
                        for (IrisTerrainPass pass : IrisTerrainPass.values()) {
                            if (pass.isShadow() && !sodiumPipeline.hasShadowPass()) {
                                renderPassShaders.put(pass, null);
                                continue;
                            }

                            renderPassShaders.put(pass, createShader(pass, sodiumPipeline));
                        }
                    } else {
                        renderPassShaders.clear();
                    }
                }
            }
        }
        RenderPassManager.setVanillaRenderPass();
    }

    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE", target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    public Object deleteVRPipelineShaders(EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> instance, Object key){
        // return shader of the current RenderPass
        return !RenderPassType.isVanilla() ? pipelinePrograms.get(ClientDataHolderVR.getInstance().currentPass).get((IrisTerrainPass)key) : instance.get((IrisTerrainPass)key);
    }

    @Inject(method = "deleteShaders", at = @At("HEAD"), remap = false)
    public void deleteVRPipelineShaders(CallbackInfo ci){
        if (VRState.vrInitialized) {
            for (EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> map : pipelinePrograms.values()) {
                for (GlProgram<?> program : map.values()) {
                    if (program != null) {
                        program.delete();
                    }
                }
                map.clear();
            }
            pipelinePrograms.clear();
        }
    }
}
