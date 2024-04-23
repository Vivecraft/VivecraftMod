package org.vivecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface Xevents {

    @ExpectPlatform
    static boolean renderBlockOverlay(Player player, PoseStack poseStack, BlockState blockState, BlockPos blockPos) {
        return false;
    }

    @ExpectPlatform
    static boolean renderWaterOverlay(Player player, PoseStack poseStack) {
        return false;
    }

    @ExpectPlatform
    static boolean renderFireOverlay(Player player, PoseStack poseStack) {
        return false;
    }

    @ExpectPlatform
    static void onRenderTickStart(float partialTicks) {

    }

    @ExpectPlatform
    static void onRenderTickEnd(float partialTicks) {

    }
}
