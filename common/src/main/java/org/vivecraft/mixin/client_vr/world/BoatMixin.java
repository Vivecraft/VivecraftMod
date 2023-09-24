package org.vivecraft.mixin.client_vr.world;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static org.vivecraft.client_vr.VRState.*;

import static org.joml.Math.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//TODO needed?
@Mixin(net.minecraft.world.entity.vehicle.Boat.class)
public abstract class BoatMixin extends net.minecraft.world.entity.Entity {

    @Shadow
    private float deltaRotation;
    @Shadow
    private boolean inputLeft;
    @Shadow
    private boolean inputRight;
    @Shadow
    private boolean inputUp;

    @Shadow
    public abstract void setPaddleState(boolean pLeft, boolean pRight);

    public BoatMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }



    @ModifyConstant(constant = @Constant(floatValue = 1.0F, ordinal = 0), method = "controlBoat()V")
    public float inputLeft(float f) {
        float f1 = mc.player.input.leftImpulse;
        return f1;
    }

    @ModifyConstant(constant = @Constant(floatValue = 1.0F, ordinal = 1), method = "controlBoat()V")
    public float inputRight(float f) {
        float f1 = mc.player.input.leftImpulse;
        return -f1;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", shift = Shift.BEFORE), method = "controlBoat", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void roomscaleRowing(CallbackInfo ci, float f) {
        if (!vrRunning) { return; }

        double mx, mz;

        if (this.inputUp && !dh.vrSettings.seated)
        {
            //controller-based
            float yaw = dh.vrPlayer.vrdata_world_pre.getController(1).getYaw();
            if(dh.vrSettings.vehicleRotation)
            {
                //tank controls
                float end = this.getYRot() % 360;
                float start = yaw;
                float difference = abs(end - start);

                if (difference > 180)
                {
                    if (end > start)
                    {
                        start += 360;
                    }
                    else
                    {
                        end += 360;
                    }
                }

                difference = end - start;

                if (abs(difference) < 30)
                {
                    f = 0.04F;
                }
                else if (abs(difference) > 150)
                {
                    f = -0.005F;
                }
                else if(difference < 0)
                {
                    this.deltaRotation += 1;
                    f = 0.005F;
                }
                else if(difference > 0)
                {
                    this.deltaRotation -= 1;
                    f = 0.005F;
                }
                else
                {
                    f = 0;
                }

                mx = sin(toRadians(-this.getYRot())) * f;
                mz = cos(toRadians(this.getYRot())) * f;
            }
            else
            {
                //point to move
                mx = sin(toRadians(-yaw)) * f;
                mz = cos(toRadians(yaw)) * f;
                this.setYRot(yaw);
            }
        }
        else
        {
            //roomscale or vanilla behavior
            if (dh.rowTracker.isRowing() && !dh.vrSettings.seated)
            {

                this.deltaRotation += dh.rowTracker.LOar / 1.5F;
                this.deltaRotation -= dh.rowTracker.ROar / 1.5F;
                    /*
                    this.deltaRotation += mc.rowTracker.forces[0] *50;
                    this.deltaRotation -= mc.rowTracker.forces[1] *50;
                     */

                if (this.deltaRotation < 0)
                {
                    this.inputLeft = true;
                }
                if (this.deltaRotation > 0)
                {
                    this.inputRight = true;
                }

                f = 0.06F * dh.rowTracker.FOar;
                if (f > 0)
                {
                    this.inputUp = true;
                }

                    /*
                    f=(float)(mc.rowTracker.forces[0] + mc.rowTracker.forces[1]);
                    if(f > 0.005) this.forwardInputDown = true;
                    */

            }
            mx= sin(toRadians(-this.getYRot())) * f;
            mz= cos(toRadians(this.getYRot())) * f;
        }
        this.setDeltaMovement(this.getDeltaMovement().x + mx, this.getDeltaMovement().y, this.getDeltaMovement().z + mz);

        this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
        ci.cancel();
    }

}
