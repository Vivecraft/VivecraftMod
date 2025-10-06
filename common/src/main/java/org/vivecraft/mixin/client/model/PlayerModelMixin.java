package org.vivecraft.mixin.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.render.VRPlayerModel;

@Mixin(PlayerModel.class)
public class PlayerModelMixin extends HumanoidModel<PlayerRenderState> {

    @Unique
    private final Vector3f vivecraft$tempV = new Vector3f();
    @Unique
    private final Vector3f vivecraft$tempV2 = new Vector3f();
    @Unique
    private final Matrix3f vivecraft$tempM = new Matrix3f();

    public PlayerModelMixin(ModelPart root) {
        super(root);
    }


    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("TAIL"))
    private void vivecraft$VRAnim(PlayerRenderState renderState, CallbackInfo ci) {
        this.head.visible = true;
        if (((EntityRenderStateExtension) renderState).vivecraft$getRotInfo() != null) {
            VRPlayerModel.animateVRModel((PlayerModel) (Object) this, renderState,
                this.vivecraft$tempV, this.vivecraft$tempV2, this.vivecraft$tempM);
        }
    }
}
