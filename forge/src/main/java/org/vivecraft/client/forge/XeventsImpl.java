package org.vivecraft.client.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ForgeEventFactoryClient;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;

public class XeventsImpl {

    public static boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos) {
        return ForgeHooksClient.renderBlockOverlay(player, mat, RenderBlockScreenEffectEvent.OverlayType.BLOCK, state, pos);
    }

    public static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderWaterOverlay(player, mat);
    }

    public static boolean renderFireOverlay(Player player, PoseStack mat) {
        return ForgeHooksClient.renderFireOverlay(player, mat);
    }

    public static void onRenderTickStart(DeltaTracker partialTick) {
        ForgeEventFactoryClient.onRenderTickStart(partialTick);
    }

    public static void onRenderTickEnd(DeltaTracker partialTick) {
        ForgeEventFactoryClient.onRenderTickEnd(partialTick);
    }
}
