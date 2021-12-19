package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import java.util.Arrays;
import java.util.List;

public class VR_IVRChaperone_FnTable extends Structure
{
    public VR_IVRChaperone_FnTable.GetCalibrationState_callback GetCalibrationState;
    public VR_IVRChaperone_FnTable.GetPlayAreaSize_callback GetPlayAreaSize;
    public VR_IVRChaperone_FnTable.GetPlayAreaRect_callback GetPlayAreaRect;
    public VR_IVRChaperone_FnTable.ReloadInfo_callback ReloadInfo;
    public VR_IVRChaperone_FnTable.SetSceneColor_callback SetSceneColor;
    public VR_IVRChaperone_FnTable.GetBoundsColor_callback GetBoundsColor;
    public VR_IVRChaperone_FnTable.AreBoundsVisible_callback AreBoundsVisible;
    public VR_IVRChaperone_FnTable.ForceBoundsVisible_callback ForceBoundsVisible;

    public VR_IVRChaperone_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetCalibrationState", "GetPlayAreaSize", "GetPlayAreaRect", "ReloadInfo", "SetSceneColor", "GetBoundsColor", "AreBoundsVisible", "ForceBoundsVisible");
    }

    public VR_IVRChaperone_FnTable(VR_IVRChaperone_FnTable.GetCalibrationState_callback GetCalibrationState, VR_IVRChaperone_FnTable.GetPlayAreaSize_callback GetPlayAreaSize, VR_IVRChaperone_FnTable.GetPlayAreaRect_callback GetPlayAreaRect, VR_IVRChaperone_FnTable.ReloadInfo_callback ReloadInfo, VR_IVRChaperone_FnTable.SetSceneColor_callback SetSceneColor, VR_IVRChaperone_FnTable.GetBoundsColor_callback GetBoundsColor, VR_IVRChaperone_FnTable.AreBoundsVisible_callback AreBoundsVisible, VR_IVRChaperone_FnTable.ForceBoundsVisible_callback ForceBoundsVisible)
    {
        this.GetCalibrationState = GetCalibrationState;
        this.GetPlayAreaSize = GetPlayAreaSize;
        this.GetPlayAreaRect = GetPlayAreaRect;
        this.ReloadInfo = ReloadInfo;
        this.SetSceneColor = SetSceneColor;
        this.GetBoundsColor = GetBoundsColor;
        this.AreBoundsVisible = AreBoundsVisible;
        this.ForceBoundsVisible = ForceBoundsVisible;
    }

    public VR_IVRChaperone_FnTable(Pointer peer)
    {
        super(peer);
    }

    public interface AreBoundsVisible_callback extends Callback
    {
        byte apply();
    }

    public static class ByReference extends VR_IVRChaperone_FnTable implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRChaperone_FnTable implements com.sun.jna.Structure.ByValue
    {
    }

    public interface ForceBoundsVisible_callback extends Callback
    {
        void apply(byte var1);
    }

    public interface GetBoundsColor_callback extends Callback
    {
        void apply(HmdColor_t var1, int var2, float var3, HmdColor_t var4);
    }

    public interface GetCalibrationState_callback extends Callback
    {
        int apply();
    }

    public interface GetPlayAreaRect_callback extends Callback
    {
        byte apply(HmdQuad_t var1);
    }

    public interface GetPlayAreaSize_callback extends Callback
    {
        byte apply(FloatByReference var1, FloatByReference var2);
    }

    public interface ReloadInfo_callback extends Callback
    {
        void apply();
    }

    public interface SetSceneColor_callback extends Callback
    {
        void apply(HmdColor_t.ByValue var1);
    }
}
