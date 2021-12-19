package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class VRSkeletalSummaryData_t extends Structure
{
    public float[] flFingerCurl = new float[5];
    public float[] flFingerSplay = new float[4];

    public VRSkeletalSummaryData_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("flFingerCurl", "flFingerSplay");
    }

    public VRSkeletalSummaryData_t(float[] flFingerCurl, float[] flFingerSplay)
    {
        if (flFingerCurl.length != this.flFingerCurl.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.flFingerCurl = flFingerCurl;

            if (flFingerSplay.length != this.flFingerSplay.length)
            {
                throw new IllegalArgumentException("Wrong array size !");
            }
            else
            {
                this.flFingerSplay = flFingerSplay;
            }
        }
    }

    public VRSkeletalSummaryData_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VRSkeletalSummaryData_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends VRSkeletalSummaryData_t implements com.sun.jna.Structure.ByValue
    {
    }
}
