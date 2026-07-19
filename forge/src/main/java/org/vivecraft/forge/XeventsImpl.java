package org.vivecraft.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import org.vivecraft.Xevents;

public class XeventsImpl implements Xevents {

    @Override
    public boolean renderBlockOverlay(
        Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos,
        SubmitNodeCollector submitNodeCollector)
    {
        return ForgeHooksClient.renderBlockOverlay(player, poseStack, RenderBlockScreenEffectEvent.OverlayType.BLOCK,
            blockState, blockPos);
    }

    @Override
    public boolean renderWaterOverlay(Player player, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        return ForgeHooksClient.renderWaterOverlay(player, poseStack);
    }

    @Override
    public boolean renderFireOverlay(Player player, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        return ForgeHooksClient.renderFireOverlay(player, poseStack);
    }
}
