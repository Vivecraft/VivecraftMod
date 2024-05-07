package org.vivecraft.client.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.event.EventHooks;

public class XeventsImpl {

    public static boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos) {
        return ClientHooks.renderBlockOverlay(player, mat, RenderBlockScreenEffectEvent.OverlayType.BLOCK, state, pos);
    }

    public static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return ClientHooks.renderWaterOverlay(player, mat);
    }

    public static boolean renderFireOverlay(Player player, PoseStack mat) {
        return ClientHooks.renderFireOverlay(player, mat);
    }

    public static void onRenderTickStart(float f) {
        ClientHooks.fireClientTickPre();
    }

    public static void onRenderTickEnd(float f) {
        ClientHooks.fireRenderFramePost(f);
    }
}
