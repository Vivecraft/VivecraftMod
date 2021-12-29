package com.example.examplemod.mixin.client.renderer;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.example.examplemod.ItemInHandRendererExtension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererVRMixin implements ItemInHandRendererExtension {

	@Shadow
	private Minecraft minecraft;

	@Override
	public Triple<Float, BlockState, BlockPos> getNearOpaqueBlock(Vec3 in, double dist) {
		if (this.minecraft.level == null) {
			return null;
		} else {
			AABB aabb = new AABB(in.subtract(dist, dist, dist), in.add(dist, dist, dist));
			Stream<BlockPos> stream = BlockPos.betweenClosedStream(aabb).filter((bp) -> {
				return this.minecraft.level.getBlockState(bp).isViewBlocking(this.minecraft.level, bp);
			});
			Optional<BlockPos> optional = stream.findFirst();
			return optional.isPresent()
					? Triple.of(1.0F, this.minecraft.level.getBlockState(optional.get()), optional.get())
					: null;
		}
	}

//	@Override
//	public void renderArmWithItem(LocalPlayer player, float partialTicks, float f, InteractionHand mainHand,
//			float attackAnim, ItemStack itemstack, float g, PoseStack matrix,
//			BufferSource multibuffersource$buffersource, int packedLightCoords) {
//
//	}

	@Override
	public boolean isInsideOpaqueBlock(Vec3 in) {
		if (this.minecraft.level == null) {
			return false;
		} else {
			BlockPos blockpos = new BlockPos(in);
			return this.minecraft.level.getBlockState(blockpos).isSolidRender(this.minecraft.level, blockpos);
		}
	}

}
