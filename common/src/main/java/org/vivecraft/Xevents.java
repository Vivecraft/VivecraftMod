package org.vivecraft;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface Xevents {

    Xevents INSTANCE = Services.load(Xevents.class);

    /**
     * checks if someone wants to cancel the in block overlay
     *
     * @param player            Player to check for
     * @param playerRenderState PlayerRenderState of the player
     * @param blockState        blockState of the block the camera is in
     * @param blockPos          position of the block the camera is in
     * @param camera            Camera object
     * @param worldPartialTick  partial tick of the world
     * @param playerPartialTick partial tick of the player, in case it is frozen
     * @return true if the rendering was canceled
     */
    boolean extractBlockOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, BlockState blockState, BlockPos blockPos,
        Camera camera, float worldPartialTick, float playerPartialTick);

    /**
     * checks if someone wants to cancel the water overlay
     *
     * @param player            Player to check for
     * @param playerRenderState PlayerRenderState of the player
     * @param camera            Camera object
     * @param worldPartialTick  partial tick of the world
     * @param playerPartialTick partial tick of the player, in case it is frozen
     * @return true if the rendering was canceled
     */
    boolean extractWaterOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, Camera camera, float worldPartialTick,
        float playerPartialTick);

    /**
     * checks if someone wants to cancel the fire overlay
     *
     * @param player            Player to check for
     * @param playerRenderState PlayerRenderState of the player
     * @param camera            Camera object
     * @param worldPartialTick  partial tick of the world
     * @param playerPartialTick partial tick of the player, in case it is frozen
     * @return true if the rendering was canceled
     */
    boolean extractFireOverlay(
        LocalPlayer player, PlayerRenderState playerRenderState, Camera camera, float worldPartialTick,
        float playerPartialTick);
}
