package org.vivecraft.mixin.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.KeyboardInput;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;

import static org.vivecraft.client_vr.provider.openvr_lwjgl.control.VivecraftMovementInput.getMovementAxisValue;

@Mixin(KeyboardInput.class)
public class KeyboardInputVRMixin extends Input {

    @Final
    @Shadow
    private Options options;
    @Unique
    private boolean vivecraft$autoSprintActive = false;
    @Unique
    private boolean vivecraft$movementSetByAnalog = false;

    @Unique
    private float vivecraft$axisToDigitalMovement(float value) {
        if (value > 0.5F) {
            return 1.0F;
        } else {
            return value < -0.5F ? -1.0F : 0.0F;
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void vivecraft$tick(boolean isSneaking, float sneakSpeed, CallbackInfo ci) {
        if (!VRState.vrRunning) {
            return;
        }

        ci.cancel();

        this.leftImpulse = 0.0F;
        this.forwardImpulse = 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        boolean climbing = dataholder.climbTracker.isClimbeyClimb() && !minecraft.player.isInWater() && dataholder.climbTracker.isGrabbingLadder();

        if (climbing || !this.options.keyUp.isDown() && !VivecraftVRMod.INSTANCE.keyTeleportFallback.isDown()) {
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

        if (!climbing && !dataholder.vrSettings.seated && minecraft.screen == null && !KeyboardHandler.Showing) {
            VRInputAction vrinputaction = dataholder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveStrafe);
            VRInputAction vrinputaction1 = dataholder.vr.getInputAction(VivecraftVRMod.INSTANCE.keyFreeMoveRotate);
            Vector2f vector2f = vrinputaction.getAxis2DUseTracked();
            Vector2f vector21f = vrinputaction1.getAxis2DUseTracked();

            if (vector2f.x() == 0.0F && vector2f.y() == 0.0F) {
                if (vector21f.y() != 0.0F) {
                    flag1 = true;
                    f = vector21f.y();

                    if (dataholder.vrSettings.analogMovement) {
                        this.forwardImpulse = vector21f.y();
                        this.leftImpulse = 0.0F;
                        this.leftImpulse -= getMovementAxisValue(this.options.keyRight);
                        this.leftImpulse += getMovementAxisValue(this.options.keyLeft);
                    } else {
                        this.forwardImpulse = this.vivecraft$axisToDigitalMovement(vector21f.y());
                    }
                } else if (dataholder.vrSettings.analogMovement) {
                    flag1 = true;
                    this.forwardImpulse = 0.0F;
                    this.leftImpulse = 0.0F;
                    float f1 = getMovementAxisValue(this.options.keyUp);

                    if (f1 == 0.0F) {
                        f1 = getMovementAxisValue(VivecraftVRMod.INSTANCE.keyTeleportFallback);
                    }

                    f = f1;
                    this.forwardImpulse += f1;
                    this.forwardImpulse -= getMovementAxisValue(this.options.keyDown);
                    this.leftImpulse -= getMovementAxisValue(this.options.keyRight);
                    this.leftImpulse += getMovementAxisValue(this.options.keyLeft);
                    float f2 = 0.05F;
                    float f4 = 1.0F / (1.0F - f2);
                    float f12 = 0.0F;

                    if (Math.abs(this.forwardImpulse) > f2) {
                        f12 = (Math.abs(this.forwardImpulse) - f2) * f4 * Math.signum(this.forwardImpulse);
                    }

                    this.forwardImpulse = f12;
                    float f3 = 1.0F / (1.0F - f2);
                    float f11 = 0.0F;

                    if (Math.abs(this.leftImpulse) > f2) {
                        f11 = (Math.abs(this.leftImpulse) - f2) * f3 * Math.signum(this.leftImpulse);
                    }

                    this.leftImpulse = f11;
                }
            } else {
                flag1 = true;
                f = vector2f.y();

                if (dataholder.vrSettings.analogMovement) {
                    this.forwardImpulse = vector2f.y();
                    this.leftImpulse = -vector2f.x();
                } else {
                    this.forwardImpulse = this.vivecraft$axisToDigitalMovement(vector2f.y());
                    this.leftImpulse = this.vivecraft$axisToDigitalMovement(-vector2f.x());
                }
            }

            if (flag1) {
                this.vivecraft$movementSetByAnalog = true;
                this.up = this.forwardImpulse > 0.0F;
                this.down = this.forwardImpulse < 0.0F;
                this.left = this.leftImpulse > 0.0F;
                this.right = this.leftImpulse < 0.0F;
                VRInputAction.setKeyBindState(this.options.keyUp, this.up);
                VRInputAction.setKeyBindState(this.options.keyDown, this.down);
                VRInputAction.setKeyBindState(this.options.keyLeft, this.left);
                VRInputAction.setKeyBindState(this.options.keyRight, this.right);

                if (dataholder.vrSettings.autoSprint) {
                    if (f >= dataholder.vrSettings.autoSprintThreshold) {
                        minecraft.player.setSprinting(true);
                        this.vivecraft$autoSprintActive = true;
                        this.forwardImpulse = 1.0F;
                    } else if (this.forwardImpulse > 0.0F && dataholder.vrSettings.analogMovement) {
                        this.forwardImpulse = this.forwardImpulse / dataholder.vrSettings.autoSprintThreshold;
                    }
                }
            }
        }

        if (!flag1 && this.vivecraft$movementSetByAnalog) {
            VRInputAction.setKeyBindState(this.options.keyUp, false);
            VRInputAction.setKeyBindState(this.options.keyDown, false);
            VRInputAction.setKeyBindState(this.options.keyLeft, false);
            VRInputAction.setKeyBindState(this.options.keyRight, false);
        }

        this.vivecraft$movementSetByAnalog = flag1;

        if (this.vivecraft$autoSprintActive && f < dataholder.vrSettings.autoSprintThreshold) {
            minecraft.player.setSprinting(false);
            this.vivecraft$autoSprintActive = false;
        }

        boolean flag2 = minecraft.screen == null && (dataholder.vrPlayer.getFreeMove() || dataholder.vrSettings.simulateFalling) && !climbing;
        this.jumping = this.options.keyJump.isDown() && flag2;
        this.shiftKeyDown = (dataholder.sneakTracker.sneakCounter > 0 || dataholder.sneakTracker.sneakOverride || this.options.keyShift.isDown()) && minecraft.screen == null;

        if (isSneaking) {
            this.leftImpulse = (float) ((double) this.leftImpulse * sneakSpeed);
            this.forwardImpulse = (float) ((double) this.forwardImpulse * sneakSpeed);
        }
    }
}
