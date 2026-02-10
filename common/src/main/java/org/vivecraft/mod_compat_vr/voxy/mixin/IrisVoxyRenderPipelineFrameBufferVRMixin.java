package org.vivecraft.mod_compat_vr.voxy.mixin;

import me.cortex.voxy.client.core.IrisVoxyRenderPipeline;
import me.cortex.voxy.client.core.rendering.util.DepthFramebuffer;
import org.spongepowered.asm.mixin.*;
import org.vivecraft.client_vr.extensions.FieldDependentMixin;

// voxy 0.2.5 has this in IrisVoxyRenderPipeline
@Pseudo
@FieldDependentMixin("fb")
@Mixin(IrisVoxyRenderPipeline.class)
public abstract class IrisVoxyRenderPipelineFrameBufferVRMixin {

    @Final
    @Mutable
    @Shadow(remap = false)
    public DepthFramebuffer fb;
}
