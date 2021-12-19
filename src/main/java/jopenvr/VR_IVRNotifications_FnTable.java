package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVRNotifications_FnTable extends Structure
{
    public VR_IVRNotifications_FnTable.CreateNotification_callback CreateNotification;
    public VR_IVRNotifications_FnTable.RemoveNotification_callback RemoveNotification;

    public VR_IVRNotifications_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("CreateNotification", "RemoveNotification");
    }

    public VR_IVRNotifications_FnTable(VR_IVRNotifications_FnTable.CreateNotification_callback CreateNotification, VR_IVRNotifications_FnTable.RemoveNotification_callback RemoveNotification)
    {
        this.CreateNotification = CreateNotification;
        this.RemoveNotification = RemoveNotification;
    }

    public VR_IVRNotifications_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRNotifications_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRNotifications_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface CreateNotification_callback extends Callback
    {
        int apply(long var1, long var3, int var5, Pointer var6, int var7, NotificationBitmap_t var8, IntByReference var9);
    }

    public interface RemoveNotification_callback extends Callback
    {
        int apply(int var1);
    }
}
