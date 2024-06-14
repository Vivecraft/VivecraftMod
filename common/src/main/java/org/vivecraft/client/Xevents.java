package org.vivecraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.DeltaTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface Xevents {

    @ExpectPlatform
    static boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos) {
        return false;
    }

    @ExpectPlatform
    static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return false;
    }

    @ExpectPlatform
    static boolean renderFireOverlay(Player player, PoseStack mat) {
        return false;
    }

    @ExpectPlatform
    static void onRenderTickStart(DeltaTracker partialTick) {

    }

    @ExpectPlatform
    static void onRenderTickEnd(DeltaTracker partialTick) {

    }
}
