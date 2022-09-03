package org.vivecraft.mixin.world.entity.monster;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.api.CommonNetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

@Mixin(EnderMan.EndermanLookForPlayerGoal.class)
public abstract class EndermanLookForPlayerGoalMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;isLookingAtMe(Lnet/minecraft/world/entity/player/Player;)Z"), method = "method_18449")
    private static boolean predicate(EnderMan instance, Player player) {
        if (CommonNetworkHelper.isVive((ServerPlayer) player) && CommonNetworkHelper.vivePlayers.get(player) != null) {
            return shouldEndermanAttackVRPlayer(instance, (ServerPlayer) player);
        }else {
            return instance.isLookingAtMe(player);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/EnderMan;isLookingAtMe(Lnet/minecraft/world/entity/player/Player;)Z"), method = "tick")
    public boolean shouldAttack(EnderMan instance, Player player) {
        if (CommonNetworkHelper.isVive((ServerPlayer) player) && CommonNetworkHelper.vivePlayers.get(player) != null) {
            return shouldEndermanAttackVRPlayer(instance, (ServerPlayer) player);
        }else {
            return instance.isLookingAtMe(player);
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
