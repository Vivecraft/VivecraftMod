package org.vivecraft.fabric.mixin;

import com.google.common.collect.Streams;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.client.gui.pip.GuiFBTPlayerRenderer;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(GameRenderer.class)
public class FabricGameRendererMixin {
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;<init>(Lnet/minecraft/client/gui/render/state/GuiRenderState;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;Ljava/util/List;)V"))
    private List<PictureInPictureRenderer<?>> vivecraft$addFBTPLayerRendererFabric(
        List<PictureInPictureRenderer<?>> pips)
    {
        return Streams.concat(pips.stream(), Stream.of(new GuiFBTPlayerRenderer(this.renderBuffers.bufferSource())))
            .collect(Collectors.toList());
    }
}
