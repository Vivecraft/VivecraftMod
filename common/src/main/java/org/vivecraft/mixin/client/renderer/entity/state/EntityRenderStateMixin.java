package org.vivecraft.mixin.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateExtension {

    @Unique
    private ClientVRPlayers.RotInfo vivecraft$rotInfo;

    @Unique
    private boolean vivecraft$isFirstPersonPlayer;

    @Unique
    private float vivecraft$totalScale;

    @Override
    public ClientVRPlayers.RotInfo vivecraft$getRotInfo() {
        return this.vivecraft$rotInfo;
    }

    @Override
    public void vivecraft$setRotInfo(ClientVRPlayers.RotInfo rotInfo) {
        this.vivecraft$rotInfo = rotInfo;
    }

    @Override
    public boolean vivecraft$isFirstPersonPlayer() {
        return this.vivecraft$isFirstPersonPlayer;
    }

    @Override
    public void vivecraft$setFirstPersonPlayer(boolean firstPersonPlayer) {
        this.vivecraft$isFirstPersonPlayer = firstPersonPlayer;
    }

    @Override
    public float vivecraft$getTotalScale() {
        return this.vivecraft$totalScale;
    }

    @Override
    public void vivecraft$setTotalScale(float totalScale) {
        this.vivecraft$totalScale = totalScale;
    }
}
