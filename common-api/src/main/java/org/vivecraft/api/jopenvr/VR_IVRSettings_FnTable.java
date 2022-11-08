package org.vivecraft.api.jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRSettings_FnTable extends Structure
{
    public GetSettingsErrorNameFromEnum_callback GetSettingsErrorNameFromEnum;
    public Sync_callback Sync;
    public SetBool_callback SetBool;
    public SetInt32_callback SetInt32;
    public SetFloat_callback SetFloat;
    public SetString_callback SetString;
    public GetBool_callback GetBool;
    public GetInt32_callback GetInt32;
    public GetFloat_callback GetFloat;
    public GetString_callback GetString;
    public RemoveSection_callback RemoveSection;
    public RemoveKeyInSection_callback RemoveKeyInSection;

    public VR_IVRSettings_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("GetSettingsErrorNameFromEnum", "Sync", "SetBool", "SetInt32", "SetFloat", "SetString", "GetBool", "GetInt32", "GetFloat", "GetString", "RemoveSection", "RemoveKeyInSection");
    }

    public VR_IVRSettings_FnTable(Pointer peer)
    {
        super(peer);
    }

    public static class ByReference extends VR_IVRSettings_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRSettings_FnTable implements Structure.ByValue
    {
    }

    public interface GetBool_callback extends Callback
    {
        byte apply(Pointer var1, Pointer var2, IntByReference var3);
    }

    public interface GetFloat_callback extends Callback
    {
        float apply(Pointer var1, Pointer var2, IntByReference var3);
    }

    public interface GetInt32_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, IntByReference var3);
    }

    public interface GetSettingsErrorNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetString_callback extends Callback
    {
        void apply(Pointer var1, Pointer var2, Pointer var3, int var4, IntByReference var5);
    }

    public interface RemoveKeyInSection_callback extends Callback
    {
        void apply(Pointer var1, Pointer var2, IntByReference var3);
    }

    public interface RemoveSection_callback extends Callback
    {
        void apply(Pointer var1, IntByReference var2);
    }

    public interface SetBool_callback extends Callback
    {
        void apply(Pointer var1, Pointer var2, byte var3, IntByReference var4);
    }

    public interface SetFloat_callback extends Callback
    {
        void apply(Pointer var1, Pointer var2, float var3, IntByReference var4);
    }

    public interface SetInt32_callback extends Callback
    {
        void apply(Pointer var1, Pointer var2, int var3, IntByReference var4);
    }

    public interface SetString_callback extends Callback
    {
        void apply(Pointer var1, Pointer var2, Pointer var3, IntByReference var4);
    }

    public interface Sync_callback extends Callback
    {
        byte apply(byte var1, IntByReference var2);
    }
}
