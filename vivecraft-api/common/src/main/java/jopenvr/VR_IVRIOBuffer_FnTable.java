package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRIOBuffer_FnTable extends Structure
{
    public Open_callback Open;
    public Close_callback Close;
    public Read_callback Read;
    public Write_callback Write;
    public PropertyContainer_callback PropertyContainer;
    public HasReaders_callback HasReaders;

    public VR_IVRIOBuffer_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("Open", "Close", "Read", "Write", "PropertyContainer", "HasReaders");
    }

    public VR_IVRIOBuffer_FnTable(Open_callback Open, Close_callback Close, Read_callback Read, Write_callback Write, PropertyContainer_callback PropertyContainer, HasReaders_callback HasReaders)
    {
        this.Open = Open;
        this.Close = Close;
        this.Read = Read;
        this.Write = Write;
        this.PropertyContainer = PropertyContainer;
        this.HasReaders = HasReaders;
    }

    public VR_IVRIOBuffer_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRIOBuffer_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRIOBuffer_FnTable implements Structure.ByValue
    {
    }

    public interface Close_callback extends Callback
    {
        int apply(long var1);
    }

    public interface HasReaders_callback extends Callback
    {
        byte apply(long var1);
    }

    public interface Open_callback extends Callback
    {
        int apply(Pointer var1, int var2, int var3, int var4, LongByReference var5);
    }

    public interface PropertyContainer_callback extends Callback
    {
        long apply(long var1);
    }

    public interface Read_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4, IntByReference var5);
    }

    public interface Write_callback extends Callback
    {
        int apply(long var1, Pointer var3, int var4);
    }
}
