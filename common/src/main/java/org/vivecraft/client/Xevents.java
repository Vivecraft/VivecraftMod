package org.vivecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface Xevents {

    /**
     * checks if someone wants to cancel the in block overlay
     * @param player Player to check for
     * @param poseStack PoseStack used for rendering
     * @param blockState blockState of the block the camera is in
     * @param blockPos position of the block the camera is in
     * @return true if the rendering was canceled
     */
    @ExpectPlatform
    static boolean renderBlockOverlay(Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos) {
        return false;
    }

    /**
     * checks if someone wants to cancel the water overlay
     * @param player Player to check for
     * @param poseStack PoseStack used for rendering
     * @return true if the rendering was canceled
     */
    @ExpectPlatform
    static boolean renderWaterOverlay(Player player, PoseStack poseStack) {
        return false;
    }

    /**
     * checks if someone wants to cancel the fire overlay
     * @param player Player to check for
     * @param poseStack PoseStack used for rendering
     * @return true if the rendering was canceled
     */
    @ExpectPlatform
    static boolean renderFireOverlay(Player player, PoseStack poseStack) {
        return false;
    }
}
