package org.vivecraft.mixin.client.renderer;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.PostChain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    // to render item activation outside GameRenderer
    @Invoker
    void callRenderItemActivationAnimation(int a, int b, float c);

    // to have different postEffects per pass
    @Accessor
    void setPostEffect(PostChain postChain);
}
