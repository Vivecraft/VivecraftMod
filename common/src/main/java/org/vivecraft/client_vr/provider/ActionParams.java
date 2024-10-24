package org.vivecraft.client_vr.provider;

import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;

/**
 * holds the parameters for a VR action key
 */
public class ActionParams {
    public static final ActionParams DEFAULT = new ActionParams("optional", "boolean", null);
    public final VRInputActionSet actionSetOverride;
    public final String requirement;
    public final String type;

    /**
     * @param requirement if the action is "optional", "suggested" or "mandatory"
     * @param type input type of the action. one of "boolean", "vector1, "vector2" or "vector3"
     * @param actionSetOverride action set to put it in, any of {@link VRInputActionSet}
     */
    public ActionParams(String requirement, String type, VRInputActionSet actionSetOverride) {
        this.requirement = requirement;
        this.type = type;
        this.actionSetOverride = actionSetOverride;
    }
}
