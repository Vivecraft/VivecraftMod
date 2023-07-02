package org.vivecraft.client_vr.provider.nullvr;


import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HapticScheduler;

public class NullVRHapticScheduler extends HapticScheduler
{
    public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds)
    {
    }
}
