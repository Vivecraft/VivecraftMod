package org.vivecraft.mod_compat_vr.iris.mixin;

import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.coderbot.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.mod_compat_vr.iris.extensions.IrisChunkProgramOverridesExtension;

import java.lang.reflect.InvocationTargetException;

@Pseudo
@Mixin(IrisChunkProgramOverrides.class)
public class IrisChunkProgramOverridesMixinSodium_0_4_11 {

    @Group(name = "create sodium shaders", min = 1, max = 1)
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/compat/sodium/impl/shader_overrides/IrisChunkProgramOverrides;createShaders(Lnet/coderbot/iris/pipeline/SodiumTerrainPipeline;Lme/jellysquid/mods/sodium/client/render/chunk/vertex/format/ChunkVertexType;)V"), method = "getProgramOverride", remap = false, expect = 0)
    public void createAllPipelinesShadersSodium_0_4_11(IrisChunkProgramOverrides instance, SodiumTerrainPipeline sodiumTerrainPipeline, ChunkVertexType chunkVertexType) {
        try {
            ((IrisChunkProgramOverridesExtension) this).createAllPipelinesShadersSodiumProcessing(
                    sodiumTerrainPipeline,
                    chunkVertexType,
                    instance.getClass().getMethod("createShaders", SodiumTerrainPipeline.class, ChunkVertexType.class)
            );
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            // this shouldn't happen if everything went well
            throw new RuntimeException(e);
        }
    }
}
