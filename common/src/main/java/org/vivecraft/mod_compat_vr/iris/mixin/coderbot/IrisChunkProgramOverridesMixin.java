package org.vivecraft.mod_compat_vr.iris.mixin.coderbot;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import net.coderbot.iris.Iris;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkShaderInterface;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisTerrainPass;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import net.coderbot.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_xr.render_pass.WorldRenderPass;
import org.vivecraft.mod_compat_vr.iris.extensions.IrisChunkProgramOverridesExtension;
import org.vivecraft.mod_compat_vr.iris.extensions.PipelineManagerExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.EnumMap;

@Pseudo
@Mixin(IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixin implements IrisChunkProgramOverridesExtension {

    @Shadow(remap = false)
    @Final
    private EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> programs;

    @Unique
    private final EnumMap<RenderPass, EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>>> vivecraft$pipelinePrograms = new EnumMap<>(RenderPass.class);

    @Override
    @Unique
    public void vivecraft$createAllPipelinesShadersSodiumProcessing(Object sodiumTerrainPipeline, Object chunkVertexType, Method createShadersMethod) throws InvocationTargetException, IllegalAccessException {
        if (VRState.vrInitialized) {
            WorldRenderPass current = RenderPassManager.wrp;
            RenderPass currentPass = ClientDataHolderVR.getInstance().currentPass;

            RenderPassManager.renderPassType = RenderPassType.WORLD_ONLY;
            for (RenderPass renderPass : RenderPass.values()) {
                VRSettings.logger.info("Creating VR sodium shaders for RenderPass {}", renderPass);

                WorldRenderingPipeline worldPipeline = (WorldRenderingPipeline) ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getVRPipeline(renderPass);
                // GUI and unused renderPasses don't have a pipeline
                if (worldPipeline != null) {
                    SodiumTerrainPipeline sodiumPipeline = worldPipeline.getSodiumTerrainPipeline();
                    EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> renderPassShaders = new EnumMap<>(IrisTerrainPass.class);
                    vivecraft$pipelinePrograms.put(renderPass, renderPassShaders);
                    createShadersMethod.invoke(this, sodiumPipeline, chunkVertexType);
                    // programs should have  the shaders for this pass now
                    renderPassShaders.putAll(programs);
                    // clear it now, since programs is for the vanilla pass
                    programs.clear();
                }
            }

            RenderPassManager.setVanillaRenderPass();
            VRSettings.logger.info("Creating sodium shaders for vanilla RenderPass");
            createShadersMethod.invoke(this, ((WorldRenderingPipeline) ((PipelineManagerExtension) Iris.getPipelineManager()).vivecraft$getVanillaPipeline()).getSodiumTerrainPipeline(), chunkVertexType);
            if (current != null) {
                RenderPassManager.setWorldRenderPass(current);
                ClientDataHolderVR.getInstance().currentPass = currentPass;
            }
        } else {
            createShadersMethod.invoke(this, sodiumTerrainPipeline, chunkVertexType);
        }
    }

    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE", target = "Ljava/util/EnumMap;get(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    public Object vivecraft$getVRPipelineShaders(EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> instance, Object key) {
        // return shader of the current RenderPass
        return !RenderPassType.isVanilla() ? vivecraft$pipelinePrograms.get(ClientDataHolderVR.getInstance().currentPass).get((IrisTerrainPass) key) : instance.get((IrisTerrainPass) key);
    }

    @Inject(method = "deleteShaders", at = @At("HEAD"), remap = false)
    public void vivecraft$deleteVRPipelineShaders(CallbackInfo ci) {
        if (VRState.vrInitialized) {
            for (EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> map : vivecraft$pipelinePrograms.values()) {
                for (GlProgram<?> program : map.values()) {
                    if (program != null) {
                        program.delete();
                    }
                }
                map.clear();
            }
            vivecraft$pipelinePrograms.clear();
        }
    }
}
