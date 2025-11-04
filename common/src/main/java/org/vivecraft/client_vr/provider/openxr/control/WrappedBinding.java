package org.vivecraft.client_vr.provider.openxr.control;

import org.vivecraft.client_vr.provider.control.ActionType;

import java.util.ArrayList;
import java.util.List;

public record WrappedBinding(String path, ActionType type) {

    public static List<WrappedBinding> quest2Bindings() {
        List<WrappedBinding> bindings = new ArrayList<>();

        bindings.add(new WrappedBinding("/user/hand/left/input/y/click", ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/y/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/x/click",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/x/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/menu/click",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/squeeze/value",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/left/input/trigger/value",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/left/input/trigger/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/trigger/proximity",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/thumb_resting_surfaces/proximity",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/thumbstick",ActionType.VEC2));
        bindings.add(new WrappedBinding("/user/hand/left/input/thumbstick/click",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/thumbstick/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/left/input/thumbstick/y",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/left/input/thumbstick/x",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/left/input/thumbrest/touch",ActionType.BOOLEAN));

        bindings.add(new WrappedBinding("/user/hand/right/input/a/click",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/a/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/b/click",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/b/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/system/click",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/squeeze/value",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/right/input/trigger/value",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/right/input/trigger/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/trigger/proximity",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/thumb_resting_surfaces/proximity",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/thumbstick",ActionType.VEC2));
        bindings.add(new WrappedBinding("/user/hand/right/input/thumbstick/click",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/thumbstick/touch",ActionType.BOOLEAN));
        bindings.add(new WrappedBinding("/user/hand/right/input/thumbstick/y",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/right/input/thumbstick/x",ActionType.VEC1));
        bindings.add(new WrappedBinding("/user/hand/right/input/thumbrest/touch",ActionType.BOOLEAN));
        return bindings;
    }
}
