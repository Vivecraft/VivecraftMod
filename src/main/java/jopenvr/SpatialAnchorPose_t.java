package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class SpatialAnchorPose_t extends Structure
{
    public HmdMatrix34_t mAnchorToAbsoluteTracking;

    public SpatialAnchorPose_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("mAnchorToAbsoluteTracking");
    }

    public SpatialAnchorPose_t(HmdMatrix34_t mAnchorToAbsoluteTracking)
    {
        this.mAnchorToAbsoluteTracking = mAnchorToAbsoluteTracking;
    }

    public SpatialAnchorPose_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends SpatialAnchorPose_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends SpatialAnchorPose_t implements com.sun.jna.Structure.ByValue
    {
    }
}
