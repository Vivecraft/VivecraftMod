package org.vivecraft.provider.openvr_jna;

import java.util.concurrent.TimeUnit;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.provider.HapticScheduler;

public class OpenVRHapticScheduler extends HapticScheduler
{
    private void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude)
    {
        int i = MCOpenVR.get().vrInput.TriggerHapticVibrationAction.apply(MCOpenVR.get().getHapticHandle(controller), 0.0F, durationSeconds, frequency, amplitude, 0L);

        if (i != 0)
        {
            System.out.println("Error triggering haptic: " + MCOpenVR.getInputErrorName(i));
        }
    }

    public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds)
    {
        this.executor.schedule(() ->
        {
            this.triggerHapticPulse(controller, durationSeconds, frequency, amplitude);
        }, (long)(delaySeconds * 1000000.0F), TimeUnit.MICROSECONDS);
    }
}
