package org.vivecraft.fabric;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.vivecraft.Xevents;

public class XeventsImpl implements Xevents {

    @Override
    public boolean extractBlockOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, BlockState blockState, BlockPos blockPos,
        Camera camera, float worldPartialTick, float playerPartialTick)
    {
        return false;
    }

    @Override
    public boolean extractWaterOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, Camera camera, float worldPartialTick,
        float playerPartialTick)
    {
        return false;
    }

    @Override
    public boolean extractFireOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, Camera camera, float worldPartialTick,
        float playerPartialTick)
    {
        return false;
    }
}
