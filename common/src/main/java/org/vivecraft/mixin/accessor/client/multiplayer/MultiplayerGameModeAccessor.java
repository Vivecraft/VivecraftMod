package org.vivecraft.mixin.accessor.client.multiplayer;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MultiplayerGameModeAccessor {
    // to disable destroy delay, on roomscale hitting
    @Accessor
    void setDestroyDelay(int destroyDelay);
}
