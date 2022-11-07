package jopenvr;

import com.sun.jna.IntegerType;

public class NativeLong64 extends IntegerType
{
    public static final int SIZE = 8;

    public NativeLong64()
    {
        this(0L);
    }

    public NativeLong64(long value)
    {
        super(8, value);
    }
}
