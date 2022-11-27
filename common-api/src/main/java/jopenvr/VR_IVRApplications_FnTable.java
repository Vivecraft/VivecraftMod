package jopenvr;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

import java.util.Arrays;
import java.util.List;

public class VR_IVRApplications_FnTable extends Structure
{
    public AddApplicationManifest_callback AddApplicationManifest;
    public RemoveApplicationManifest_callback RemoveApplicationManifest;
    public IsApplicationInstalled_callback IsApplicationInstalled;
    public GetApplicationCount_callback GetApplicationCount;
    public GetApplicationKeyByIndex_callback GetApplicationKeyByIndex;
    public GetApplicationKeyByProcessId_callback GetApplicationKeyByProcessId;
    public LaunchApplication_callback LaunchApplication;
    public LaunchTemplateApplication_callback LaunchTemplateApplication;
    public LaunchApplicationFromMimeType_callback LaunchApplicationFromMimeType;
    public LaunchDashboardOverlay_callback LaunchDashboardOverlay;
    public CancelApplicationLaunch_callback CancelApplicationLaunch;
    public IdentifyApplication_callback IdentifyApplication;
    public GetApplicationProcessId_callback GetApplicationProcessId;
    public GetApplicationsErrorNameFromEnum_callback GetApplicationsErrorNameFromEnum;
    public GetApplicationPropertyString_callback GetApplicationPropertyString;
    public GetApplicationPropertyBool_callback GetApplicationPropertyBool;
    public GetApplicationPropertyUint64_callback GetApplicationPropertyUint64;
    public SetApplicationAutoLaunch_callback SetApplicationAutoLaunch;
    public GetApplicationAutoLaunch_callback GetApplicationAutoLaunch;
    public SetDefaultApplicationForMimeType_callback SetDefaultApplicationForMimeType;
    public GetDefaultApplicationForMimeType_callback GetDefaultApplicationForMimeType;
    public GetApplicationSupportedMimeTypes_callback GetApplicationSupportedMimeTypes;
    public GetApplicationsThatSupportMimeType_callback GetApplicationsThatSupportMimeType;
    public GetApplicationLaunchArguments_callback GetApplicationLaunchArguments;
    public GetStartingApplication_callback GetStartingApplication;
    public GetTransitionState_callback GetTransitionState;
    public PerformApplicationPrelaunchCheck_callback PerformApplicationPrelaunchCheck;
    public GetApplicationsTransitionStateNameFromEnum_callback GetApplicationsTransitionStateNameFromEnum;
    public IsQuitUserPromptRequested_callback IsQuitUserPromptRequested;
    public LaunchInternalProcess_callback LaunchInternalProcess;
    public GetCurrentSceneProcessId_callback GetCurrentSceneProcessId;

    public VR_IVRApplications_FnTable()
    {
    }

    protected List<String> getFieldOrder()
    {
        return Arrays.asList("AddApplicationManifest", "RemoveApplicationManifest", "IsApplicationInstalled", "GetApplicationCount", "GetApplicationKeyByIndex", "GetApplicationKeyByProcessId", "LaunchApplication", "LaunchTemplateApplication", "LaunchApplicationFromMimeType", "LaunchDashboardOverlay", "CancelApplicationLaunch", "IdentifyApplication", "GetApplicationProcessId", "GetApplicationsErrorNameFromEnum", "GetApplicationPropertyString", "GetApplicationPropertyBool", "GetApplicationPropertyUint64", "SetApplicationAutoLaunch", "GetApplicationAutoLaunch", "SetDefaultApplicationForMimeType", "GetDefaultApplicationForMimeType", "GetApplicationSupportedMimeTypes", "GetApplicationsThatSupportMimeType", "GetApplicationLaunchArguments", "GetStartingApplication", "GetTransitionState", "PerformApplicationPrelaunchCheck", "GetApplicationsTransitionStateNameFromEnum", "IsQuitUserPromptRequested", "LaunchInternalProcess", "GetCurrentSceneProcessId");
    }

