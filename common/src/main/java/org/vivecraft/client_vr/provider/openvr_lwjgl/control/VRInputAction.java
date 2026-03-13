package org.vivecraft.client_vr.provider.openvr_lwjgl.control;

import net.minecraft.client.KeyMapping;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client_vr.provider.control.ActionType;
import org.vivecraft.client_vr.provider.control.ControllerType;
import org.vivecraft.client_vr.provider.control.InputAction;
import org.vivecraft.client_vr.provider.control.VRInputActionSet;

public class VRInputAction extends InputAction {

    public long handle;
    public final DigitalData[] digitalData = new DigitalData[ControllerType.values().length];
    public final AnalogData[] analogData = new AnalogData[ControllerType.values().length];

    public VRInputAction(
        KeyMapping keyMapping, String requirement, ActionType type, VRInputActionSet actionSetOverride)
    {
        super(keyMapping, requirement, type, actionSetOverride);

        for (int c = 0; c < ControllerType.values().length; c++) {
            this.enabled[c] = true;
            this.analogData[c] = new AnalogData();
            this.digitalData[c] = new DigitalData();
        }
    }

    @Override
    public boolean isButtonPressed() {
        if (this.type == ActionType.PRESS) {
            return this.digitalData().state;
        } else {
            Vector3fc axis = this.getAxis3D(false);
            return Math.abs(axis.x()) > 0.5F || Math.abs(axis.y()) > 0.5F || Math.abs(axis.z()) > 0.5F;
        }
    }

    @Override
    public boolean isButtonChanged() {
        if (this.type == ActionType.PRESS) {
            return this.digitalData().isChanged;
        } else {
            Vector3fc axis = this.getAxis3D(false);
            Vector3fc delta = this.getAxis3D(true);
            return Math.abs(axis.x() - delta.x()) > 0.5F != Math.abs(axis.x()) > 0.5F ||
                Math.abs(axis.y() - delta.y()) > 0.5F != Math.abs(axis.y()) > 0.5F ||
                Math.abs(axis.z() - delta.z()) > 0.5F != Math.abs(axis.z()) > 0.5F;
        }
    }

    @Override
    public float getAxis1D(boolean delta) {
        return switch (this.type) {
            case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> this.digitalToAnalog(delta);
            case VEC1, VEC2 -> delta ? this.analogData().deltaX : this.analogData().x;
            default -> 0.0F;
        };
    }

    @Override
    public Vector2fc getAxis2D(boolean delta) {
        return switch (this.type) {
            case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> new Vector2f(this.digitalToAnalog(delta), 0.0F);
            case VEC1 -> delta ? new Vector2f(this.analogData().deltaX, 0.0F) : new Vector2f(this.analogData().x, 0.0F);
            case VEC2 -> delta ? new Vector2f(this.analogData().deltaX, this.analogData().deltaY) :
                new Vector2f(this.analogData().x, this.analogData().y);
            default -> new Vector2f();
        };
    }

    //TODO remove
    public Vector3fc getAxis3D(boolean delta) {
        return switch (this.type) {
            case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE ->
                new Vector3f(this.digitalToAnalog(delta), 0.0F, 0.0F);
            case VEC1 -> delta ? new Vector3f(this.analogData().deltaX, 0.0F, 0.0F) :
                new Vector3f(this.analogData().x, 0.0F, 0.0F);
            case VEC2 -> delta ? new Vector3f(this.analogData().deltaX, this.analogData().deltaY, 0.0F) :
                new Vector3f(this.analogData().x, this.analogData().y, 0.0F);
//            case "vector3" ->
//                delta ? new Vector3f(this.analogData().deltaX, this.analogData().deltaY, this.analogData().deltaZ) :
//                    new Vector3f(this.analogData().x, this.analogData().y, this.analogData().z);
            default -> new Vector3f();
        };
    }

    /**
     * This special variant of getAxis1D internally handles the isEnabled check and will continue
     * to give an output even after disabled until the user lets go of the input.
     * Cannot provide delta values as it wouldn't make any sense.
     */
    @Override
    public float getAxis1DUseTracked() {
        if (this.currentlyInUse || this.isEnabled()) {
            float axis = this.getAxis1D(false);
            this.currentlyInUse = axis != 0.0F;
            return axis;
        } else {
            return 0.0F;
        }
    }

    /**
     * This special variant of getAxis2D internally handles the isEnabled check and will continue
     * to give an output even after disabled until the user lets go of the input.
     * Cannot provide delta values as it wouldn't make any sense.
     */
    @Override
    public Vector2fc getAxis2DUseTracked() {
        if (this.currentlyInUse || this.isEnabled()) {
            Vector2fc axis = this.getAxis2D(false);
            this.currentlyInUse = axis.x() != 0.0F || axis.y() != 0.0F;
            return axis;
        } else {
            return new Vector2f();
        }
    }

    private float digitalToAnalog(boolean delta) {
        if (delta) {
            if (this.digitalData().isChanged) {
                return this.digitalData().state ? 1.0F : -1.0F;
            } else {
                return 0.0F;
            }
        } else {
            return this.digitalData().state ? 1.0F : 0.0F;
        }
    }

    /**
     * @return the last origin the vr runtime sent, if the VRInputAction is not currently in an active set this is likely 0
     */
    @Override
    public long getLastOrigin() {
        return switch (this.type) {
            case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> this.digitalData().activeOrigin;
            case VEC1, VEC2 -> this.analogData().activeOrigin;
            default -> 0L;
        };
    }

    private DigitalData digitalData() {
        return this.isHanded() ? this.digitalData[this.currentHand.ordinal()] : this.digitalData[0];
    }

    private AnalogData analogData() {
        return this.isHanded() ? this.analogData[this.currentHand.ordinal()] : this.analogData[0];
    }

    public void setHandle(long handle) {
        if (this.handle != 0L) {
            throw new IllegalStateException("Handle already assigned!");
        } else {
            this.handle = handle;
        }
    }

    @Override
    public boolean isActive() {
        return switch (this.type) {
            case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> this.digitalData().isActive;
            case VEC1, VEC2 -> this.analogData().isActive;
            default -> false;
        };
    }

    public static class AnalogData {
        public float x;
        public float y;
        public float z;
        public float deltaX;
        public float deltaY;
        public float deltaZ;
        public boolean isChanged;
        public boolean isActive;
        public long activeOrigin;
    }

    public static class DigitalData {
        public boolean state;
        public boolean isChanged;
        public boolean isActive;
        public long activeOrigin;
        public long lastChange;
    }
}
