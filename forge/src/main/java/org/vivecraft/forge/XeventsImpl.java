package org.vivecraft.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.ForgeEventFactory;

public class XeventsImpl {

    public static boolean renderBlockOverlay(Player player, PoseStack mat, BlockState state, BlockPos pos) {
        return ForgeEventFactory.renderBlockOverlay(player, mat, RenderBlockOverlayEvent.OverlayType.BLOCK, state, pos);
    }

    public static boolean renderWaterOverlay(Player player, PoseStack mat) {
        return ForgeEventFactory.renderWaterOverlay(player, mat);
    }

    public static boolean renderFireOverlay(Player player, PoseStack mat) {
        return ForgeEventFactory.renderFireOverlay(player, mat);
    }

    public static void onRenderTickStart(float f) {
        ForgeEventFactory.onRenderTickStart(f);
    }

    public static void onRenderTickEnd(float f) {
        ForgeEventFactory.onRenderTickEnd(f);
    }

    public static void drawScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        ForgeHooksClient.drawScreen(screen, poseStack, mouseX, mouseY, partialTick);
    }
}
