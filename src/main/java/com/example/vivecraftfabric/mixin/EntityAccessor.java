package com.example.vivecraftfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.Entity;

@Mixin(Entity.class)
public interface EntityAccessor {

	@Accessor("eyeHeight")
	public void setEyeHeight(float eyeHeight);
}
