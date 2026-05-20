package org.vivecraft.mod_compat_vr.mca.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.extensions.EntityRenderStateExtension;

/**
 * MCA uses a fixed offset for the breasts, this changes it to be relative to the body rotation
 */
@Pseudo
@Mixin(targets = {
    "net.conczin.mca.client.model.PlayerEntityExtendedModel",
    "fabric.net.mca.client.model.PlayerEntityExtendedModel",
    "forge.net.mca.client.model.PlayerEntityExtendedModel",
    "quilt.net.mca.client.model.PlayerEntityExtendedModel"})
public abstract class PlayerEntityExtendedModelMixin {

    @Final
    @Shadow
    public ModelPart breasts;

    @Final
    @Shadow
    public ModelPart breastsWear;

    @Shadow
    float breastSize;

    @Unique
    private final Vector3f vivecraft$position = new Vector3f();
    @Unique
    private final Vector3f vivecraft$rotation = new Vector3f();
    @Unique
    private final Matrix3f vivecraft$rotMatrix = new Matrix3f();

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void vivecraft$moveBreasts(CallbackInfo ci, @Local(argsOnly = true) AvatarRenderState villager) {
        if (((EntityRenderStateExtension) villager).vivecraft$getRotInfo() != null) {

            ModelPart body = ((PlayerModel) (Object) this).body;
            this.vivecraft$rotMatrix.rotationZYX(body.zRot, body.yRot, body.xRot);

            this.vivecraft$rotMatrix.transform(
                0.25F,
                (float) (5.0F - Math.pow(this.breastSize, 0.5) * 2.5F),
                -1.5F + this.breastSize * 0.25F,
                this.vivecraft$position);

            this.vivecraft$rotMatrix.rotateX(Mth.PI * 0.3F);
            this.vivecraft$rotMatrix.getEulerAnglesZYX(this.vivecraft$rotation);

            this.breasts.setRotation(this.vivecraft$rotation.x, this.vivecraft$rotation.y, this.vivecraft$rotation.z);
            // MCA is using a weird scale on the breasts
            // see CommonVillagerModel#renderCommon
            this.breasts.setPos(
                this.vivecraft$position.x + body.x / (this.breastSize * 0.2f + 1.05f),
                this.vivecraft$position.y + body.y / (this.breastSize * 0.75f + 0.75f),
                this.vivecraft$position.z + body.z / (this.breastSize * 0.75f + 0.75f));
            this.breastsWear.setPos(this.breasts.x, this.breasts.y, this.breasts.z);
            this.breastsWear.setRotation(this.breasts.xRot, this.breasts.yRot, this.breasts.zRot);
        }
    }
}
