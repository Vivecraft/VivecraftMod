package com.example.examplemod;

import org.apache.commons.lang3.tuple.Triple;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface ItemInHandRendererExtension {

	Triple<Float, BlockState, BlockPos> getNearOpaqueBlock(Vec3 position, double minClipDistance);

	void renderArmWithItem(LocalPlayer player, float partialTicks, float f, InteractionHand mainHand, float attackAnim,
			ItemStack itemstack, float g, PoseStack matrix, BufferSource multibuffersource$buffersource,
			int packedLightCoords);

	boolean isInsideOpaqueBlock(Vec3 vec31);

}
