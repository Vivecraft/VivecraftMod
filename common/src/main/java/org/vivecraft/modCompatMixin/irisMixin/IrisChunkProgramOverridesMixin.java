package org.vivecraft.modCompatMixin.irisMixin;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
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
import org.vivecraft.ClientDataHolder;
import org.vivecraft.extensions.iris.PipelineManagerExtension;
import org.vivecraft.render.RenderPass;

import java.util.EnumMap;

@Mixin(IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixin {

    private final EnumMap<RenderPass, EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>>> pipelinePrograms = new EnumMap<>(RenderPass.class);
    @Shadow(remap = false)
    private GlProgram<IrisChunkShaderInterface> createShader(IrisTerrainPass pass, SodiumTerrainPipeline pipeline){
        return null;
    }

    @Inject(method = "createShaders", at = @At("HEAD"), remap = false)
    public void createAllPipelinesShaders(SodiumTerrainPipeline pipeline, ChunkVertexType vertexType, CallbackInfo ci){
        for (RenderPass renderPass : RenderPass.values()) {
            WorldRenderingPipeline worldPipeline = ((PipelineManagerExtension)Iris.getPipelineManager()).getVRPipeline(renderPass);
            // GUI and unused renderPasses don't have a pipeline
            if (worldPipeline != null) {
                SodiumTerrainPipeline sodiumPipeline = worldPipeline.getSodiumTerrainPipeline();
                EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> renderPassShaders = new EnumMap<>(IrisTerrainPass.class);
                pipelinePrograms.put(renderPass, renderPassShaders);
                if (sodiumPipeline != null) {
                    sodiumPipeline.patchShaders(vertexType);
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

    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE", target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    public Object deleteVRPipelineShaders(EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> instance, Object key){
        // return shader of the current RenderPass
        return pipelinePrograms.get(ClientDataHolder.getInstance().currentPass).get((IrisTerrainPass)key);
    }

    @Inject(method = "deleteShaders", at = @At("HEAD"), remap = false)
    public void deleteVRPipelineShaders(CallbackInfo ci){
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
