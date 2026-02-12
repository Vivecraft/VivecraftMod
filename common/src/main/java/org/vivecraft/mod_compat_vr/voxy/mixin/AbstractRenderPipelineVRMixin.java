package org.vivecraft.mod_compat_vr.voxy.mixin;

import me.cortex.voxy.client.core.AbstractRenderPipeline;
import me.cortex.voxy.client.core.rendering.util.DepthFramebuffer;
import org.spongepowered.asm.mixin.*;
import org.vivecraft.client_vr.extensions.FieldDependentMixin;

// voxy 0.2.6+ has this in AbstractRenderPipeline
@Pseudo
@FieldDependentMixin("fb")
@Mixin(AbstractRenderPipeline.class)
public abstract class AbstractRenderPipelineVRMixin {

    @Final
    @Mutable
    @Shadow(remap = false)
    public DepthFramebuffer fb;
}
