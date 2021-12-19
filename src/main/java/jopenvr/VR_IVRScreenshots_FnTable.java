package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVRScreenshots_FnTable extends Structure
{
    public VR_IVRScreenshots_FnTable.RequestScreenshot_callback RequestScreenshot;
    public VR_IVRScreenshots_FnTable.HookScreenshot_callback HookScreenshot;
    public VR_IVRScreenshots_FnTable.GetScreenshotPropertyType_callback GetScreenshotPropertyType;
    public VR_IVRScreenshots_FnTable.GetScreenshotPropertyFilename_callback GetScreenshotPropertyFilename;
    public VR_IVRScreenshots_FnTable.UpdateScreenshotProgress_callback UpdateScreenshotProgress;
    public VR_IVRScreenshots_FnTable.TakeStereoScreenshot_callback TakeStereoScreenshot;
    public VR_IVRScreenshots_FnTable.SubmitScreenshot_callback SubmitScreenshot;

    public VR_IVRScreenshots_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("RequestScreenshot", "HookScreenshot", "GetScreenshotPropertyType", "GetScreenshotPropertyFilename", "UpdateScreenshotProgress", "TakeStereoScreenshot", "SubmitScreenshot");
    }

    public VR_IVRScreenshots_FnTable(VR_IVRScreenshots_FnTable.RequestScreenshot_callback RequestScreenshot, VR_IVRScreenshots_FnTable.HookScreenshot_callback HookScreenshot, VR_IVRScreenshots_FnTable.GetScreenshotPropertyType_callback GetScreenshotPropertyType, VR_IVRScreenshots_FnTable.GetScreenshotPropertyFilename_callback GetScreenshotPropertyFilename, VR_IVRScreenshots_FnTable.UpdateScreenshotProgress_callback UpdateScreenshotProgress, VR_IVRScreenshots_FnTable.TakeStereoScreenshot_callback TakeStereoScreenshot, VR_IVRScreenshots_FnTable.SubmitScreenshot_callback SubmitScreenshot)
    {
        this.RequestScreenshot = RequestScreenshot;
        this.HookScreenshot = HookScreenshot;
        this.GetScreenshotPropertyType = GetScreenshotPropertyType;
        this.GetScreenshotPropertyFilename = GetScreenshotPropertyFilename;
        this.UpdateScreenshotProgress = UpdateScreenshotProgress;
        this.TakeStereoScreenshot = TakeStereoScreenshot;
        this.SubmitScreenshot = SubmitScreenshot;
    }

    public VR_IVRScreenshots_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRScreenshots_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRScreenshots_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface GetScreenshotPropertyFilename_callback extends Callback
    {
        int apply(int var1, int var2, Pointer var3, int var4, IntByReference var5);
    }

    public interface GetScreenshotPropertyType_callback extends Callback
    {
        int apply(int var1, IntByReference var2);
    }

    public interface HookScreenshot_callback extends Callback
    {
        int apply(IntByReference var1, int var2);
    }

    public interface RequestScreenshot_callback extends Callback
    {
        int apply(IntByReference var1, int var2, Pointer var3, Pointer var4);
    }

    public interface SubmitScreenshot_callback extends Callback
    {
        int apply(int var1, int var2, Pointer var3, Pointer var4);
    }

    public interface TakeStereoScreenshot_callback extends Callback
    {
        int apply(IntByReference var1, Pointer var2, Pointer var3);
    }

    public interface UpdateScreenshotProgress_callback extends Callback
    {
        int apply(int var1, float var2);
    }
}
