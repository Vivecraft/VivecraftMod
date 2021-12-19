package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VREvent_Notification_t extends Structure
{
    public long ulUserValue;
    public int notificationId;

    public VREvent_Notification_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("ulUserValue", "notificationId");
    }

    public VREvent_Notification_t(long ulUserValue, int notificationId)
    {
        this.ulUserValue = ulUserValue;
        this.notificationId = notificationId;
    }

    public VREvent_Notification_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VREvent_Notification_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VREvent_Notification_t implements com.sun.jna.Structure.ByValue
    {
    }
}
