package com.example.examplemod.mixin.world.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.api.ServerVivePlayer;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Entity {
	
	protected FishingHookMixin(EntityType<? extends Projectile> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(at = @At(value = "RETURN", target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"), 
			method = "<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;II)V", 
			locals = LocalCapture.CAPTURE_FAILHARD)
	public void hook(Player p_37106_, Level p_37107_, int p_37108_, int p_37109_ , CallbackInfo info, float f, float f1) {
		ServerVivePlayer serverviveplayer = NetworkHelper.vivePlayers.get(p_37106_.getUUID());
		if (serverviveplayer != null && serverviveplayer.isVR()) {
			Vec3 vec32 = serverviveplayer.getControllerDir(serverviveplayer.activeHand);
			Vec3 vec3 = serverviveplayer.getControllerPos(serverviveplayer.activeHand, p_37106_);
			f = -((float) Math.toDegrees(Math.asin(vec32.y / vec32.length())));
			f1 = (float) Math.toDegrees(Math.atan2(-vec32.x, vec32.z));
			this.moveTo(vec3.x + vec32.x * (double)0.6F, vec3.y + vec32.y * (double)0.6F, vec3.z + vec32.z * (double)0.6F, f1, f);
		}
		
	}

}
