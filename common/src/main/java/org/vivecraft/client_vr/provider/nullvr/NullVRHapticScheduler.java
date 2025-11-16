package org.vivecraft.client_vr.provider.nullvr;


import org.vivecraft.client_vr.provider.HapticScheduler;
import org.vivecraft.client_vr.provider.control.ControllerType;

public class NullVRHapticScheduler extends HapticScheduler {

    @Override
    public void queueHapticPulse(
        ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds)
    {}
}
