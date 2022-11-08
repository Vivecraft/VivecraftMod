package org.vivecraft.api.jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRChaperoneSetup_FnTable extends Structure
{
    public CommitWorkingCopy_callback CommitWorkingCopy;
    public RevertWorkingCopy_callback RevertWorkingCopy;
    public GetWorkingPlayAreaSize_callback GetWorkingPlayAreaSize;
    public GetWorkingPlayAreaRect_callback GetWorkingPlayAreaRect;
    public GetWorkingCollisionBoundsInfo_callback GetWorkingCollisionBoundsInfo;
    public GetLiveCollisionBoundsInfo_callback GetLiveCollisionBoundsInfo;
    public GetWorkingSeatedZeroPoseToRawTrackingPose_callback GetWorkingSeatedZeroPoseToRawTrackingPose;
    public GetWorkingStandingZeroPoseToRawTrackingPose_callback GetWorkingStandingZeroPoseToRawTrackingPose;
    public SetWorkingPlayAreaSize_callback SetWorkingPlayAreaSize;
    public SetWorkingCollisionBoundsInfo_callback SetWorkingCollisionBoundsInfo;
    public SetWorkingPerimeter_callback SetWorkingPerimeter;
    public SetWorkingSeatedZeroPoseToRawTrackingPose_callback SetWorkingSeatedZeroPoseToRawTrackingPose;
    public SetWorkingStandingZeroPoseToRawTrackingPose_callback SetWorkingStandingZeroPoseToRawTrackingPose;
    public ReloadFromDisk_callback ReloadFromDisk;
    public GetLiveSeatedZeroPoseToRawTrackingPose_callback GetLiveSeatedZeroPoseToRawTrackingPose;
    public ExportLiveToBuffer_callback ExportLiveToBuffer;
    public ImportFromBufferToWorking_callback ImportFromBufferToWorking;
    public ShowWorkingSetPreview_callback ShowWorkingSetPreview;
    public HideWorkingSetPreview_callback HideWorkingSetPreview;
    public RoomSetupStarting_callback RoomSetupStarting;

    public VR_IVRChaperoneSetup_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("CommitWorkingCopy", "RevertWorkingCopy", "GetWorkingPlayAreaSize", "GetWorkingPlayAreaRect", "GetWorkingCollisionBoundsInfo", "GetLiveCollisionBoundsInfo", "GetWorkingSeatedZeroPoseToRawTrackingPose", "GetWorkingStandingZeroPoseToRawTrackingPose", "SetWorkingPlayAreaSize", "SetWorkingCollisionBoundsInfo", "SetWorkingPerimeter", "SetWorkingSeatedZeroPoseToRawTrackingPose", "SetWorkingStandingZeroPoseToRawTrackingPose", "ReloadFromDisk", "GetLiveSeatedZeroPoseToRawTrackingPose", "ExportLiveToBuffer", "ImportFromBufferToWorking", "ShowWorkingSetPreview", "HideWorkingSetPreview", "RoomSetupStarting");
    }

    public VR_IVRChaperoneSetup_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRChaperoneSetup_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRChaperoneSetup_FnTable implements Structure.ByValue
    {
    }

    public interface CommitWorkingCopy_callback extends Callback
    {
        byte apply(int var1);
    }

    public interface ExportLiveToBuffer_callback extends Callback
    {
        byte apply(Pointer var1, IntByReference var2);
    }

    public interface GetLiveCollisionBoundsInfo_callback extends Callback
    {
        byte apply(HmdQuad_t var1, IntByReference var2);
    }

    public interface GetLiveSeatedZeroPoseToRawTrackingPose_callback extends Callback
    {
        byte apply(HmdMatrix34_t var1);
    }

    public interface GetWorkingCollisionBoundsInfo_callback extends Callback
    {
        byte apply(HmdQuad_t var1, IntByReference var2);
    }

    public interface GetWorkingPlayAreaRect_callback extends Callback
    {
        byte apply(HmdQuad_t var1);
    }

    public interface GetWorkingPlayAreaSize_callback extends Callback
    {
        byte apply(FloatByReference var1, FloatByReference var2);
    }

    public interface GetWorkingSeatedZeroPoseToRawTrackingPose_callback extends Callback
    {
        byte apply(HmdMatrix34_t var1);
    }

    public interface GetWorkingStandingZeroPoseToRawTrackingPose_callback extends Callback
    {
        byte apply(HmdMatrix34_t var1);
    }

    public interface HideWorkingSetPreview_callback extends Callback
    {
        void apply();
    }

    public interface ImportFromBufferToWorking_callback extends Callback
    {
        byte apply(Pointer var1, int var2);
    }

    public interface ReloadFromDisk_callback extends Callback
    {
        void apply(int var1);
    }

    public interface RevertWorkingCopy_callback extends Callback
    {
        void apply();
    }

    public interface RoomSetupStarting_callback extends Callback
    {
        void apply();
    }

    public interface SetWorkingCollisionBoundsInfo_callback extends Callback
    {
        void apply(HmdQuad_t var1, int var2);
    }

    public interface SetWorkingPerimeter_callback extends Callback
    {
        void apply(HmdVector2_t var1, int var2);
    }

    public interface SetWorkingPlayAreaSize_callback extends Callback
    {
        void apply(float var1, float var2);
    }

    public interface SetWorkingSeatedZeroPoseToRawTrackingPose_callback extends Callback
    {
        void apply(HmdMatrix34_t var1);
    }

    public interface SetWorkingStandingZeroPoseToRawTrackingPose_callback extends Callback
    {
        void apply(HmdMatrix34_t var1);
    }

    public interface ShowWorkingSetPreview_callback extends Callback
    {
        void apply();
    }
}
