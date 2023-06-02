package org.vivecraft.mixin.client_vr.world;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

//TODO needed?
@Mixin(Boat.class)
public abstract class BoatMixin extends Entity {

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



	@ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 0), method = "controlBoat()V")
	public float inputLeft(float f) {
		 Minecraft minecraft = Minecraft.getInstance();
		 float f1 = minecraft.player.input.leftImpulse;
		 return f1;
	}

	@ModifyConstant(constant = @Constant(floatValue = 1F, ordinal = 1), method = "controlBoat()V")
	public float inputRight(float f) {
		 Minecraft minecraft = Minecraft.getInstance();
		 float f1 = minecraft.player.input.leftImpulse;
		 return -f1;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V", shift = At.Shift.BEFORE), method = "controlBoat", locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	public void roomscaleRowing(CallbackInfo ci, float f) {
		if (!VRState.vrRunning) {
			return;
		}

		double mx, mz;
		ClientDataHolderVR clientDataHolderVR = ClientDataHolderVR.getInstance();

		if(this.inputUp && !clientDataHolderVR.vrSettings.seated){
			//controller-based
			float yaw = clientDataHolderVR.vrPlayer.vrdata_world_pre.getController(1).getYaw();
			if(clientDataHolderVR.vrSettings.vehicleRotation){
				//tank controls
				float end = this.getYRot() % 360;
				float start = yaw;
				float difference = Math.abs(end - start);

				if (difference > 180)
					if (end > start)
						start += 360;
					else
						end += 360;

				difference = end - start;

				f = 0;

				if (Math.abs(difference) < 30){
					f = 0.04f;
				}
				else if (Math.abs(difference) > 150) {
					f = -0.005F;
				}
				else if(difference < 0){
					this.deltaRotation +=1;
					f = 0.005f;
				} else if(difference > 0) {
					this.deltaRotation -=1;
					f = 0.005f;
				}

				mx = Math.sin(-this.getYRot()* 0.017453292F) * f;
				mz = Math.cos(this.getYRot() * 0.017453292F) * f;
			} else {
				//point to move
				mx = Math.sin(-yaw* 0.017453292F) * f;
				mz = Math.cos(yaw * 0.017453292F) * f;
				this.setYRot(yaw);
			}


		} else {
			//roomscale or vanilla behavior
			if(clientDataHolderVR.rowTracker.isRowing() && !clientDataHolderVR.vrSettings.seated){

				this.deltaRotation += clientDataHolderVR.rowTracker.LOar / 1.5;
				this.deltaRotation -= clientDataHolderVR.rowTracker.ROar / 1.5;
    				/*
    				this.deltaRotation += mc.rowTracker.forces[0] *50;
    				this.deltaRotation -= mc.rowTracker.forces[1] *50;
    				 */

				if (deltaRotation < 0) this.inputLeft = true;
				if (deltaRotation > 0) this.inputRight = true;

				f = 0.06f * clientDataHolderVR.rowTracker.Foar;
				if(f > 0) this.inputUp = true;

    				/*
    				f=(float)(mc.rowTracker.forces[0] + mc.rowTracker.forces[1]);
    				if(f > 0.005) this.forwardInputDown = true;
    				*/

				mx= Math.sin(-this.getYRot() * 0.017453292F) * f;
				mz= Math.cos(this.getYRot() * 0.017453292F) * f;
			}else{
				//default boat (seated mode)
				mx= Math.sin(-this.getYRot() * 0.017453292F) * f;
				mz= Math.cos(this.getYRot() * 0.017453292F) * f;
			}
		}
		this.setDeltaMovement(this.getDeltaMovement().x + mx, this.getDeltaMovement().y, this.getDeltaMovement().z + mz);

		this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
		ci.cancel();
	}

}
