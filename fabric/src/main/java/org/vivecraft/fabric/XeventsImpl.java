package org.vivecraft.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.vivecraft.Xplat;

public class XeventsImpl {

    public static boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos) {
        return false;
    }

    public static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return false;
    }

    public static boolean renderFireOverlay(Player player, PoseStack mat) {
        return false;
    }

    public static void onRenderTickStart(float f) {

    }

    public static void onRenderTickEnd(float f) {

    }

}
