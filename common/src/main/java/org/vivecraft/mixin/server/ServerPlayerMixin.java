package org.vivecraft.mixin.server;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.Xplat;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.common.network.packet.s2c.DamageDirectionPayloadS2C;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.common.utils.Utils;
import org.vivecraft.mixin.world.entity.PlayerMixin;
import org.vivecraft.server.ServerNetworking;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;
import org.vivecraft.server.config.ServerConfig;

import java.util.IllegalFormatException;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin {

    @Shadow
    @Final
    public MinecraftServer server;

    @Shadow
    public ServerGamePacketListenerImpl connection;

    protected ServerPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "initInventoryMenu", at = @At("TAIL"))
    private void vivecraft$addItemEasterEgg(CallbackInfo ci) {
        // triggers on player respawn and rejoin
        ServerVivePlayer serverVivePlayer = vivecraft$getVivePlayer();
        if (ServerConfig.VR_FUN.get() && serverVivePlayer != null && serverVivePlayer.isVR() &&
            this.random.nextInt(40) == 3)
        {
            ItemStack easterEggItem;
            if (this.random.nextInt(2) == 1) {
                easterEggItem = new ItemStack(Items.PUMPKIN_PIE);
                easterEggItem.set(DataComponents.CUSTOM_NAME, Component.literal("EAT ME"));
            } else {
                easterEggItem = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                easterEggItem.set(DataComponents.CUSTOM_NAME, Component.literal("DRINK ME"));
            }

            if (this.getInventory().add(easterEggItem)) {
                this.inventoryMenu.broadcastChanges();
            }
        }
    }

    @Inject(method = "doTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;tick()V", shift = Shift.AFTER))
    private void vivecraft$overridePose(CallbackInfo ci) {
        ServerVRPlayers.overridePose((ServerPlayer) (Object) this);
    }

    /**
     * inject into {@link Player#sweepAttack}
     */
    @Override
    protected int vivecraft$modifySweepParticleSpawnPos(
        ServerLevel instance, ParticleOptions type, double posX, double posY, double posZ, int particleCount,
        double xOffset, double yOffset, double zOffset, double speed, Operation<Integer> original)
    {
        ServerVivePlayer serverVivePlayer = vivecraft$getVivePlayer();
        if (serverVivePlayer != null && serverVivePlayer.isVR()) {
            Vec3 aim = serverVivePlayer.getAimDir(false);
            float yaw = (float) Math.atan2(-aim.x, aim.z);

            xOffset = -Mth.sin(yaw);
            zOffset = Mth.cos(yaw);

            Vec3 pos = serverVivePlayer.getAimPos(false);

            return original.call(instance, type,
                pos.x + xOffset, pos.y, pos.z + zOffset,
                particleCount,
                xOffset, yOffset, zOffset, speed);
        } else {
            return original.call(instance, type, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);
        }
    }

    /**
     * inject into {@link Player#attack}
     */
    @Override
    protected float vivecraft$damageModifier(float damage) {
        // feet make more damage with boots
        if (ServerConfig.DUAL_WIELDING.get() && ServerConfig.BOOTS_ARMOR_DAMAGE.get() > 0) {
            ServerVivePlayer vivePlayer = vivecraft$getVivePlayer();
            if (vivePlayer.isVR() && vivePlayer.activeBodyPart.isFoot() &&
                !this.getItemBySlot(EquipmentSlot.FEET).isEmpty())
            {
                float addedDamage = 0F;
                for (ItemAttributeModifiers.Entry entry : this.getItemBySlot(EquipmentSlot.FEET)
                    .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
                    if (entry.attribute().is(Attributes.ARMOR)) {
                        float amount = (float) entry.modifier().amount();
                        switch (entry.modifier().operation()) {
                            case ADD_VALUE -> addedDamage += amount;
                            case ADD_MULTIPLIED_TOTAL -> addedDamage += amount * addedDamage;
                        }
                    }
                }
                return damage + addedDamage * ServerConfig.BOOTS_ARMOR_DAMAGE.get().floatValue();
            }
        }
        return damage;
    }

    @Unique
    private ItemStack vivecraft$roomscaleShieldItem;

    @Unique
    private InteractionHand vivecraft$roomscaleShieldHand;

    /**
     * inject into {@link LivingEntity#isDamageSourceBlocked}
     */
    @Override
    protected ItemStack vivecraft$roomscaleShieldBlockingItem(
        ItemStack original, DamageSource damageSource, LocalBooleanRef roomscaleBlocked)
    {
        // in case it wasn't reset the last time, since isDamageSourceBlocked is not just called from the serverHurt
        this.vivecraft$roomscaleShieldItem = null;
        this.vivecraft$roomscaleShieldHand = null;

        ServerVivePlayer serverVivePlayer = vivecraft$getVivePlayer();
        // only when VR and not already blocking, and if it is a directional damage
        if (ServerConfig.ALLOW_ROOMSCALE_SHIELD_BLOCKING.get() && (original == null || original.isEmpty()) &&
            damageSource.getSourcePosition() != null && serverVivePlayer != null && serverVivePlayer.isVR())
        {
            Vec3 dmgPos = damageSource.getSourcePosition();
            boolean isProjectile = false;
            // if the hit is from an entity, move it back in the movement direction, to get a better source direction
            if (damageSource.getDirectEntity() instanceof Entity entity && dmgPos == entity.position()) {
                isProjectile = entity instanceof Projectile;
                dmgPos = entity.getBoundingBox().getCenter().subtract(entity.getDeltaMovement().normalize());
            }
            // check if any hand is holding a shield
            for (int i = 0; i < 2; i++) {
                InteractionHand hand = InteractionHand.values()[i];
                ItemStack stack = this.getItemBySlot(LivingEntity.getSlotForHand(hand));
                // check for shield and do not bypass item cooldowns
                if (stack != null && stack.getItem().getUseAnimation(stack) == ItemUseAnimation.BLOCK &&
                    !this.getCooldowns().isOnCooldown(stack))
                {
                    // check if it blocks
                    Vector3fc sideDir;
                    if (serverVivePlayer.vrPlayerState().leftHanded()) {
                        sideDir = hand == InteractionHand.MAIN_HAND ? MathUtils.RIGHT : MathUtils.LEFT;
                    } else {
                        sideDir = hand == InteractionHand.MAIN_HAND ? MathUtils.LEFT : MathUtils.RIGHT;
                    }
                    Vec3 shieldDir = serverVivePlayer.getBodyPartVectorCustom(VRBodyPart.fromInteractionHand(hand),
                        sideDir);

                    // 0.5 = 120° blocking cone
                    boolean isBlocked;
                    if (isProjectile) {
                        // direction to hand
                        Vec3 dmgDir = dmgPos.subtract(
                            serverVivePlayer.getBodyPartPos(VRBodyPart.fromInteractionHand(hand))).normalize();
                        isBlocked = shieldDir.dot(dmgDir) > 0.5;
                    } else {
                        // horizontal direction to the player
                        Vec3 dmgDir = dmgPos.subtract(this.position()).horizontal().normalize();
                        isBlocked = shieldDir.horizontal().normalize().dot(dmgDir) > 0.5;
                    }
                    if (isBlocked) {
                        roomscaleBlocked.set(true);
                        this.vivecraft$roomscaleShieldItem = stack;
                        this.vivecraft$roomscaleShieldHand = hand;
                        return stack;
                    }
                }
            }
        }
        return original;
    }

    /**
     * inject into {@link Player#hurtCurrentlyUsedShield}
     */
    @Override
    protected void vivecraft$roomscaleShieldItemDamage(float damageAmount, Operation<Void> original) {
        ItemStack backup;
        if (ServerConfig.ALLOW_ROOMSCALE_SHIELD_BLOCKING.get() && this.vivecraft$roomscaleShieldItem != null) {
            backup = this.useItem;
            this.useItem = this.vivecraft$roomscaleShieldItem;
            original.call(damageAmount);
            this.useItem = backup;
            this.vivecraft$roomscaleShieldItem = null;
            this.vivecraft$roomscaleShieldHand = null;
        } else {
            original.call(damageAmount);
        }
    }

    /**
     * inject into {@link Player#hurtCurrentlyUsedShield}
     */
    @Override
    protected InteractionHand vivecraft$roomscaleShieldHand(InteractionHand original) {
        return ServerConfig.ALLOW_ROOMSCALE_SHIELD_BLOCKING.get() && this.vivecraft$roomscaleShieldHand != null ?
            this.vivecraft$roomscaleShieldHand : original;
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noAttackWhileBlocking(Entity target, CallbackInfo ci) {
        ServerVivePlayer vivePlayer = vivecraft$getVivePlayer();
        if (!ServerConfig.ALLOW_ATTACKS_WHILE_BLOCKING.get() && vivePlayer != null && vivePlayer.isVR() &&
            this.isBlocking())
        {
            ci.cancel();
        }
    }

    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void vivecraft$dropVive(
        ItemStack droppedItem, boolean dropAround, boolean includeThrowerName, CallbackInfoReturnable<ItemEntity> cir,
        @Local ItemEntity item)
    {
        ServerVivePlayer serverVivePlayer = vivecraft$getVivePlayer();
        if (!dropAround && serverVivePlayer != null && serverVivePlayer.isVR()) {
            // spawn item from players hand
            Vec3 pos = serverVivePlayer.getAimPos(false);
            Vec3 aim = serverVivePlayer.getAimDir(false);

            // item speed, taken from Player#drop
            final float speed = 0.3F;
            item.setDeltaMovement(aim.x * speed, aim.y * speed, aim.z * speed);
            item.setPos(pos.x + item.getDeltaMovement().x,
                pos.y + item.getDeltaMovement().y,
                pos.z + item.getDeltaMovement().z);
        }
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void vivecraft$checkCanGetHurt(
        ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir)
    {
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

            if ((!otherVive.isVR() && thisVive.isVR() && thisVive.isSeated()) ||
                (!thisVive.isVR() && otherVive.isVR() && otherVive.isSeated()))
            {
                // nonvr vs Seated
                if (!ServerConfig.PVP_SEATEDVR_VS_NONVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled nonvr vs seated VR damage";
                }
            } else if ((!otherVive.isVR() && thisVive.isVR() && !thisVive.isSeated()) ||
                (!thisVive.isVR() && otherVive.isVR() && !otherVive.isSeated()))
            {
                // nonvr vs Standing
                if (!ServerConfig.PVP_VR_VS_NONVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled nonvr vs standing VR damage";
                }
            } else if ((otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated()) ||
                (thisVive.isVR() && thisVive.isSeated() && otherVive.isVR() && !otherVive.isSeated()))
            {
                // Standing vs Seated
                if (!ServerConfig.PVP_VR_VS_SEATEDVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled seated VR vs standing VR damage";
                }
            } else if (otherVive.isVR() && !otherVive.isSeated() && thisVive.isVR() && !thisVive.isSeated()) {
                // Standing vs Standing
                if (!ServerConfig.PVP_VR_VS_VR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled standing VR vs standing VR damage";
                }
            } else if (otherVive.isVR() && otherVive.isSeated() && thisVive.isVR() && thisVive.isSeated()) {
                // Seated vs Seated
                if (!ServerConfig.PVP_SEATEDVR_VS_SEATEDVR.get()) {
                    blockedDamage = true;
                    blockedDamageCase = "canceled seated VR vs seated VR damage";
                }
            }
            if (blockedDamage) {
                if (ServerConfig.PVP_NOTIFY_BLOCKED_DAMAGE.get()) {
                    other.sendSystemMessage(Component.literal(blockedDamageCase));
                }
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void vivecraft$sendDamageDir(
        CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) DamageSource damageSource)
    {
        if (cir.getReturnValueZ()) {
            ServerVivePlayer vivePlayer = this.vivecraft$getVivePlayer();
            if (vivePlayer != null && vivePlayer.isVR() && vivePlayer.wantsDamageDirection) {
                this.connection.send(Xplat.getS2CPacket(
                    new DamageDirectionPayloadS2C(Utils.getDirFromDamageSource(damageSource, this))));
            }
        }
    }

    @Inject(method = "die", at = @At("HEAD"))
    private void vivecraft$customDeathMessage(DamageSource damageSource, CallbackInfo ci) {
        // only when enabled
        if (ServerConfig.MESSAGES_ENABLED.get()) {
            ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer((ServerPlayer) (Object) this);
            String message = "";
            String entity = "";

            // get the right message
            if (damageSource.getEntity() != null) {
                entity = damageSource.getEntity().getName().plainCopy().getString();
                // death by mob
                if (vivePlayer == null) {
                    message = ServerConfig.MESSAGES_DEATH_BY_MOB_VANILLA.get();
                } else if (!vivePlayer.isVR()) {
                    message = ServerConfig.MESSAGES_DEATH_BY_MOB_NONVR.get();
                } else if (vivePlayer.isSeated()) {
                    message = ServerConfig.MESSAGES_DEATH_BY_MOB_SEATED.get();
                } else {
                    message = ServerConfig.MESSAGES_DEATH_BY_MOB_VR.get();
                }
            }

            if (message.isEmpty()) {
                // general death, of if the mob one isn't set
                if (vivePlayer == null) {
                    message = ServerConfig.MESSAGES_DEATH_VANILLA.get();
                } else if (!vivePlayer.isVR()) {
                    message = ServerConfig.MESSAGES_DEATH_NONVR.get();
                } else if (vivePlayer.isSeated()) {
                    message = ServerConfig.MESSAGES_DEATH_SEATED.get();
                } else {
                    message = ServerConfig.MESSAGES_DEATH_VR.get();
                }
            }

            // actually send the message, if there is one set
            if (!message.isEmpty()) {
                try {
                    this.server.getPlayerList()
                        .broadcastSystemMessage(Component.literal(message.formatted(getName().getString(), entity)),
                            false);
                } catch (IllegalFormatException e) {
                    // catch errors users might put into the messages, to not crash other stuff
                    ServerNetworking.LOGGER.error("Vivecraft: Death message '{}' has errors:", message, e);
                }
            }
        }
    }

    @Unique
    private ServerVivePlayer vivecraft$getVivePlayer() {
        return ServerVRPlayers.getVivePlayer((ServerPlayer) (Object) this);
    }
}
