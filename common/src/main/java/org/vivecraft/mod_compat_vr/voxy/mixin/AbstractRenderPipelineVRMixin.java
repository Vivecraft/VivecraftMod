package org.vivecraft.mod_compat_vr.voxy.mixin;

import me.cortex.voxy.client.core.AbstractRenderPipeline;
import me.cortex.voxy.client.core.rendering.util.DepthFramebuffer;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(AbstractRenderPipeline.class)
public abstract class AbstractRenderPipelineVRMixin {

    @Final
    @Mutable
    @Shadow
    public DepthFramebuffer fb;
}
