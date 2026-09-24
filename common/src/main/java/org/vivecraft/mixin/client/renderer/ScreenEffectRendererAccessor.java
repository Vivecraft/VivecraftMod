package org.vivecraft.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * This accessor is used instead of an access widener, because of <a href="https://github.com/neoforged/FancyModLoader/issues/453">this</a> NeoForge bug, so this can be reverted to an access widener once that is fixed.
 */
@Mixin(ScreenEffectRenderer.class)
public interface ScreenEffectRendererAccessor {
    @Invoker
    void invokeRenderItemActivationAnimation(
        PlayerRenderState playerRenderState, PoseStack poseStack, float partialTicks,
        SubmitNodeCollector submitNodeCollector);
}
