package org.vivecraft.mixin.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.mixin.server.ServerPlayerMixin;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {

    @Shadow
    public abstract Inventory getInventory();

    @Shadow
    @Final
    public InventoryMenu inventoryMenu;

    @Shadow
    public abstract ItemCooldowns getCooldowns();

    public PlayerMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * dummy to be overridden in {@link ServerPlayerMixin}
     */
    @WrapOperation(method = "doSweepAttack*", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
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

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noAttackWhileBlocking(Entity target, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer serverPlayer) {
            ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
            if (!ServerConfig.ALLOW_ATTACKS_WHILE_BLOCKING.get() && vivePlayer != null && vivePlayer.isVR() &&
                this.isBlocking())
            {
                ci.cancel();
            }
        }
    }
}