    public VR_IVRApplications_FnTable(Pointer peer)
    {
        super(peer);
    }

    public interface AddApplicationManifest_callback extends Callback
    {
        int apply(Pointer var1, byte var2);
    }

    public static class ByReference extends VR_IVRApplications_FnTable implements Structure.ByReference
    {
    }

    public static class ByValue extends VR_IVRApplications_FnTable implements Structure.ByValue
    {
    }

    public interface CancelApplicationLaunch_callback extends Callback
    {
        byte apply(Pointer var1);
    }

    public interface GetApplicationAutoLaunch_callback extends Callback
    {
        byte apply(Pointer var1);
    }

    public interface GetApplicationCount_callback extends Callback
    {
        int apply();
    }

    public interface GetApplicationKeyByIndex_callback extends Callback
    {
        int apply(int var1, Pointer var2, int var3);
    }

    public interface GetApplicationKeyByProcessId_callback extends Callback
    {
        int apply(int var1, Pointer var2, int var3);
    }

    public interface GetApplicationLaunchArguments_callback extends Callback
    {
        int apply(int var1, Pointer var2, int var3);
    }

    public interface GetApplicationProcessId_callback extends Callback
    {
        int apply(Pointer var1);
    }

    public interface GetApplicationPropertyBool_callback extends Callback
    {
        byte apply(Pointer var1, int var2, IntByReference var3);
    }

    public interface GetApplicationPropertyString_callback extends Callback
    {
        int apply(Pointer var1, int var2, Pointer var3, int var4, IntByReference var5);
    }

    public interface GetApplicationPropertyUint64_callback extends Callback
    {
        long apply(Pointer var1, int var2, IntByReference var3);
    }

    public interface GetApplicationSupportedMimeTypes_callback extends Callback
    {
        byte apply(Pointer var1, Pointer var2, int var3);
    }

    public interface GetApplicationsErrorNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetApplicationsThatSupportMimeType_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, int var3);
    }

    public interface GetApplicationsTransitionStateNameFromEnum_callback extends Callback
    {
        Pointer apply(int var1);
    }

    public interface GetCurrentSceneProcessId_callback extends Callback
    {
        int apply();
    }

    public interface GetDefaultApplicationForMimeType_callback extends Callback
    {
        byte apply(Pointer var1, Pointer var2, int var3);
    }

    public interface GetStartingApplication_callback extends Callback
    {
        int apply(Pointer var1, int var2);
    }

    public interface GetTransitionState_callback extends Callback
    {
        int apply();
    }

    public interface IdentifyApplication_callback extends Callback
    {
        int apply(int var1, Pointer var2);
    }

    public interface IsApplicationInstalled_callback extends Callback
    {
        byte apply(Pointer var1);
    }

    public interface IsQuitUserPromptRequested_callback extends Callback
    {
        byte apply();
    }

    public interface LaunchApplicationFromMimeType_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2);
    }

    public interface LaunchApplication_callback extends Callback
    {
        int apply(Pointer var1);
    }

    public interface LaunchDashboardOverlay_callback extends Callback
    {
        int apply(Pointer var1);
    }

    public interface LaunchInternalProcess_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, Pointer var3);
    }

    public interface LaunchTemplateApplication_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2, AppOverrideKeys_t var3, int var4);
    }

    public interface PerformApplicationPrelaunchCheck_callback extends Callback
    {
        int apply(Pointer var1);
    }

    public interface RemoveApplicationManifest_callback extends Callback
    {
        int apply(Pointer var1);
    }

    public interface SetApplicationAutoLaunch_callback extends Callback
    {
        int apply(Pointer var1, byte var2);
    }

    public interface SetDefaultApplicationForMimeType_callback extends Callback
    {
        int apply(Pointer var1, Pointer var2);
    }
}
