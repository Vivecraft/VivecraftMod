package org.vivecraft.client_vr.extensions;

import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface PlayerExtension {

	int getMovementTeleportTimer();
	void setMovementTeleportTimer(int value);
	
	boolean hasTeleported();
	void setTeleported(boolean teleported);

	void setItemInUseClient(ItemStack itemstack1, InteractionHand interactionhand);

	void setItemInUseCountClient(int i);

	boolean isClimbeyClimbEquipped();

	void stepSound(BlockPos blockpos, double soundPosX, double soundPosY, double soundPosZ);

	void swingArm(InteractionHand interactionhand, VRFirstPersonArmSwing interact);

	boolean isClimbeyJumpEquipped();
	float getMuhJumpFactor();
	float getMuhSpeedFactor();
	double getRoomYOffsetFromPose();
	boolean getInitFromServer();
}
