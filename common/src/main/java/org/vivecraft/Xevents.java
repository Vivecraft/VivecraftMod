package org.vivecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface Xevents {

    @ExpectPlatform
    public static boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos) {
        return false;
    }

    @ExpectPlatform
    public static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return false;
    }

    @ExpectPlatform
    public static boolean renderFireOverlay(Player player, PoseStack mat) {
        return false;
    }

    @ExpectPlatform
    public static void onRenderTickStart(float f) {

    }

    @ExpectPlatform
    public static void onRenderTickEnd(float f) {

    }

    @ExpectPlatform
    public static void drawScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick) {

    }


}
