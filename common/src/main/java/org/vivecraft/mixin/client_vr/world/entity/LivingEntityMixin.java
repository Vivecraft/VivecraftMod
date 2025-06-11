package org.vivecraft.mixin.client_vr.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.mixin.client.player.AbstractClientPlayerMixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    public abstract InteractionHand getUsedItemHand();

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * dummy to be overridden in {@link AbstractClientPlayerMixin}
     */
    @WrapOperation(method = "spawnItemParticles", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    protected void vivecraft$modifyEatParticles(
        Level instance, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed,
        double zSpeed, Operation<Void> original)
    {
        original.call(instance, particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
