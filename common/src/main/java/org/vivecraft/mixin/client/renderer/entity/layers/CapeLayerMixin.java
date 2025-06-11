package org.vivecraft.mixin.client.renderer.entity.layers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.extensions.EntityRenderStateExtension;
import org.vivecraft.client.utils.ModelUtils;
import org.vivecraft.common.utils.MathUtils;

@Mixin(CapeLayer.class)
public abstract class CapeLayerMixin extends RenderLayer<PlayerRenderState, PlayerModel> {

    @Unique
    private final Vector3f vivecraft$tempV = new Vector3f();

    @Unique
    private final Matrix3f vivecraft$bodyRot = new Matrix3f();

    public CapeLayerMixin(RenderLayerParent<PlayerRenderState, PlayerModel> renderer) {
        super(renderer);
    }

    // DEBUG CAPE
    /*
    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/PlayerSkin;capeTexture()Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation vivecraft$whiteCape(PlayerSkin skin, Operation<ResourceLocation> original) {
        ResourceLocation capeTexture = original.call(skin);
        if (capeTexture == null) {
            capeTexture = RenderHelper.WHITE_TEXTURE;
        }
        return capeTexture;
    }
    */

    @ModifyExpressionValue(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/PlayerRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/CapeLayer;hasLayer(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;)Z", ordinal = 1))
    private boolean vivecraft$modifyTransform(
        boolean hasArmor, @Local(argsOnly = true) PlayerRenderState renderState,
        @Local(argsOnly = true) PoseStack poseStack)
    {
        ClientVRPlayers.RotInfo rotInfo = ((EntityRenderStateExtension) renderState).vivecraft$getRotInfo();
        // only do this if it's a vr player
        if (rotInfo != null) {
            // if it's a vr player do a custom offset
            this.vivecraft$bodyRot.rotationZYX(getParentModel().body.zRot, -getParentModel().body.yRot,
                -getParentModel().body.xRot);

            // attach the cape to the body
            this.vivecraft$bodyRot.transform(MathUtils.UP, this.vivecraft$tempV);
            float xRot = (float) Math.atan2(this.vivecraft$tempV.y, this.vivecraft$tempV.z) - Mth.HALF_PI;

            // make sure it doesn't go below -PI
            xRot = xRot < -Mth.PI ? xRot + Mth.TWO_PI : xRot;

            this.vivecraft$bodyRot.transform(MathUtils.LEFT, this.vivecraft$tempV);
            float yRot = (float) -Math.atan2(this.vivecraft$tempV.x, this.vivecraft$tempV.y) + Mth.HALF_PI;

            // transform offset to be body relative
            this.vivecraft$tempV.set(0F, 0F, 5.5F - 0.5F * (getParentModel().body.xRot / Mth.HALF_PI));
            if (hasArmor) {
                // vanilla cape offset with armor
                this.vivecraft$tempV.add(0F, -0.85F, 1.1F);
            }
            this.vivecraft$tempV.rotateX(xRot);
            this.vivecraft$tempV.rotateZ(yRot);

            // +24 because it should be the offset to the default position, which is at 24
            this.vivecraft$tempV.add(getParentModel().body.x, getParentModel().body.y + 24F, getParentModel().body.z);

            // no yaw, since we  need the vector to be player rotated anyway
            ModelUtils.modelToWorld(renderState, this.vivecraft$tempV, rotInfo, 0F, false, false, this.vivecraft$tempV);
            poseStack.translate(this.vivecraft$tempV.x, -this.vivecraft$tempV.y, -this.vivecraft$tempV.z);

            // rotate with body
            // max of 0 to keep it down when the body bends backwards
            float min = (renderState.isFallFlying ? 1F : renderState.swimAmount) * -Mth.HALF_PI;
            float flap = renderState.capeFlap + Mth.RAD_TO_DEG * Math.max(min, xRot);

            // limit the up rotation when walking forward, depending on body rotation
            float lean = xRot / Mth.HALF_PI;
            if (lean >= 0) {
                lean = renderState.capeLean * (1F - Mth.clamp(lean, 0F, 1F));
            } else {
                lean = 0F;
            }

            // manual rotation
            poseStack.mulPose(new Quaternionf()
                .rotateX((6.0f + lean / 2.0f + flap) * Mth.DEG_TO_RAD)
                .rotateZ(renderState.capeLean2 / 2.0f * Mth.DEG_TO_RAD)
                .rotateY((180.0f - renderState.capeLean2 / 2.0f) * Mth.DEG_TO_RAD + yRot));

            return false;
        } else {
            return hasArmor;
        }
    }
}
