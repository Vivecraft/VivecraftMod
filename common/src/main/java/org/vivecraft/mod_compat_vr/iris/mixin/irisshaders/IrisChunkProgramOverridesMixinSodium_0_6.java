package org.vivecraft.mod_compat_vr.iris.mixin.irisshaders;

import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.compat.sodium.impl.shader_overrides.IrisChunkProgramOverrides;
import net.irisshaders.iris.pipeline.SodiumTerrainPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.extensions.ClassDependentMixin;
import org.vivecraft.mod_compat_vr.iris.extensions.IrisChunkProgramOverridesExtension;

import java.lang.reflect.InvocationTargetException;

@Pseudo
@Mixin(IrisChunkProgramOverrides.class)
@ClassDependentMixin("net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType")
public class IrisChunkProgramOverridesMixinSodium_0_6 {

    @Group(name = "create sodium shaders", min = 1, max = 1)
    @Redirect(method = "getProgramOverride", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/compat/sodium/impl/shader_overrides/IrisChunkProgramOverrides;createShaders(Lnet/irisshaders/iris/pipeline/SodiumTerrainPipeline;Lnet/caffeinemc/mods/sodium/client/render/chunk/vertex/format/ChunkVertexType;)V"), remap = false, expect = 0)
    private void vivecraft$createAllPipelinesShadersSodium_0_6(IrisChunkProgramOverrides instance, SodiumTerrainPipeline sodiumTerrainPipeline, ChunkVertexType chunkVertexType) {
        try {
            ((IrisChunkProgramOverridesExtension) this).vivecraft$createAllPipelinesShadersSodiumProcessing(
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
