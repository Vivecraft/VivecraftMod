package org.vivecraft.mixin.world.entity.projectile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {

    protected FishingHookMixin(EntityType<? extends Projectile> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        // TODO Auto-generated constructor stub
    }

    @Unique
    private ServerVivePlayer vivecraft$serverviveplayer = null;
    @Unique
    private Vec3 vivecraft$controllerDir = null;
    @Unique
    private Vec3 vivecraft$controllerPos = null;

    @ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 0)
    private float vivecraft$modifyXrot(float xRot, Player player) {
        // some mods like Aquaculture create a FishingHook on the client with a LocalPlayer
        // this is nonsense, so just ignore it
        if (player instanceof ServerPlayer serverPlayer) {
            vivecraft$serverviveplayer = ServerVRPlayers.getVivePlayer(serverPlayer);
            if (vivecraft$serverviveplayer != null && vivecraft$serverviveplayer.isVR()) {
                vivecraft$controllerDir = vivecraft$serverviveplayer.getControllerDir(vivecraft$serverviveplayer.activeHand);
                vivecraft$controllerPos = vivecraft$serverviveplayer.getControllerPos(vivecraft$serverviveplayer.activeHand, serverPlayer);
            }
        }

        if (vivecraft$serverviveplayer != null && vivecraft$serverviveplayer.isVR()) {
            return -((float) Math.toDegrees(Math.asin(vivecraft$controllerDir.y / vivecraft$controllerDir.length())));
        } else {
            return xRot;
        }
    }

    @ModifyVariable(at = @At(value = "STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 1)
    private float vivecraft$modifyYrot(float yRot) {
        if (vivecraft$serverviveplayer != null && vivecraft$serverviveplayer.isVR()) {
            return (float) Math.toDegrees(Math.atan2(-vivecraft$controllerDir.x, vivecraft$controllerDir.z));
        } else {
            return yRot;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;moveTo(DDDFF)V"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
    private void vivecraft$modifyMoveTo(FishingHook instance, double x, double y, double z, float yRot, float xRot) {
        if (vivecraft$serverviveplayer != null && vivecraft$serverviveplayer.isVR()) {
            instance.moveTo(vivecraft$controllerPos.x + vivecraft$controllerDir.x * (double) 0.6F, vivecraft$controllerPos.y + vivecraft$controllerDir.y * (double) 0.6F, vivecraft$controllerPos.z + vivecraft$controllerDir.z * (double) 0.6F, yRot, xRot);
            vivecraft$controllerDir = null;
            vivecraft$controllerPos = null;
        } else {
            this.moveTo(x, y, z, yRot, xRot);
        }

        vivecraft$serverviveplayer = null;
    }
}
