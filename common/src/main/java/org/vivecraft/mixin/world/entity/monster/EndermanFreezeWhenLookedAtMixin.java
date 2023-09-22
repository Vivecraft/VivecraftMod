package org.vivecraft.mixin.world.entity.monster;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.server.ServerVRPlayers;
import org.vivecraft.server.ServerVivePlayer;

@Mixin(targets = "net.minecraft.world.entity.monster.EnderMan$EndermanFreezeWhenLookedAt")
public class EndermanFreezeWhenLookedAtMixin {

    @Shadow
    @Nullable
    private LivingEntity target;
    @Final
    @Shadow
    private EnderMan enderman;

    @Inject(at = @At("HEAD"), method = "tick", cancellable = true)
    public void vivecraft$vrTick(CallbackInfo ci) {
        if (this.target instanceof ServerPlayer player && ServerVRPlayers.isVRPlayer(player)) {
            ServerVivePlayer data = ServerVRPlayers.getVivePlayer(player);
            this.enderman.getLookControl().setLookAt(data.getHMDPos(player));
            ci.cancel();
        }
    }
}
