package org.vivecraft.client_vr.utils.external;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;

public class jkatvr implements Library
{
    public static final String KATVR_LIBRARY_NAME = "WalkerBase.dll";
    public static final NativeLibrary KATVR_NATIVE_LIB = NativeLibrary.getInstance("WalkerBase.dll");
    static float yaw;
    static float yawOffset;
    static double power;
    static int direction;
    static int ismoving;
    static IntByReference y = new IntByReference();
    static IntByReference m = new IntByReference();
    static IntByReference is = new IntByReference();
    static DoubleByReference pow = new DoubleByReference();
    static FloatByReference fl = new FloatByReference();
    static float mag = 0.15F;
    static float bmag = 0.1F;
    static float maxpower = 3000.0F;

    public static native void Init(int var0);

    public static native int Launch();

    public static native boolean CheckForLaunch();

    public static native void Halt();

    public static native boolean GetWalkerData(int var0, IntByReference var1, DoubleByReference var2, IntByReference var3, IntByReference var4, FloatByReference var5);

    public static void query()
    {
        try
        {
            boolean flag = GetWalkerData(0, y, pow, m, is, fl);
            yaw = (float)y.getValue();
            power = pow.getValue();
            direction = -m.getValue();
            ismoving = is.getValue();
            yaw = yaw / 1024.0F * 360.0F;
        }
        catch (Exception exception)
        {
            System.out.println("KATVR Error: " + exception.getMessage());
        }
    }

    public static float getYaw()
    {
        return yaw - yawOffset;
    }

    public static boolean isMoving()
    {
        return ismoving == 1;
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
        Native.register(jkatvr.class, KATVR_NATIVE_LIB);
    }
}
