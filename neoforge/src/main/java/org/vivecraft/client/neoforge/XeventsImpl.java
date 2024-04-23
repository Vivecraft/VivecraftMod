package org.vivecraft.client.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.event.EventHooks;

public class XeventsImpl {

    public static boolean renderBlockOverlay(Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos) {
        return ClientHooks.renderBlockOverlay(player, poseStack, RenderBlockScreenEffectEvent.OverlayType.BLOCK, blockState, blockPos);
    }

    public static boolean renderWaterOverlay(Player player, PoseStack poseStack) {
        return ClientHooks.renderWaterOverlay(player, poseStack);
    }

    public static boolean renderFireOverlay(Player player, PoseStack poseStack) {
        return ClientHooks.renderFireOverlay(player, poseStack);
    }

    public static void onRenderTickStart(float partialTicks) {
        EventHooks.onRenderTickStart(partialTicks);
    }

    public static void onRenderTickEnd(float partialTicks) {
        EventHooks.onRenderTickEnd(partialTicks);
    }
}
