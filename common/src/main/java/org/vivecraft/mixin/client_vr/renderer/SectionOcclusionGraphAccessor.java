package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.renderer.SectionOcclusionGraph;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(SectionOcclusionGraph.class)
public interface SectionOcclusionGraphAccessor {
    @Accessor
    AtomicBoolean getNeedsFrustumUpdate();
}
