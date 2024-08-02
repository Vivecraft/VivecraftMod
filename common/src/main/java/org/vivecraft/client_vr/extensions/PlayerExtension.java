package org.vivecraft.client_vr.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

public interface PlayerExtension {

    /**
     * @return current movementTeleportTimer
     */
    int vivecraft$getMovementTeleportTimer();

    /**
     * set current movementTeleportTimer
     */
    void vivecraft$setMovementTeleportTimer(int value);

    /**
     * sets if the player used the vivecraft teleport to move
     * @param teleported if the player teleported
     */
    void vivecraft$setTeleported(boolean teleported);

    /**
     * sets the given ItemStack as used by the player
     * @param itemStack ItemStack to use
     * @param interactionHand hand that used the ItemStack
     */
    void vivecraft$setItemInUseClient(ItemStack itemStack, InteractionHand interactionHand);

    /**
     * sets the ticks the used item has remaining
     * @param remaining ticks remaining to use
     */
    void vivecraft$setItemInUseRemainingClient(int remaining);

    /**
     * @return if the player has a climbing claw item in either hand
     */
    boolean vivecraft$isClimbeyClimbEquipped();

    /**
     * plays the sound of the block at {@code blockPos} at the {@code soundPos} location, in the level the player is in.
     * @param blockPos position of the block for the sound, in world space
     * @param soundPos position of where to play the sound, in world space
     */
    void vivecraft$stepSound(BlockPos blockPos, Vec3 soundPos);

    /**
     * sets the swingType and swings the player arm
     * @param interactionHand hand to swing
     * @param swingType swing animation to use
     */
    void vivecraft$swingArm(InteractionHand interactionHand, VRFirstPersonArmSwing swingType);

    /**
     * @return jump factor of the block the player is standing on, with vertical player motion applied
     */
    float vivecraft$getMuhJumpFactor();

    /**
     * @return speed factor of the block the player is standing on, with horizontal player motion applied
     */
    float vivecraft$getMuhSpeedFactor();

    /**
     * @return vertical offset of the player from the current pose
     */
    double vivecraft$getRoomYOffsetFromPose();

    /**
     * @return if the players position got initialized from the server
     */
    boolean vivecraft$getInitFromServer();
}
