package org.vivecraft.extensions;

import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.render.VRFirstPersonArmSwing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface PlayerExtension {

	int getMovementTeleportTimer();
	void setMovementTeleportTimer(int value);
	void addMovementTeleportTimer(int value);
	
	boolean hasTeleported();
	void setTeleported(boolean teleported);

	void setItemInUseClient(ItemStack itemstack1, InteractionHand interactionhand);

	void setItemInUseCountClient(int i);

	boolean isClimbeyClimbEquipped();

	void stepSound(BlockPos blockpos, Vec3 vec3);

	void swingArm(InteractionHand interactionhand, VRFirstPersonArmSwing interact);

	boolean isClimbeyJumpEquipped();
	float getMuhJumpFactor();
	float getMuhSpeedFactor();
	double getRoomYOffsetFromPose();
	boolean getInitFromServer();
	void updateSyncFields(LocalPlayer old);
}
