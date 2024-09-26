package org.vivecraft.mixin.server;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

import java.util.IllegalFormatException;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow
    @Final
    public MinecraftServer server;

    public ServerPlayerMixin(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "initInventoryMenu", at = @At("TAIL"))
    private void vivecraft$addItemEasterEgg(CallbackInfo ci) {
        // triggers on player respawn and rejoin
        ServerVivePlayer serverVivePlayer = vivecraft$getVivePlayer();
        if (ServerConfig.vrFun.get() && serverVivePlayer != null && serverVivePlayer.isVR() &&
            this.random.nextInt(40) == 3)
        {
            ItemStack easterEggItem;
            if (this.random.nextInt(2) == 1) {
                easterEggItem = new ItemStack(Items.PUMPKIN_PIE)
                    .setHoverName(Component.literal("EAT ME"));
            } else {
                easterEggItem = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)
                    .setHoverName(Component.literal("DRINK ME"));
            }

            easterEggItem.getOrCreateTag().putInt("HideFlags", 32);

            if (this.getInventory().add(easterEggItem)) {
                this.inventoryMenu.broadcastChanges();
            }
        }
    }

    @Inject(method = "doTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;tick()V", shift = Shift.AFTER))
    private void vivecraft$overridePose(CallbackInfo ci) {
        ServerVRPlayers.overridePose((ServerPlayer) (Object) this);
    }

    @Override
    public void sweepAttack() {
        ServerVivePlayer serverviveplayer = vivecraft$getVivePlayer();

        if (serverviveplayer != null && serverviveplayer.isVR()) {
            // spawn particles at controller, have to assume controller 0
            Vec3 aim = serverviveplayer.getControllerDir(0);
            float yaw = (float) Math.toDegrees(Math.atan2(aim.x, -aim.z));

            double xOffset = -Mth.sin(yaw * ((float) Math.PI / 180F));
            double zOffset = Mth.cos(yaw * ((float) Math.PI / 180F));

            Vec3 pos = serverviveplayer.getControllerPos(0);

            if (this.level() instanceof ServerLevel) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.SWEEP_ATTACK,
                    pos.x + xOffset, pos.y, pos.z + zOffset, 0, xOffset, 0.0D, zOffset, 0.0D);
            }
        } else {
            super.sweepAttack();
        }
    }

    // TODO: this is not needed
    @Override
    protected void triggerItemUseEffects(ItemStack stack, int amount) {
        if (!stack.isEmpty() && this.isUsingItem()) {
            if (stack.getUseAnimation() == UseAnim.DRINK) {
                this.playSound(this.getDrinkingSound(stack), 0.5F, this.level().random.nextFloat() * 0.1F + 0.9F);
            }

            if (stack.getUseAnimation() == UseAnim.EAT) {
                this.vivecraft$addItemParticles(stack, amount);
                this.playSound(this.getEatingSound(stack), 0.5F + 0.5F * (float) this.random.nextInt(2),
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    // TODO: this should override "spawnItemParticles", or inject into LivingEntity
    @Unique
    private void vivecraft$addItemParticles(ItemStack stack, int count) {
        ServerVivePlayer serverviveplayer = vivecraft$getVivePlayer();
        for (int i = 0; i < count; ++i) {
            Vec3 dir = new Vec3(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            dir = dir.xRot(-this.getXRot() * ((float) Math.PI / 180F));
            dir = dir.yRot(-this.getYRot() * ((float) Math.PI / 180F));
            double yOffset = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3 pos = new Vec3(((double) this.random.nextFloat() - 0.5D) * 0.3D, yOffset, 0.6D);
            pos = pos.xRot(-this.getXRot() * ((float) Math.PI / 180F));
            pos = pos.yRot(-this.getYRot() * ((float) Math.PI / 180F));
            pos = pos.add(this.getX(), this.getEyeY(), this.getZ());
            if (serverviveplayer != null && serverviveplayer.isVR()) {
                InteractionHand interactionhand = this.getUsedItemHand();

                if (interactionhand == InteractionHand.MAIN_HAND) {
                    pos = serverviveplayer.getControllerPos(0);
                } else {
                    pos = serverviveplayer.getControllerPos(1);
                }
            }
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z, dir.x,
                dir.y + 0.05D, dir.z);
        }
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z")
    )
    private void vivecraft$dropVive(
        ItemStack droppedItem, boolean dropAround, boolean includeThrowerName, CallbackInfoReturnable<ItemEntity> cir,
        @Local ItemEntity item)
    {
        ServerVivePlayer serverVivePlayer = vivecraft$getVivePlayer();
        if (!dropAround && serverVivePlayer != null && serverVivePlayer.isVR()) {
            // spawn item from players hand
            Vec3 pos = serverVivePlayer.getControllerPos(0);
            Vec3 aim = serverVivePlayer.getControllerDir(0);

            // item speed, taken from Player#drop
            final float speed = 0.3F;
            item.setDeltaMovement(aim.x * speed, aim.y * speed, aim.z * speed);
            item.setPos(pos.x + item.getDeltaMovement().x,
                pos.y + item.getDeltaMovement().y,
                pos.z + item.getDeltaMovement().z);
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void vivecraft$checkCanGetHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = source.getEntity();
        ServerPlayer other = null;

        // check if the damage came from another player
        if (entity instanceof ServerPlayer) {
            other = (ServerPlayer) entity;
        } else if ((entity instanceof AbstractArrow && (((AbstractArrow) entity).getOwner() instanceof ServerPlayer))) {
            other = (ServerPlayer) ((AbstractArrow) entity).getOwner();
        }

        if (other != null) {
            // both entities are players, so need to check

            ServerVivePlayer otherVive = ServerVRPlayers.getVivePlayer(other);
            ServerVivePlayer thisVive = vivecraft$getVivePlayer();

            // create new object, if they are null, simplifies the checks
            if (otherVive == null) {
                otherVive = new ServerVivePlayer(other);
            }

            if (thisVive == null) {
                thisVive = new ServerVivePlayer((ServerPlayer) (Object) this);
            }

            boolean blockedDamage = false;
            String blockedDamageCase = "";

            if ((!otherVive.isVR() && thisVive.isVR() && thisVive.isSeated())
                || (!thisVive.isVR() && otherVive.isVR() && otherVive.isSeated())) {
                // nonvr vs Seated
                if (!ServerConfig.pvpSEATEDVRvsNONVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled nonvr vs seated VR damage";
                }
            } else if ((!otherVive.isVR() && thisVive.isVR() && !thisVive.isSeated())
                || (!thisVive.isVR() && otherVive.isVR() && !otherVive.isSeated())) {
                // nonvr vs Standing
                if (!ServerConfig.pvpVRvsNONVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled nonvr vs standing VR damage";
                }
            } else if ((otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated())
                || (thisVive.isVR() && thisVive.isSeated() && otherVive.isVR() && !otherVive.isSeated())) {
                // Standing vs Seated
                if (!ServerConfig.pvpVRvsSEATEDVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled seated VR vs standing VR damage";
                }
            } else if (otherVive.isVR() && !otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated()) {
                // Standing vs Standing
                if (!ServerConfig.pvpVRvsVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled standing VR vs standing VR damage";
                }
            } else if (otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && thisVive.isSeated()) {
                // Seated vs Seated
                if (!ServerConfig.pvpSEATEDVRvsSEATEDVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled seated VR vs seated VR damage";
                }
            }
            if (blockedDamage) {
                if (ServerConfig.pvpNotifyBlockedDamage.get()) {
                    other.sendSystemMessage(Component.literal(blockedDamageCase));
                }
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void vivecraft$customDeathMessage(DamageSource damageSource, CallbackInfo ci) {
        // only when enabled
        if (ServerConfig.messagesEnabled.get()) {
            ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer((ServerPlayer) (Object) this);
            String message = "";
            String entity = "";

            // get the right message
            if (damageSource.getEntity() != null) {
                entity = damageSource.getEntity().getName().plainCopy().getString();
                // death by mob
                if (vivePlayer == null) {
                    message = ServerConfig.messagesDeathByMobVanilla.get();
                } else if (!vivePlayer.isVR()) {
                    message = ServerConfig.messagesDeathByMobNonVR.get();
                } else if (vivePlayer.isSeated()) {
                    message = ServerConfig.messagesDeathByMobSeated.get();
                } else {
                    message = ServerConfig.messagesDeathByMobVR.get();
                }
            }

            if (message.isEmpty()) {
                // general death, of if the mob one isn't set
                if (vivePlayer == null) {
                    message = ServerConfig.messagesDeathVanilla.get();
                } else if (!vivePlayer.isVR()) {
                    message = ServerConfig.messagesDeathNonVR.get();
                } else if (vivePlayer.isSeated()) {
                    message = ServerConfig.messagesDeathSeated.get();
                } else {
                    message = ServerConfig.messagesDeathVR.get();
                }
            }

            // actually send the message, if there is one set
            if (!message.isEmpty()) {
                try {
                    this.server.getPlayerList().broadcastSystemMessage(Component.literal(message.formatted(getName().getString(), entity)), false);
                } catch (IllegalFormatException e) {
                    // catch errors users might put into the messages, to not crash other stuff
                    ServerNetworking.LOGGER.error("Death message '{}' has errors: {}", message, e.toString());
                }
            }
        }
    }

    @Unique
    private ServerVivePlayer vivecraft$getVivePlayer() {
        return ServerVRPlayers.getVivePlayer((ServerPlayer) (Object) this);
    }
}
