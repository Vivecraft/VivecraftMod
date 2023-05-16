package org.vivecraft.client_vr.provider;

import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;

public class ActionParams
{
    public final VRInputActionSet actionSetOverride;
    public final String requirement;
    public final String type;

    public ActionParams(String requirement, String type, VRInputActionSet actionSetOverride)
    {
        this.requirement = requirement;
        this.type = type;
        this.actionSetOverride = actionSetOverride;
    }
}
