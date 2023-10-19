package org.vivecraft.mixin.accessor.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemInHandRenderer.class)
public interface ItemInHandRendererAccessor {
    // to render vr hands
    @Invoker
    void callRenderArmWithItem(
        AbstractClientPlayer abstractClientPlayer,
        float f,
        float g,
        InteractionHand interactionHand,
        float h,
        ItemStack itemStack,
        float i,
        PoseStack poseStack,
        MultiBufferSource multiBufferSource,
        int j
    );
}
