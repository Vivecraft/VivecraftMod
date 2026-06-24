package org.vivecraft.mixin.world.level.savedata.maps;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.server.ServerVRPlayers;

@Mixin(MapItemSavedData.class)
public class MapItemSavedDataMixin {
    @WrapOperation(method = "tickCarriedBy", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getYRot()F"))
    private float vivecraft$useHeadRot(Player player, Operation<Float> original) {
        if (player instanceof ServerPlayer serverPlayer && ServerVRPlayers.isVRPlayer(serverPlayer)) {
            return ServerVRPlayers.getVivePlayer(serverPlayer).getBodyYawRad() * Mth.RAD_TO_DEG;
        } else {
            return original.call(player);
        }
    }
}
