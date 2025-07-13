package org.vivecraft.client_vr.provider.openxr.control;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vivecraft.client_vr.provider.openxr.MCOpenXR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public record WrappedBindings(String path, String type) {

    public static List<WrappedBindings> quest2Bindings() {
        List<WrappedBindings> bindings = new ArrayList<>();

        bindings.add(new WrappedBindings("/user/hand/left/input/y/click", "boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/y/touch","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/x/click","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/x/touch","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/menu/click","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/squeeze/value","vector1"));
        bindings.add(new WrappedBindings("/user/hand/left/input/trigger/value","vector1"));
        bindings.add(new WrappedBindings("/user/hand/left/input/trigger/touch","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/trigger/proximity","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/thumb_resting_surfaces/proximity","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/thumbstick","vector2"));
        bindings.add(new WrappedBindings("/user/hand/left/input/thumbstick/click","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/thumbstick/touch","boolean"));
        bindings.add(new WrappedBindings("/user/hand/left/input/thumbstick/y","vector1"));
        bindings.add(new WrappedBindings("/user/hand/left/input/thumbstick/x","vector1"));
        bindings.add(new WrappedBindings("/user/hand/left/input/thumbrest/touch","boolean"));

        bindings.add(new WrappedBindings("/user/hand/right/input/a/click","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/a/touch","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/b/click","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/b/touch","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/system/click","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/squeeze/value","vector1"));
        bindings.add(new WrappedBindings("/user/hand/right/input/trigger/value","vector1"));
        bindings.add(new WrappedBindings("/user/hand/right/input/trigger/touch","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/trigger/proximity","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/thumb_resting_surfaces/proximity","boolean"));
        bindings.add(new WrappedBindings("/user/hand/right/input/thumbstick","vector2"));
        bindings.add(new WrappedBindings("/user/hand/right/input/thumbstick/click",""));
        bindings.add(new WrappedBindings("/user/hand/right/input/thumbstick/touch",""));
        bindings.add(new WrappedBindings("/user/hand/right/input/thumbstick/y","vector1"));
        bindings.add(new WrappedBindings("/user/hand/right/input/thumbstick/x","vector1"));
        bindings.add(new WrappedBindings("/user/hand/right/input/thumbrest/touch","boolean"));
        return bindings;
    }
}
