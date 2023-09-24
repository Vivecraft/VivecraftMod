package org.vivecraft.mixin.client_vr;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

import org.joml.Vector2f;

import static org.vivecraft.client_vr.VRState.*;
import static org.vivecraft.client_vr.provider.openvr_lwjgl.control.VivecraftMovementInput.getMovementAxisValue;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.player.KeyboardInput.class)
public class KeyboardInputVRMixin extends net.minecraft.client.player.Input {

    @Final
    @Shadow
    private net.minecraft.client.Options options;
    @Unique
    private boolean autoSprintActive = false;
    @Unique
    private boolean movementSetByAnalog = false;

    @Unique
    private float axisToDigitalMovement(float value) {
        if (value > 0.5F) {
            return 1.0F;
        } else {
            return value < -0.5F ? -1.0F : 0.0F;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(boolean isSneaking, float sneakSpeed, CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }

        ci.cancel();

        this.leftImpulse = 0.0F;
        this.forwardImpulse = 0.0F;
        boolean climbing = dh.climbTracker.isClimbeyClimb() && !mc.player.isInWater() && dh.climbTracker.isGrabbingLadder();

        if (climbing || !this.options.keyUp.isDown() && !VivecraftVRMod.keyTeleportFallback.isDown()) {
            this.up = false;
        } else {
            ++this.forwardImpulse;
            this.up = true;
        }

        if (!climbing && this.options.keyDown.isDown()) {
            --this.forwardImpulse;
            this.down = true;
        } else {
            this.down = false;
        }

        if (!climbing && this.options.keyLeft.isDown()) {
            ++this.leftImpulse;
            this.left = true;
        } else {
            this.left = false;
        }

        if (!climbing && this.options.keyRight.isDown()) {
            --this.leftImpulse;
            this.right = true;
        } else {
            this.right = false;
        }

        boolean flag1 = false;
        float f = 0.0F;

        if (!climbing && !dh.vrSettings.seated && mc.screen == null && !KeyboardHandler.isShowing()) {
            VRInputAction vrinputaction = dh.vr.getInputAction(VivecraftVRMod.keyFreeMoveStrafe);
            VRInputAction vrinputaction1 = dh.vr.getInputAction(VivecraftVRMod.keyFreeMoveRotate);
            Vector2f vector2 = vrinputaction.getAxis2DUseTracked();
            Vector2f vector21 = vrinputaction1.getAxis2DUseTracked();

            if (vector2.x() == 0.0F && vector2.y() == 0.0F) {
                if (vector21.y() != 0.0F) {
                    flag1 = true;
                    f = vector21.y();

                    if (dh.vrSettings.analogMovement) {
                        this.forwardImpulse = vector21.y();
                        this.leftImpulse = 0.0F;
                        this.leftImpulse -= getMovementAxisValue(this.options.keyRight);
                        this.leftImpulse += getMovementAxisValue(this.options.keyLeft);
                    } else {
                        this.forwardImpulse = this.axisToDigitalMovement(vector21.y());
                    }
                } else if (dh.vrSettings.analogMovement) {
                    flag1 = true;
                    this.forwardImpulse = 0.0F;
                    this.leftImpulse = 0.0F;
                    float f1 = getMovementAxisValue(this.options.keyUp);

                    if (f1 == 0.0F) {
                        f1 = getMovementAxisValue(VivecraftVRMod.keyTeleportFallback);
                    }

                    f = f1;
                    this.forwardImpulse += f1;
                    this.forwardImpulse -= getMovementAxisValue(this.options.keyDown);
                    this.leftImpulse -= getMovementAxisValue(this.options.keyRight);
                    this.leftImpulse += getMovementAxisValue(this.options.keyLeft);
                    float f2 = 0.05F;
                    this.forwardImpulse = Utils.applyDeadzone(this.forwardImpulse, f2);
                    this.leftImpulse = Utils.applyDeadzone(this.leftImpulse, f2);
                }
            } else {
                flag1 = true;
                f = vector2.y();

                if (dh.vrSettings.analogMovement) {
                    this.forwardImpulse = vector2.y();
                    this.leftImpulse = -vector2.x();
                } else {
                    this.forwardImpulse = this.axisToDigitalMovement(vector2.y());
                    this.leftImpulse = this.axisToDigitalMovement(-vector2.x());
                }
            }

            if (flag1) {
                this.movementSetByAnalog = true;
                this.up = this.forwardImpulse > 0.0F;
                this.down = this.forwardImpulse < 0.0F;
                this.left = this.leftImpulse > 0.0F;
                this.right = this.leftImpulse < 0.0F;
                VRInputAction.setKeyBindState(this.options.keyUp, this.up);
                VRInputAction.setKeyBindState(this.options.keyDown, this.down);
                VRInputAction.setKeyBindState(this.options.keyLeft, this.left);
                VRInputAction.setKeyBindState(this.options.keyRight, this.right);

                if (dh.vrSettings.autoSprint) {
                    if (f >= dh.vrSettings.autoSprintThreshold) {
                        mc.player.setSprinting(true);
                        this.autoSprintActive = true;
                        this.forwardImpulse = 1.0F;
                    } else if (this.forwardImpulse > 0.0F && dh.vrSettings.analogMovement) {
                        this.forwardImpulse = this.forwardImpulse / dh.vrSettings.autoSprintThreshold * 1.0F;
                    }
                }
            }
        }

        if (!flag1 && this.movementSetByAnalog) {
            VRInputAction.setKeyBindState(this.options.keyUp, false);
            VRInputAction.setKeyBindState(this.options.keyDown, false);
            VRInputAction.setKeyBindState(this.options.keyLeft, false);
            VRInputAction.setKeyBindState(this.options.keyRight, false);
        }

        this.movementSetByAnalog = flag1;

        if (this.autoSprintActive && f < dh.vrSettings.autoSprintThreshold) {
            mc.player.setSprinting(false);
            this.autoSprintActive = false;
        }

        boolean flag2 = mc.screen == null && (dh.vrPlayer.getFreeMove() || dh.vrSettings.simulateFalling) && !climbing;
        this.jumping = this.options.keyJump.isDown() && flag2;
        this.shiftKeyDown = (dh.sneakTracker.sneakCounter > 0 || dh.sneakTracker.sneakOverride || this.options.keyShift.isDown()) && mc.screen == null;

        if (isSneaking) {
            this.leftImpulse = (float) ((double) this.leftImpulse * sneakSpeed);
            this.forwardImpulse = (float) ((double) this.forwardImpulse * sneakSpeed);
        }
    }
}
