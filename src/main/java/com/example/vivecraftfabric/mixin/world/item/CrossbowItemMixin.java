package com.example.vivecraftfabric.mixin.world.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.api.ServerVivePlayer;
import org.vivecraft.utils.math.Vector3;

import com.mojang.math.Quaternion;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/world/entity/Entity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"), 
			method = "shootProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V", 
			locals = LocalCapture.CAPTURE_FAILHARD)
	private static void shoot(Level p_40895_, LivingEntity p_40896_, InteractionHand p_40897_, ItemStack p_40898_,
			ItemStack p_40899_, float p_40900_, boolean p_40901_, float p_40902_, float p_40903_, float p_40904_,
			CallbackInfo info, boolean flag, Projectile projectile, Vec3 vec31,
			Quaternion quaternion, Vec3 vec3) {
		ServerVivePlayer serverviveplayer = NetworkHelper.vivePlayers.get(p_40896_.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			vec3 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
			serverviveplayer.getControllerVectorCustom(serverviveplayer.activeHand, new Vector3(0.0F, 1.0F, 0.0F));
		}
	}
}
