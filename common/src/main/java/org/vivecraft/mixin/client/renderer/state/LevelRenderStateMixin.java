package org.vivecraft.mixin.client.renderer.state;

import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.LevelRenderStateExtension;
import org.vivecraft.client_vr.render.renderstates.VRRenderState;

@Mixin(LevelRenderState.class)
public class LevelRenderStateMixin implements LevelRenderStateExtension {

    @Unique
    private final BlockOutlineRenderState[] vivecraft$interactOutlineStates = new BlockOutlineRenderState[2];

    @Unique
    private final VRRenderState vivecraft$vrRenderState = new VRRenderState();

    @Override
    @Unique
    public void vivecraft$setInteractOutlineState(int controller, BlockOutlineRenderState interactOutline) {
        this.vivecraft$interactOutlineStates[controller] = interactOutline;
    }

    @Override
    @Unique
    public BlockOutlineRenderState[] vivecraft$getInteractOutlineStates() {
        return this.vivecraft$interactOutlineStates;
    }

    @Override
    @Unique
    public VRRenderState vivecraft$getVRRenderState() {
        return this.vivecraft$vrRenderState;
    }

    @Inject(method = "reset", at = @At("HEAD"))
    private void vivecraft$reset(CallbackInfo ci) {
        this.vivecraft$interactOutlineStates[0] = null;
        this.vivecraft$interactOutlineStates[1] = null;
    }
}
