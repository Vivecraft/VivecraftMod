package org.vivecraft.mixin.accessor.client.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {
    // to create custom END shader
    @NotNull
    @Invoker
    static RenderType.CompositeRenderType callCreate(
        String string,
        VertexFormat vertexFormat,
        VertexFormat.Mode mode,
        int i,
        boolean bl,
        boolean bl2,
        RenderType.CompositeState compositeStat
    ) {
        return null;
    }

}
