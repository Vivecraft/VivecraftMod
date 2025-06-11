package org.vivecraft.mixin.client_vr.renderer;

import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PostChain.class)
public class PostChainVRMixin {
    // TODO figure out how to use stencils when applicable, if that is even in here
}
