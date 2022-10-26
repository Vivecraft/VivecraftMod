package org.vivecraft.mixin.server.level;

import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.MinecraftServer;
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
import org.vivecraft.ClientDataHolder;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

	@Shadow @Final public MinecraftServer server;
	@Unique
	private String language = "en_us";
	@Unique
	private boolean hasTabListName = false;
	@Unique
	private Component tabListDisplayName = null;

	public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable ProfilePublicKey profilePublicKey) {
		super(level, blockPos, f, gameProfile, profilePublicKey);
	}

	@Inject(at = @At("TAIL"), method = "initMenu(Lnet/minecraft/world/inventory/AbstractContainerMenu;)V")
	public void menu(AbstractContainerMenu p_143400_, CallbackInfo info) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(this.getUUID());
		// TODO change setting to commonDataHolder?
		if ((!this.server.isDedicatedServer() && ClientDataHolder.getInstance().vrSettings != null && !ClientDataHolder.getInstance().vrSettings.disableFun) && serverviveplayer != null && serverviveplayer.isVR() && this.random.nextInt(40) == 3) {
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
	public void tick(CallbackInfo info) {
		CommonNetworkHelper.overridePose((ServerPlayer) (Object)this);
	}

	public void sweepAttack() {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(this.getUUID());

		if (serverviveplayer != null && serverviveplayer.isVR()) {
			Vec3 vec3 = serverviveplayer.getControllerDir(0);
			float f = (float) Math.toDegrees(Math.atan2(vec3.x, -vec3.z));
			double d0 = (double) (-Mth.sin(f * ((float) Math.PI / 180F)));
			double d1 = (double) Mth.cos(f * ((float) Math.PI / 180F));
			Vec3 vec31 = serverviveplayer.getControllerPos(0, this);

			if (this.level instanceof ServerLevel) {
				((ServerLevel) this.level).sendParticles(ParticleTypes.SWEEP_ATTACK, vec31.x + d0, vec31.y,
						vec31.z + d1, 0, d0, 0.0D, d1, 0.0D);
			}
		} else {
			super.sweepAttack();
		}
	}

	protected void triggerItemUseEffects(ItemStack pStack, int pCount) {
		if (!pStack.isEmpty() && this.isUsingItem()) {
			if (pStack.getUseAnimation() == UseAnim.DRINK) {
				this.playSound(this.getDrinkingSound(pStack), 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
			}

			if (pStack.getUseAnimation() == UseAnim.EAT) {
				this.addItemParticles(pStack, pCount);
				this.playSound(this.getEatingSound(pStack), 0.5F + 0.5F * (float) this.random.nextInt(2),
						(this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
			}
		}
	}

	private void addItemParticles(ItemStack stack, int count) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(this.getUUID());
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
			this.level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack), vec31.x, vec31.y, vec31.z, vec3.x,
					vec3.y + 0.05D, vec3.z);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = Shift.BEFORE), method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
			locals = LocalCapture.CAPTURE_FAILHARD)
	public void dropvive(ItemStack p_9085_, boolean p_9086_, boolean p_9087_, CallbackInfoReturnable<ItemEntity> info,
			ItemEntity itementity) {
		ServerVivePlayer serverviveplayer = CommonNetworkHelper.vivePlayers.get(this.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR() && !p_9087_) {
			Vec3 vec3 = serverviveplayer.getControllerPos(0, this);
			Vec3 vec31 = serverviveplayer.getControllerDir(0);
			float f = 0.3F;
			itementity.setDeltaMovement(vec31.x * (double) f, vec31.y * (double) f, vec31.z * (double) f);
			itementity.setPos(vec3.x() + itementity.getDeltaMovement().x(),
					vec3.y() + itementity.getDeltaMovement().y(), vec3.z() + itementity.getDeltaMovement().z());
		}
	}

	public String getLanguage() {
		return this.language;
	}

}
