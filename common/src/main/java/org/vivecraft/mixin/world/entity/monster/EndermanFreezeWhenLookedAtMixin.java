package org.vivecraft.mixin.world.entity.monster;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

@Mixin(EnderMan.EndermanFreezeWhenLookedAt.class)
public class EndermanFreezeWhenLookedAtMixin {

    @Shadow
    @Nullable
    private LivingEntity target;
    @Final
    @Shadow
    private EnderMan enderman;

    @Inject(at = @At("HEAD"), method = "canUse", cancellable = true)
    public void vrTarget(CallbackInfoReturnable<Boolean> cir) {
        if (this.target instanceof ServerPlayer player && CommonNetworkHelper.isVive(player) && CommonNetworkHelper.vivePlayers.get(player) != null) {
            double dist = target.distanceToSqr(this.enderman);
            cir.setReturnValue(dist <= 256.0D && shouldEndermanAttackVRPlayer(this.enderman, player));
        }
    }

    @Inject(at = @At("HEAD"), method = "tick")
    public void vrTick(CallbackInfo ci) {
        if (this.target instanceof ServerPlayer player && CommonNetworkHelper.isVive(player) && CommonNetworkHelper.vivePlayers.get(player) != null) {
            ServerVivePlayer data = CommonNetworkHelper.vivePlayers.get(player);
            this.enderman.getLookControl().setLookAt(data.getHMDPos(player));
        }
    }

    private static boolean shouldEndermanAttackVRPlayer(EnderMan enderman, ServerPlayer player) {
        ItemStack itemstack = player.getInventory().armor.get(3);
        if (!itemstack.is(Items.CARVED_PUMPKIN)) { //no enderitem
            ServerVivePlayer data = CommonNetworkHelper.vivePlayers.get(player);
            Vec3 vector3d = data.getHMDDir();
            Vec3 vector3d1 = new Vec3(enderman.getX() - data.getHMDPos(player).x, enderman.getEyeY() - data.getHMDPos(player).y, enderman.getZ() - data.getHMDPos(player).z);
            double d0 = vector3d1.length();
            vector3d1 = vector3d1.normalize();
            double d1 = vector3d.dot(vector3d1);
            return d1 > 1.0D - 0.025D / d0 && canEntityBeSeen(enderman, data.getHMDPos(player));
        }

        return false;
    }

    private static boolean canEntityBeSeen(Entity entity, Vec3 playerEyePos) {
        Vec3 entityEyePos = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        return entity.level.clip(new ClipContext(playerEyePos, entityEyePos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    }

}
