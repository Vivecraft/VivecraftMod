package org.vivecraft.titleworlds.mixin.cancel_save.needed;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import org.vivecraft.titleworlds.SnapshotCreateServer;
import org.vivecraft.titleworlds.TitleWorldsMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityStorage.class)
public class EntityStorageMixin {

    @Shadow
    @Final
    private ServerLevel level;

    /**
     * Prevent saving any entities
     */
    @Inject(method = "storeEntities", at = @At("HEAD"), cancellable = true)
    void cancelSave(CallbackInfo ci) {
        if (TitleWorldsMod.state.isTitleWorld && TitleWorldsMod.state.noSave
                || (this.level.getServer() instanceof SnapshotCreateServer snapshotCreateServer
                && snapshotCreateServer.cancelSaveEntity.get())) {
            ci.cancel();
        }
    }
}
