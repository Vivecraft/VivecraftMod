package org.vivecraft.client_vr.extensions;

import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface ItemInHandRendererExtension {

	Triple<Float, BlockState, BlockPos> getNearOpaqueBlock(Vec3 position, double minClipDistance);

	boolean isInsideOpaqueBlock(Vec3 vec31);

	void setSwingType(VRFirstPersonArmSwing interact);
}
