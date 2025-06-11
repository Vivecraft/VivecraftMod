package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vivecraft.mixin.server.ServerPlayerMixin;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {

    @Shadow
    public abstract Inventory getInventory();

    @Shadow
    @Final
    public InventoryMenu inventoryMenu;

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * dummy to be overridden in {@link ServerPlayerMixin}
     */
    @WrapOperation(method = "sweepAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    protected int vivecraft$modifySweepParticleSpawnPos(
        ServerLevel instance, ParticleOptions type, double posX, double posY, double posZ, int particleCount,
        double xOffset, double yOffset, double zOffset, double speed, Operation<Integer> original)
    {
        return original.call(instance, type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);
    }

    /**
     * dummy to be overridden in {@link ServerPlayerMixin}
     */
    @ModifyArg(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    protected float vivecraft$damageModifier(float damage) {
        return damage;
    }
}
