package org.vivecraft.neoforge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import org.vivecraft.Xevents;

public class XeventsImpl implements Xevents {

    @Override
    public boolean renderBlockOverlay(
        Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos,
        SubmitNodeCollector submitNodeCollector)
    {
        return ClientHooks.renderBlockOverlay(player, poseStack, RenderBlockScreenEffectEvent.OverlayType.BLOCK,
            blockState, blockPos, Minecraft.getInstance().getAtlasManager(), submitNodeCollector);
    }

    @Override
    public boolean renderWaterOverlay(Player player, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        return ClientHooks.renderWaterOverlay(player, poseStack, Minecraft.getInstance().getAtlasManager(),
            submitNodeCollector);
    }

    @Override
    public boolean renderFireOverlay(Player player, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        return ClientHooks.renderFireOverlay(player, poseStack, Minecraft.getInstance().getAtlasManager(),
            submitNodeCollector);
    }
}
