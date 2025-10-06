package org.vivecraft.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.ForgeEventFactory;
import org.vivecraft.Xevents;

public class XeventsImpl implements Xevents {

    public static boolean renderBlockOverlay(
        Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos)
    {
        return ForgeEventFactory.renderBlockOverlay(player, poseStack, RenderBlockOverlayEvent.OverlayType.BLOCK,
            blockState, blockPos);
    }

    public static boolean renderWaterOverlay(Player player, PoseStack poseStack) {
        return ForgeEventFactory.renderWaterOverlay(player, poseStack);
    }

    public static boolean renderFireOverlay(Player player, PoseStack poseStack) {
        return ForgeEventFactory.renderFireOverlay(player, poseStack);
    }
}
