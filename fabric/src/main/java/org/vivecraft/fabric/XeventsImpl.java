package org.vivecraft.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.vivecraft.Xevents;

public class XeventsImpl implements Xevents {

    public static boolean renderBlockOverlay(
        Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos)
    {
        return false;
    }

    public static boolean renderWaterOverlay(Player player, PoseStack poseStack) {
        return false;
    }

    public static boolean renderFireOverlay(Player player, PoseStack poseStack) {
        return false;
    }
}
