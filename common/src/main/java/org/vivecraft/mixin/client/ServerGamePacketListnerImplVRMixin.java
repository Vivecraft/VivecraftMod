package org.vivecraft.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListnerImplVRMixin {

    @Shadow
    public ServerPlayer player;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;absMoveTo(DDDFF)V"), method = "handleMoveVehicle")
    public void playerPos(Entity instance, double d, double e, double f, float g, float h) {
        instance.absMoveTo(d, e, f, g, h);
        this.player.absMoveTo(d, e, f, this.player.getYRot(), this.player.getXRot());
    }

    @Inject(at = @At("RETURN"), method = "noBlocksAround", cancellable = true)
    public void noBlocks(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(BlockPos.betweenClosedStream(entity.getBoundingBox().inflate(0.0625D).expandTowards(0.0D, -0.55D, 0.0D)).allMatch(b -> entity.level.getBlockState(b).isAir()));
    }
}
