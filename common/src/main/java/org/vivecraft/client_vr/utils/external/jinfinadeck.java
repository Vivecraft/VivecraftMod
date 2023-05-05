package org.vivecraft.client_vr.utils.external;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

public class jinfinadeck implements Library
{
    public static final String INFINADECK_LIBRARY_NAME = "InfinadeckAPI.dll";
    public static final NativeLibrary INFINADECK_NATIVE_LIB = NativeLibrary.getInstance("InfinadeckAPI.dll");
    static float yaw;
    static float yawOffset;
    static double power;
    static int direction;
    static boolean ismoving;
    static IntByReference y = new IntByReference();
    static IntByReference m = new IntByReference();
    static IntByReference is = new IntByReference();
    static DoubleByReference pow = new DoubleByReference();
    static FloatByReference fl = new FloatByReference();
    static float mag = 0.15F;
    static float bmag = 0.1F;
    static float maxpower = 2.0F;

    public static native int InitInternal(IntByReference var0, boolean var1);

    public static native int DeInitInternal();

    public static native boolean CheckConnection();

    public static native boolean GetTreadmillRunState();

    public static native double GetFloorSpeedAngle();

    public static native double GetFloorSpeedMagnitude();

    public static boolean InitConnection()
    {
        IntByReference intbyreference = new IntByReference();
        InitInternal(intbyreference, false);

        if (intbyreference.getValue() != 0)
        {
            InitInternal(intbyreference, true);
        }

        return intbyreference.getValue() == 0;
    }

    public static void Destroy()
    {
        DeInitInternal();
    }

    public static void query()
    {
        try
        {
            if (CheckConnection())
            {
            }

            yaw = (float)GetFloorSpeedAngle();
            power = GetFloorSpeedMagnitude();
            direction = 1;
            ismoving = GetTreadmillRunState();
            yaw *= 57.296F;
        }
        catch (Exception exception)
        {
            System.out.println("Infinadeck Error: " + exception.getMessage());
        }
    }

    public static float getYaw()
    {
        return yaw - yawOffset;
    }

    public static boolean isMoving()
    {
        return true;
    }

    public static void resetYaw(float offsetDegrees)
    {
        yawOffset = offsetDegrees + yaw;
    }

    public static float walkDirection()
    {
        return (float)direction;
    }

    public static float getSpeed()
    {
        return (float)(power / (double)maxpower * (double)(walkDirection() == 1.0F ? mag : bmag));
    }

    static
    {
        Native.register(jinfinadeck.class, INFINADECK_NATIVE_LIB);
    }
}
