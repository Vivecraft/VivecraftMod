package org.vivecraft.provider.openvr_jna.control;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.Input;
import org.vivecraft.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.provider.MCVR;
import org.vivecraft.provider.openvr_jna.VRInputAction;
import org.vivecraft.utils.Utils;
import org.vivecraft.utils.math.Vector2;

public class VivecraftMovementInput extends Input
{
    private final Options gameSettings;
    private boolean autoSprintActive = false;
    private boolean movementSetByAnalog = false;

    public VivecraftMovementInput(Options gameSettings)
    {
        this.gameSettings = gameSettings;
    }

    public static float getMovementAxisValue(KeyMapping keyBinding)
    {
        VRInputAction vrinputaction = MCVR.get().getInputAction(keyBinding);
        return Math.abs(vrinputaction.getAxis1DUseTracked());
    }

    private float axisToDigitalMovement(float value)
    {
        if (value > 0.5F)
        {
            return 1.0F;
        }
        else
        {
            return value < -0.5F ? -1.0F : 0.0F;
        }
    }

    public void tick(boolean p_108576_)
    {
        this.leftImpulse = 0.0F;
        this.forwardImpulse = 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        boolean flag = false;

        if (minecraft.climbTracker.isClimbeyClimb() && !minecraft.player.isInWater() && minecraft.climbTracker.isGrabbingLadder())
        {
            flag = true;
        }

        if (flag || !this.gameSettings.keyUp.isDown() && !minecraft.vr.keyTeleportFallback.isDown())
        {
            this.up = false;
        }
        else
        {
            ++this.forwardImpulse;
            this.up = true;
        }

        if (!flag && this.gameSettings.keyDown.isDown())
        {
            --this.forwardImpulse;
            this.down = true;
        }
        else
        {
            this.down = false;
        }

        if (!flag && this.gameSettings.keyLeft.isDown())
        {
            ++this.leftImpulse;
            this.left = true;
        }
        else
        {
            this.left = false;
        }

        if (!flag && this.gameSettings.keyRight.isDown())
        {
            --this.leftImpulse;
            this.right = true;
        }
        else
        {
            this.right = false;
        }

        boolean flag1 = false;
        float f = 0.0F;

        if (!flag && !minecraft.vrSettings.seated && minecraft.screen == null && !KeyboardHandler.Showing)
        {
            VRInputAction vrinputaction = minecraft.vr.getInputAction(minecraft.vr.keyFreeMoveStrafe);
            VRInputAction vrinputaction1 = minecraft.vr.getInputAction(minecraft.vr.keyFreeMoveRotate);
            Vector2 vector2 = vrinputaction.getAxis2DUseTracked();
            Vector2 vector21 = vrinputaction1.getAxis2DUseTracked();

            if (vector2.getX() == 0.0F && vector2.getY() == 0.0F)
            {
                if (vector21.getY() != 0.0F)
                {
                    flag1 = true;
                    f = vector21.getY();

                    if (minecraft.vrSettings.analogMovement)
                    {
                        this.forwardImpulse = vector21.getY();
                        this.leftImpulse = 0.0F;
                        this.leftImpulse -= getMovementAxisValue(this.gameSettings.keyRight);
                        this.leftImpulse += getMovementAxisValue(this.gameSettings.keyLeft);
                    }
                    else
                    {
                        this.forwardImpulse = this.axisToDigitalMovement(vector21.getY());
                    }
                }
                else if (minecraft.vrSettings.analogMovement)
                {
                    flag1 = true;
                    this.forwardImpulse = 0.0F;
                    this.leftImpulse = 0.0F;
                    float f1 = getMovementAxisValue(this.gameSettings.keyUp);

                    if (f1 == 0.0F)
                    {
                        f1 = getMovementAxisValue(minecraft.vr.keyTeleportFallback);
                    }

                    f = f1;
                    this.forwardImpulse += f1;
                    this.forwardImpulse -= getMovementAxisValue(this.gameSettings.keyDown);
                    this.leftImpulse -= getMovementAxisValue(this.gameSettings.keyRight);
                    this.leftImpulse += getMovementAxisValue(this.gameSettings.keyLeft);
                    float f2 = 0.05F;
                    this.forwardImpulse = Utils.applyDeadzone(this.forwardImpulse, f2);
                    this.leftImpulse = Utils.applyDeadzone(this.leftImpulse, f2);
                }
            }
            else
            {
                flag1 = true;
                f = vector2.getY();

                if (minecraft.vrSettings.analogMovement)
                {
                    this.forwardImpulse = vector2.getY();
                    this.leftImpulse = -vector2.getX();
                }
                else
                {
                    this.forwardImpulse = this.axisToDigitalMovement(vector2.getY());
                    this.leftImpulse = this.axisToDigitalMovement(-vector2.getX());
                }
            }

            if (flag1)
            {
                this.movementSetByAnalog = true;
                this.up = this.forwardImpulse > 0.0F;
                this.down = this.forwardImpulse < 0.0F;
                this.left = this.leftImpulse > 0.0F;
                this.right = this.leftImpulse < 0.0F;
                VRInputAction.setKeyBindState(this.gameSettings.keyUp, this.up);
                VRInputAction.setKeyBindState(this.gameSettings.keyDown, this.down);
                VRInputAction.setKeyBindState(this.gameSettings.keyLeft, this.left);
                VRInputAction.setKeyBindState(this.gameSettings.keyRight, this.right);

                if (minecraft.vrSettings.autoSprint)
                {
                    if (f >= minecraft.vrSettings.autoSprintThreshold)
                    {
                        minecraft.player.setSprinting(true);
                        this.autoSprintActive = true;
                        this.forwardImpulse = 1.0F;
                    }
                    else if (this.forwardImpulse > 0.0F && minecraft.vrSettings.analogMovement)
                    {
                        this.forwardImpulse = this.forwardImpulse / minecraft.vrSettings.autoSprintThreshold * 1.0F;
                    }
                }
            }
        }

        if (!flag1 && this.movementSetByAnalog)
        {
            VRInputAction.setKeyBindState(this.gameSettings.keyUp, false);
            VRInputAction.setKeyBindState(this.gameSettings.keyDown, false);
            VRInputAction.setKeyBindState(this.gameSettings.keyLeft, false);
            VRInputAction.setKeyBindState(this.gameSettings.keyRight, false);
        }

        this.movementSetByAnalog = flag1;

        if (this.autoSprintActive && f < minecraft.vrSettings.autoSprintThreshold)
        {
            minecraft.player.setSprinting(false);
            this.autoSprintActive = false;
        }

        boolean flag2 = minecraft.screen == null && (minecraft.vrPlayer.getFreeMove() || minecraft.vrSettings.simulateFalling) && !flag;
        this.jumping = this.gameSettings.keyJump.isDown() && flag2;
        this.shiftKeyDown = (minecraft.sneakTracker.sneakCounter > 0 || minecraft.sneakTracker.sneakOverride || this.gameSettings.keyShift.isDown()) && minecraft.screen == null;

        if (p_108576_)
        {
            this.leftImpulse = (float)((double)this.leftImpulse * 0.3D);
            this.forwardImpulse = (float)((double)this.forwardImpulse * 0.3D);
        }
    }
}
