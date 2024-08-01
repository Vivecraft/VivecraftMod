package org.vivecraft.mixin.server;

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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
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

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(at = @At("TAIL"), method = "initInventoryMenu")
    public void vivecraft$menu(CallbackInfo ci) {
        ServerVivePlayer serverviveplayer = vivecraft$getVivePlayer();
        if (ServerConfig.vrFun.get() && serverviveplayer != null && serverviveplayer.isVR() && this.random.nextInt(40) == 3) {
            ItemStack itemstack;
            if (this.random.nextInt(2) == 1) {
                itemstack = (new ItemStack(Items.PUMPKIN_PIE)).setHoverName(Component.literal("EAT ME"));
            } else {
                itemstack = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)
                    .setHoverName(Component.literal("DRINK ME"));
            }

            itemstack.getTag().putInt("HideFlags", 32);

            if (this.getInventory().add(itemstack)) {
                this.inventoryMenu.broadcastChanges();
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;tick()V", shift = Shift.AFTER), method = "doTick()V")
    public void vivecraft$tick(CallbackInfo info) {
        ServerVRPlayers.overridePose((ServerPlayer) (Object) this);
    }

    @Override
    public void sweepAttack() {
        ServerVivePlayer serverviveplayer = vivecraft$getVivePlayer();

        if (serverviveplayer != null && serverviveplayer.isVR()) {
            Vec3 vec3 = serverviveplayer.getControllerDir(0);
            float f = (float) Math.toDegrees(Math.atan2(vec3.x, -vec3.z));
            double d0 = -Mth.sin(f * ((float) Math.PI / 180F));
            double d1 = Mth.cos(f * ((float) Math.PI / 180F));
            Vec3 vec31 = serverviveplayer.getControllerPos(0, this);

            if (this.level() instanceof ServerLevel) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.SWEEP_ATTACK, vec31.x + d0, vec31.y,
                    vec31.z + d1, 0, d0, 0.0D, d1, 0.0D);
            }
        } else {
            super.sweepAttack();
        }
    }

    // TODO: this is not needed
    @Override
    protected void triggerItemUseEffects(ItemStack pStack, int pCount) {
        if (!pStack.isEmpty() && this.isUsingItem()) {
            if (pStack.getUseAnimation() == UseAnim.DRINK) {
                this.playSound(this.getDrinkingSound(pStack), 0.5F, this.level().random.nextFloat() * 0.1F + 0.9F);
            }

            if (pStack.getUseAnimation() == UseAnim.EAT) {
                this.vivecraft$addItemParticles(pStack, pCount);
                this.playSound(this.getEatingSound(pStack), 0.5F + 0.5F * (float) this.random.nextInt(2),
                    (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            }
        }
    }

    // TODO: this should override "spawnItemParticles", or inject into LivingEntity
    @Unique
    private void vivecraft$addItemParticles(ItemStack stack, int count) {
        ServerVivePlayer serverviveplayer = vivecraft$getVivePlayer();
        for (int i = 0; i < count; ++i) {
            Vec3 vec3 = new Vec3(((double) this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D);
            vec3 = vec3.xRot(-this.getXRot() * ((float) Math.PI / 180F));
            vec3 = vec3.yRot(-this.getYRot() * ((float) Math.PI / 180F));
            double d0 = (double) (-this.random.nextFloat()) * 0.6D - 0.3D;
            Vec3 vec31 = new Vec3(((double) this.random.nextFloat() - 0.5D) * 0.3D, d0, 0.6D);
            vec31 = vec31.xRot(-this.getXRot() * ((float) Math.PI / 180F));
            vec31 = vec31.yRot(-this.getYRot() * ((float) Math.PI / 180F));
            vec31 = vec31.add(this.getX(), this.getEyeY(), this.getZ());
            if (serverviveplayer != null && serverviveplayer.isVR()) {
                InteractionHand interactionhand = this.getUsedItemHand();

                if (interactionhand == InteractionHand.MAIN_HAND) {
                    vec31 = serverviveplayer.getControllerPos(0, this);
                } else {
                    vec31 = serverviveplayer.getControllerPos(1, this);
                }
            }
            this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec31.x, vec31.y, vec31.z, vec3.x,
                vec3.y + 0.05D, vec3.z);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = Shift.BEFORE), method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void vivecraft$dropvive(ItemStack p_9085_, boolean dropAround, boolean includeName, CallbackInfoReturnable<ItemEntity> info,
        ItemEntity itementity) {
        ServerVivePlayer serverviveplayer = vivecraft$getVivePlayer();
        if (serverviveplayer != null && serverviveplayer.isVR() && !dropAround) {
            Vec3 vec3 = serverviveplayer.getControllerPos(0, this);
            Vec3 vec31 = serverviveplayer.getControllerDir(0);
            float f = 0.3F;
            itementity.setDeltaMovement(vec31.x * (double) f, vec31.y * (double) f, vec31.z * (double) f);
            itementity.setPos(vec3.x() + itementity.getDeltaMovement().x(),
                vec3.y() + itementity.getDeltaMovement().y(), vec3.z() + itementity.getDeltaMovement().z());
        }
    }

    @Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
    public void vivecraft$checkCanGetHurt(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = damageSource.getEntity();
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

    @Inject(at = @At("HEAD"), method = "die")
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
