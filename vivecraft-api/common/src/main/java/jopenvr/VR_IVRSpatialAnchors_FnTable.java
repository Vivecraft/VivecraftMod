package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRSpatialAnchors_FnTable extends Structure
{
    public CreateSpatialAnchorFromDescriptor_callback CreateSpatialAnchorFromDescriptor;
    public CreateSpatialAnchorFromPose_callback CreateSpatialAnchorFromPose;
    public GetSpatialAnchorPose_callback GetSpatialAnchorPose;
    public GetSpatialAnchorDescriptor_callback GetSpatialAnchorDescriptor;

    public VR_IVRSpatialAnchors_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("CreateSpatialAnchorFromDescriptor", "CreateSpatialAnchorFromPose", "GetSpatialAnchorPose", "GetSpatialAnchorDescriptor");
    }

    public VR_IVRSpatialAnchors_FnTable(CreateSpatialAnchorFromDescriptor_callback CreateSpatialAnchorFromDescriptor, CreateSpatialAnchorFromPose_callback CreateSpatialAnchorFromPose, GetSpatialAnchorPose_callback GetSpatialAnchorPose, GetSpatialAnchorDescriptor_callback GetSpatialAnchorDescriptor)
    {
        this.CreateSpatialAnchorFromDescriptor = CreateSpatialAnchorFromDescriptor;
        this.CreateSpatialAnchorFromPose = CreateSpatialAnchorFromPose;
        this.GetSpatialAnchorPose = GetSpatialAnchorPose;
        this.GetSpatialAnchorDescriptor = GetSpatialAnchorDescriptor;
    }

    public VR_IVRSpatialAnchors_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRSpatialAnchors_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRSpatialAnchors_FnTable implements Structure.ByValue
    {
    }

    public interface CreateSpatialAnchorFromDescriptor_callback extends Callback
    {
        int apply(Pointer var1, IntByReference var2);
    }

    public interface CreateSpatialAnchorFromPose_callback extends Callback
    {
        int apply(int var1, int var2, SpatialAnchorPose_t var3, IntByReference var4);
    }

    public interface GetSpatialAnchorDescriptor_callback extends Callback
    {
        int apply(int var1, Pointer var2, IntByReference var3);
    }

    public interface GetSpatialAnchorPose_callback extends Callback
    {
        int apply(int var1, int var2, SpatialAnchorPose_t var3);
    }
}
