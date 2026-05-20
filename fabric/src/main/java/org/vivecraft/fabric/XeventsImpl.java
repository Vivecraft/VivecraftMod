package org.vivecraft.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.vivecraft.Xevents;

public class XeventsImpl implements Xevents {

    @Override
    public boolean renderBlockOverlay(
        Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos)
    {
        return false;
    }

    @Override
    public boolean renderWaterOverlay(Player player, PoseStack poseStack) {
        return false;
    }

    @Override
    public boolean renderFireOverlay(Player player, PoseStack poseStack) {
        return false;
    }
}
