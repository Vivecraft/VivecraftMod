package jopenvr;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class DistortionCoordinates_t extends Structure
{
    public float[] rfRed = new float[2];
    public float[] rfGreen = new float[2];
    public float[] rfBlue = new float[2];

    public DistortionCoordinates_t()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("rfRed", "rfGreen", "rfBlue");
    }

    public DistortionCoordinates_t(float[] rfRed, float[] rfGreen, float[] rfBlue)
    {
        if (rfRed.length != this.rfRed.length)
        {
            throw new IllegalArgumentException("Wrong array size !");
        }
        else
        {
            this.rfRed = rfRed;

            if (rfGreen.length != this.rfGreen.length)
            {
                throw new IllegalArgumentException("Wrong array size !");
            }
            else
            {
                this.rfGreen = rfGreen;

                if (rfBlue.length != this.rfBlue.length)
                {
                    throw new IllegalArgumentException("Wrong array size !");
                }
                else
                {
                    this.rfBlue = rfBlue;
                }
            }
        }
    }

    public DistortionCoordinates_t(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends DistortionCoordinates_t implements com.sun.jna.Structure.ByReference
    {
    }

    public static class ByValue extends DistortionCoordinates_t implements com.sun.jna.Structure.ByValue
    {
    }
}
