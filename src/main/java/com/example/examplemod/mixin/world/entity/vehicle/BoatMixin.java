package com.example.examplemod.mixin.world.entity.vehicle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.Boat;

//TODO needed?
@Mixin(Boat.class)
public class BoatMixin {

	@ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 0), method = "controlBoat()V")
	public float inputLeft(float f) {
		 Minecraft minecraft = Minecraft.getInstance();
		 float f1 = minecraft.player.input.leftImpulse;
		 return -f1;
	}
	
	@ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 1), method = "controlBoat()V")
	public float inputRight(float f) {
		 Minecraft minecraft = Minecraft.getInstance();
		 float f1 = minecraft.player.input.leftImpulse;
		 return f1;
	}
}
