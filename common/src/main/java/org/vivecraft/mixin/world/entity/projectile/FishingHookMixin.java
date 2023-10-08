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

import static org.joml.Math.*;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {

    protected FishingHookMixin(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        // TODO Auto-generated constructor stub
    }

    @Unique
    private ServerVivePlayer vivecraft$serverviveplayer;
    @Unique
    private Vec3 vivecraft$controllerDir;
    @Unique
    private Vec3 vivecraft$controllerPos;

    @ModifyVariable(at = @At("STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 0)
    private float vivecraft$modifyXrot(float xRot, Player player) {
        this.vivecraft$serverviveplayer = ServerVRPlayers.getVivePlayer((ServerPlayer) player);
        if (this.vivecraft$serverviveplayer != null && this.vivecraft$serverviveplayer.isVR()) {
            this.vivecraft$controllerDir = this.vivecraft$serverviveplayer.getControllerDir(this.vivecraft$serverviveplayer.activeHand);
            this.vivecraft$controllerPos = this.vivecraft$serverviveplayer.getControllerPos(this.vivecraft$serverviveplayer.activeHand, player);
            return -((float) toDegrees(asin(this.vivecraft$controllerDir.y / this.vivecraft$controllerDir.length())));
        }
        return xRot;
    }

    @ModifyVariable(at = @At("STORE"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", ordinal = 1)
    private float vivecraft$modifyYrot(float yRot) {
        if (this.vivecraft$serverviveplayer != null && this.vivecraft$serverviveplayer.isVR()) {
            return (float) toDegrees(atan2(-this.vivecraft$controllerDir.x, this.vivecraft$controllerDir.z));
        }
        return yRot;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;moveTo(DDDFF)V"), method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V")
    private void vivecraft$modifyMoveTo(FishingHook instance, double x, double y, double z, float yRot, float xRot) {
        if (this.vivecraft$serverviveplayer != null && this.vivecraft$serverviveplayer.isVR()) {
            instance.moveTo(this.vivecraft$controllerPos.x + this.vivecraft$controllerDir.x * (double) 0.6F, this.vivecraft$controllerPos.y + this.vivecraft$controllerDir.y * (double) 0.6F, this.vivecraft$controllerPos.z + this.vivecraft$controllerDir.z * (double) 0.6F, yRot, xRot);
            this.vivecraft$controllerDir = null;
            this.vivecraft$controllerPos = null;
        } else {
            this.moveTo(x, y, z, yRot, xRot);
        }

        this.vivecraft$serverviveplayer = null;
    }
}
