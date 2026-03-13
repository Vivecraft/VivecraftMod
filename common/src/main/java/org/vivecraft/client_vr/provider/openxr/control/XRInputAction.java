package org.vivecraft.client_vr.provider.openxr.control;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.vivecraft.client_vr.provider.control.ActionType;
import org.vivecraft.client_vr.provider.control.ControllerType;
import org.vivecraft.client_vr.provider.control.InputAction;
import org.vivecraft.client_vr.provider.control.VRInputActionSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XRInputAction extends InputAction {

    private final Map<String, List<HandedAction>> handles = new HashMap<>();
    public int activeAction;

    public record HandedAction(Long handle, ControllerType hand, ActionType action) {}

    public final List<DigitalData> digitalData = new ArrayList<>();
    public final List<AnalogData> analogData = new ArrayList<>();

    public XRInputAction(
        KeyMapping keyMapping, String requirement, ActionType type, VRInputActionSet actionSetOverride)
    {
        super(keyMapping, requirement, type, actionSetOverride);
        this.type = null; //No global type, type is part of the

        for (int c = 0; c < ControllerType.values().length; c++) {
            this.enabled[c] = true;
        }
    }

    public List<HandedAction> getHandle(String controller) {
        return handles.getOrDefault(controller, List.of());
    }

    public void addHandle(String controller, Long handle, ControllerType hand, ActionType actionType) {
        if (!handles.containsKey(controller)) {
            var list = new ArrayList<HandedAction>();
            list.add(new HandedAction(handle, hand, actionType));
            handles.put(controller, list);
        } else {
            handles.get(controller).add(new HandedAction(handle, hand, actionType));
        }
        this.analogData.add(new AnalogData());
        this.digitalData.add(new DigitalData());
    }

    private List<DigitalData> digitalData() {
        return this.digitalData;
    }

    private List<AnalogData> analogData() {
        return this.analogData;
    }

    @Nullable
    public HandedAction getActiveAction(String[] controllers) {
        if (this.activeAction == 0) {
            return null;
        }
        return this.handles.get(controllers[this.digitalData().get(this.activeAction).hand.ordinal()])
            .get(this.activeAction);
    }

    @Override
    public boolean isActive() {
        for (int i = 0; i < this.digitalData().size(); i++) {
            DigitalData data = this.digitalData().get(i);
            if (data.type == null) {
                continue;
            }
            switch (data.type) {
                case PRESS, DOUBLE_PRESS, LONG_PRESS, HOLD, TOGGLE -> {
                    if (this.digitalData().get(i).isActive) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case VEC1, VEC2 -> {
                    if (this.analogData().get(i).isActive) {
                        this.activeAction = i;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isButtonPressed() {
        for (int i = 0; i < this.digitalData().size(); i++) {
            DigitalData data = this.digitalData().get(i);
            if (data.type == null) {
                continue;
            }
            switch (data.type) {
                case DOUBLE_PRESS -> {
                    if (data.doublePress) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case LONG_PRESS -> {
                    if (data.longPress) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case HOLD -> {
                    if (data.hold) {
                        this.activeAction = i;
                        return true;
                    }
                }
                case TOGGLE -> {
                    if (data.toggle) {
                        this.activeAction = i;
                        return true;
                    }
                }
            }
            if (data.state) {
                this.activeAction = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isButtonChanged() {
        for (int i = 0; i < this.digitalData().size(); i++) {
            if (this.digitalData().get(i).isChanged) {
                this.activeAction = i;
                return true;
            }
        }
        return false;
    }

    @Override
    public long getLastOrigin() {
        if (this.activeAction == 0) {
            return 0;
        }
        return digitalData().get(this.activeAction).activeOrigin;
    }

    //Multi-binds are a bit messy
    @Override
    public float getAxis1D(boolean delta) {
        for (int i = 0; i < this.analogData().size(); i++) {
            AnalogData data = this.analogData().get(i);
            if (delta) {
                if (Math.abs(data.deltaX) > 0.0f) {
                    this.activeAction = i;
                    return data.deltaX;
                }
            } else {
                if (Math.abs(data.x) > 0.0f) {
                    this.activeAction = i;
                    return data.x;
                }
            }
        }
        return 0;
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

    //Multi-binds are a bit messy
    @Override
    public Vector2fc getAxis2D(boolean delta) {
        for (int i = 0; i < this.analogData().size(); i++) {
            AnalogData data = this.analogData().get(i);
            if (delta) {
                if (Math.abs(data.deltaX) > 0.0f || Math.abs(data.deltaY) > 0.0f) {
                    this.activeAction = i;
                    return new Vector2f(data.deltaX, data.deltaY);
                }
            } else {
                if (Math.abs(data.x) > 0.0f || Math.abs(data.y) > 0.0f) {
                    this.activeAction = i;
                    return new Vector2f(data.x, data.y);
                }
            }
        }
        return new Vector2f();
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
        public long lastChange;
        public boolean longPress;
        public boolean toggle;
        public boolean doublePress;
        public boolean hold;
        public long activeOrigin;
        public ActionType type;
        public ControllerType hand;
    }
}
