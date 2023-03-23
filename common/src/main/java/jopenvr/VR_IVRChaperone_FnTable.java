package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRChaperone_FnTable extends Structure
{
    public GetCalibrationState_callback GetCalibrationState;
    public GetPlayAreaSize_callback GetPlayAreaSize;
    public GetPlayAreaRect_callback GetPlayAreaRect;
    public ReloadInfo_callback ReloadInfo;
    public SetSceneColor_callback SetSceneColor;
    public GetBoundsColor_callback GetBoundsColor;
    public AreBoundsVisible_callback AreBoundsVisible;
    public ForceBoundsVisible_callback ForceBoundsVisible;

    public VR_IVRChaperone_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetCalibrationState", "GetPlayAreaSize", "GetPlayAreaRect", "ReloadInfo", "SetSceneColor", "GetBoundsColor", "AreBoundsVisible", "ForceBoundsVisible");
    }

    public VR_IVRChaperone_FnTable(GetCalibrationState_callback GetCalibrationState, GetPlayAreaSize_callback GetPlayAreaSize, GetPlayAreaRect_callback GetPlayAreaRect, ReloadInfo_callback ReloadInfo, SetSceneColor_callback SetSceneColor, GetBoundsColor_callback GetBoundsColor, AreBoundsVisible_callback AreBoundsVisible, ForceBoundsVisible_callback ForceBoundsVisible)
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

    public static class ByReference extends VR_IVRChaperone_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRChaperone_FnTable implements Structure.ByValue
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
