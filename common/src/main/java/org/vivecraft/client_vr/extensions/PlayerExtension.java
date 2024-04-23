package org.vivecraft.client_vr.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

public interface PlayerExtension {

    int vivecraft$getMovementTeleportTimer();

    void vivecraft$setMovementTeleportTimer(int value);

    void vivecraft$setTeleported(boolean teleported);

    void vivecraft$setItemInUseClient(ItemStack itemStack, InteractionHand interactionHand);

    void vivecraft$setItemInUseCountClient(int count);

    boolean vivecraft$isClimbeyClimbEquipped();

    void vivecraft$stepSound(BlockPos blockPos, Vec3 soundPos);

    void vivecraft$swingArm(InteractionHand interactionHand, VRFirstPersonArmSwing swingType);

    float vivecraft$getMuhJumpFactor();

    float vivecraft$getMuhSpeedFactor();

    double vivecraft$getRoomYOffsetFromPose();

    boolean vivecraft$getInitFromServer();
}
