package org.vivecraft.client_vr.provider.openvr_lwjgl;

import java.util.concurrent.TimeUnit;

import org.lwjgl.openvr.VRInput;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HapticScheduler;

public class OpenVRHapticScheduler extends HapticScheduler {
    private void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
        int i = VRInput.VRInput_TriggerHapticVibrationAction(MCOpenVR.get().getHapticHandle(controller), 0.0F, durationSeconds, frequency, amplitude, 0L);

        if (i != 0) {
            System.out.println("Error triggering haptic: " + MCOpenVR.getInputErrorName(i));
        }
    }

    public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
        this.executor.schedule(() ->
        {
            this.triggerHapticPulse(controller, durationSeconds, frequency, amplitude);
        }, (long) (delaySeconds * 1000000.0F), TimeUnit.MICROSECONDS);
    }
}
