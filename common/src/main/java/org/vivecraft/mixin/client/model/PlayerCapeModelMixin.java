package org.vivecraft.mixin.client.model;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;

@Mixin(PlayerCapeModel.class)
public class PlayerCapeModelMixin<T extends PlayerRenderState> extends HumanoidModel<T> {
    @Shadow
    @Final
    private ModelPart cape;

    public PlayerCapeModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("TAIL"))
    private void vivecraft$resetStateWhenVR(CallbackInfo ci, @Local(argsOnly = true) PlayerRenderState renderState) {
        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) renderState).vivecraft$getRotInfo();
        if (rotInfo != null) {
            this.cape.resetPose();
            this.body.resetPose();
        }
    }
}
