package org.vivecraft.client_vr.provider.openxr.control;

import net.minecraft.client.KeyMapping;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.vivecraft.client_vr.provider.control.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XRInputAction extends InputAction {

    private ControllerType hand;
    private final Map<String, List<Long>> handles = new HashMap<>();

    public final DigitalData[] digitalData = new DigitalData[ControllerType.values().length];
    public final AnalogData[] analogData = new AnalogData[ControllerType.values().length];

    public XRInputAction(KeyMapping keyMapping, String requirement, ActionType type, VRInputActionSet actionSetOverride) {
        super(keyMapping, requirement, type, actionSetOverride);

        for (int c = 0; c < ControllerType.values().length; c++) {
            this.enabled[c] = true;
            this.analogData[c] = new AnalogData();
            this.digitalData[c] = new DigitalData();
        }
    }

    public ControllerType getHand() {
        return hand;
    }

    public void setHand(ControllerType hand) {
        this.hand = hand;
    }

    public List<Long> getHandle(String controller) {
        return handles.getOrDefault(controller, List.of());
    }

    public void addHandle(String controller, Long handle) {
        if (!handles.containsKey(controller)) {
            var list = new ArrayList<Long>();
            list.add(handle);
            handles.put(controller, list);
        } else {
            handles.get(controller).add(handle);
        }
    }

    private DigitalData digitalData() {
        return this.isHanded() ? this.digitalData[this.currentHand.ordinal()] : this.digitalData[0];
    }

    private AnalogData analogData() {
        return this.isHanded() ? this.analogData[this.currentHand.ordinal()] : this.analogData[0];
    }

    @Override
    public boolean isActive() {
        return switch (this.type) {
            case BOOLEAN, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> this.digitalData().isActive;
            case VEC1, VEC2 -> this.analogData().isActive;
            default -> false;
        };
    }

    @Override
    public boolean isButtonPressed() {
        return this.digitalData().state;
    }

    @Override
    public boolean isButtonChanged() {
        return this.digitalData().isChanged;
    }

    @Override
    public long getLastOrigin() {
        return switch (this.type) {
            case BOOLEAN, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> this.digitalData().activeOrigin;
            case VEC1, VEC2 -> this.analogData().activeOrigin;
            default -> 0L;
        };
    }

    @Override
    public float getAxis1D(boolean delta) {
        return switch (this.type) {
            case BOOLEAN, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> this.analogData().x;
            case VEC1, VEC2 -> delta ? this.analogData().deltaX : this.analogData().x;
            default -> 0.0F;
        };
    }

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

    @Override
    public Vector2fc getAxis2D(boolean delta) {
        return switch (this.type) {
            case BOOLEAN, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> new Vector2f(this.analogData().x, 0.0F);
            case VEC1 -> delta ? new Vector2f(this.analogData().deltaX, 0.0F) : new Vector2f(this.analogData().x, 0.0F);
            case VEC2 -> delta ? new Vector2f(this.analogData().deltaX, this.analogData().deltaY) :
                new Vector2f(this.analogData().x, this.analogData().y);
            default -> new Vector2f();
        };
    }

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

    public static class AnalogData {
        public float x;
        public float y;
        public float deltaX;
        public float deltaY;
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
        public boolean longPress;
        public boolean toggle;
        public boolean doublePress;
        public boolean hold;
    }
}
