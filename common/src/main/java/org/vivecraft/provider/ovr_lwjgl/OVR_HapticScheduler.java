//package org.vivecraft.provider.ovr_lwjgl;
//
//import java.util.concurrent.TimeUnit;
//import org.lwjgl.ovr.OVR;
//import org.vivecraft.provider.ControllerType;
//import org.vivecraft.provider.HapticScheduler;
//
//public class OVR_HapticScheduler extends HapticScheduler
//{
//    private void triggerHapticPulse(ControllerType controller, float frequency, float amplitude)
//    {
//        OVR.ovr_SetControllerVibration(MC_OVR.get().session.get(0), controller == ControllerType.LEFT ? 1 : 2, frequency, amplitude);
//    }
//
//    public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds)
//    {
//        this.executor.schedule(() ->
//        {
//            this.triggerHapticPulse(controller, frequency, amplitude);
//        }, (long)(delaySeconds * 1000000.0F), TimeUnit.MICROSECONDS);
//        this.executor.schedule(() ->
//        {
//            this.triggerHapticPulse(controller, frequency, 0.0F);
//        }, (long)((durationSeconds + delaySeconds) * 1000000.0F), TimeUnit.MICROSECONDS);
//    }
//}
