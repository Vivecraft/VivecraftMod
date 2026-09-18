package org.vivecraft.neoforge;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.CustomBlockScreenEffectRenderer;
import org.vivecraft.Xevents;

public class XeventsImpl implements Xevents {

    @Override
    public boolean extractBlockOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, BlockState blockState, BlockPos blockPos,
        Camera camera, float worldPartialTick, float playerPartialTick)
    {
        return ClientHooks.extractBlockScreenEffect(player, playerRenderState, blockPos,
            blockState, camera, worldPartialTick, playerPartialTick);
    }

    @Override
    public boolean extractWaterOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, Camera camera, float worldPartialTick,
        float playerPartialTick)
    {
        return ClientHooks.extractWaterScreenEffect(player, playerRenderState, camera, worldPartialTick,
            playerPartialTick);
    }

    @Override
    public boolean extractFireOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, Camera camera, float worldPartialTick,
        float playerPartialTick)
    {
        ClientHooks.extractFireScreenEffect(player, playerRenderState, camera, worldPartialTick, playerPartialTick);
        return playerRenderState.customFireOverlayRenderer == CustomBlockScreenEffectRenderer.NO_OP;
    }
}
