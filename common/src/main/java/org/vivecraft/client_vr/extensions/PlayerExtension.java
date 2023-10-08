package org.vivecraft.client_vr.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

public interface PlayerExtension {

    int vivecraft$getMovementTeleportTimer();

    void vivecraft$setMovementTeleportTimer(int value);

    void vivecraft$setTeleported(boolean teleported);

    void vivecraft$setItemInUseClient(ItemStack itemstack1, InteractionHand interactionhand);

    void vivecraft$setItemInUseCountClient(int i);

    boolean vivecraft$isClimbeyClimbEquipped();

    void vivecraft$stepSound(BlockPos blockpos, double soundPosX, double soundPosY, double soundPosZ);

    void vivecraft$swingArm(InteractionHand interactionhand, VRFirstPersonArmSwing interact);

    boolean vivecraft$isClimbeyJumpEquipped();

    float vivecraft$getMuhJumpFactor();

    float vivecraft$getMuhSpeedFactor();

    double vivecraft$getRoomYOffsetFromPose();

    boolean vivecraft$getInitFromServer();
}
