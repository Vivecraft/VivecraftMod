package org.vivecraft.client_vr.provider.openxr_lwjgl;

import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HapticScheduler;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.concurrent.TimeUnit;

public class OpenXRHapticScheduler extends HapticScheduler {

    @Override
    public void queueHapticPulse(
        ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds)
    {
        this.executor.schedule(() -> {
            MCOpenXR openxr = MCOpenXR.get();
            if (openxr != null) {
                int result = openxr.triggerHaptic(controller, durationSeconds, frequency, amplitude);
                if (result < 0) {
                    VRSettings.LOGGER.error("Vivecraft: Error triggering OpenXR haptic: {}",
                        OpenXRUtil.resultToString(result));
                }
            }
        }, (long) (delaySeconds * 1000000.0F), TimeUnit.MICROSECONDS);
    }
}
