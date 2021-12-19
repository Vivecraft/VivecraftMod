package com.example.examplemod.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.render.PlayerModelController;

import com.example.examplemod.MinecriftVersion;

import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MinecraftMixin implements MinecriftVersion{

	//TODO why in minecraft and not a constant?
	@ModifyConstant(method = "createTitle", constant = @Constant(stringValue = "Minecraft"))
	private String title(String s) {
		return minecriftVerString;
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sounds/SoundManager;tick(Z)V", shift = Shift.BEFORE), method = "tick()V", cancellable = true)
	public void music(CallbackInfo info) {
		PlayerModelController.getInstance().tick();
	}

}
