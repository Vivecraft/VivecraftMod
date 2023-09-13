package org.vivecraft.client_vr.extensions;

import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;

public interface ItemInHandRendererExtension {

	Triple<Float, BlockState, BlockPos> vivecraft$getNearOpaqueBlock(Vec3 position, double minClipDistance);

	boolean vivecraft$isInsideOpaqueBlock(Vec3 vec31);

	void vivecraft$setSwingType(VRFirstPersonArmSwing interact);
}
