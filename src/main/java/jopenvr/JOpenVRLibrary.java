package jopenvr;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.ptr.IntByReference;
import java.nio.IntBuffer;

public class JOpenVRLibrary implements Library
{
    public static final String JNA_LIBRARY_NAME = "openvr_api";
    public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance("openvr_api");
    public static final int k_nDriverNone = -1;
    public static final int k_unMaxDriverDebugResponseSize = 32768;
    public static final int k_unTrackedDeviceIndex_Hmd = 0;
    public static final int k_unMaxTrackedDeviceCount = 64;
    public static final int k_unTrackedDeviceIndexOther = -2;
    public static final int k_unTrackedDeviceIndexInvalid = -1;
    public static final long k_ulInvalidPropertyContainer = 0L;
    public static final int k_unInvalidPropertyTag = 0;
    public static final long k_ulInvalidDriverHandle = 0L;
    public static final int k_unFloatPropertyTag = 1;
    public static final int k_unInt32PropertyTag = 2;
    public static final int k_unUint64PropertyTag = 3;
    public static final int k_unBoolPropertyTag = 4;
    public static final int k_unStringPropertyTag = 5;
    public static final int k_unHmdMatrix34PropertyTag = 20;
    public static final int k_unHmdMatrix44PropertyTag = 21;
    public static final int k_unHmdVector3PropertyTag = 22;
    public static final int k_unHmdVector4PropertyTag = 23;
    public static final int k_unHmdVector2PropertyTag = 24;
    public static final int k_unHmdQuadPropertyTag = 25;
    public static final int k_unHiddenAreaPropertyTag = 30;
    public static final int k_unPathHandleInfoTag = 31;
    public static final int k_unActionPropertyTag = 32;
    public static final int k_unInputValuePropertyTag = 33;
    public static final int k_unWildcardPropertyTag = 34;
    public static final int k_unHapticVibrationPropertyTag = 35;
    public static final int k_unSkeletonPropertyTag = 36;
    public static final int k_unSpatialAnchorPosePropertyTag = 40;
    public static final int k_unJsonPropertyTag = 41;
    public static final int k_unActiveActionSetPropertyTag = 42;
    public static final int k_unOpenVRInternalReserved_Start = 1000;
    public static final int k_unOpenVRInternalReserved_End = 10000;
    public static final int k_unMaxPropertyStringSize = 32768;
    public static final long k_ulInvalidActionHandle = 0L;
    public static final long k_ulInvalidActionSetHandle = 0L;
    public static final long k_ulInvalidInputValueHandle = 0L;
    public static final int k_unControllerStateAxisCount = 5;
    public static final long k_ulOverlayHandleInvalid = 0L;
    public static final int k_unMaxDistortionFunctionParameters = 8;
    public static final int k_unScreenshotHandleInvalid = 0;
    public static final int k_unMaxApplicationKeyLength = 128;
    public static final int k_unVROverlayMaxKeyLength = 128;
    public static final int k_unVROverlayMaxNameLength = 128;
    public static final int k_unMaxOverlayCount = 64;
    public static final int k_unMaxOverlayIntersectionMaskPrimitivesCount = 32;
    public static final int k_unNotificationTextMaxSize = 256;
    public static final int k_unMaxSettingsKeyLength = 128;
    public static final int k_unMaxActionNameLength = 64;
    public static final int k_unMaxActionSetNameLength = 64;
    public static final int k_unMaxActionOriginCount = 16;
    public static final int k_unMaxBoneNameLength = 32;
    public static final long k_ulInvalidIOBufferHandle = 0L;
    public static final int k_ulInvalidSpatialAnchorHandle = 0;
    public static String IVRSystem_Version = "FnTable:IVRSystem_019";
    public static String IVRExtendedDisplay_Version = "FnTable:IVRExtendedDisplay_001";
    public static String IVRTrackedCamera_Version = "FnTable:IVRTrackedCamera_005";
    public static String IVRApplications_Version = "FnTable:IVRApplications_006";
    public static String IVRChaperone_Version = "FnTable:IVRChaperone_003";
    public static String IVRChaperoneSetup_Version = "FnTable:IVRChaperoneSetup_006";
    public static String IVRCompositor_Version = "FnTable:IVRCompositor_022";
    public static String IVRInput_Version = "FnTable:IVRInput_006";
    public static String IVRIOBuffer_Version = "FnTable:IVRIOBuffer_002";
    public static String IVROverlay_Version = "FnTable:IVROverlay_019";
    public static String IVRRenderModels_Version = "FnTable:IVRRenderModels_006";
    public static String IVRNotifications_Version = "FnTable:IVRNotifications_002";
    public static String IVRSettings_Version = "FnTable:IVRSettings_002";
    public static String IVRScreenshots_Version = "FnTable:IVRScreenshots_001";
    public static String IVRSpatialAnchors_Version = "FnTable:IVRSpatialAnchors_001";
    public static String IVRResources_Version = "FnTable:IVRResources_001";
    public static String IVRDriverManager_Version = "FnTable:IVRDriverManager_001";

    @Deprecated
    public static native Pointer VR_InitInternal(IntByReference var0, int var1);

    public static native Pointer VR_InitInternal(IntBuffer var0, int var1);

    public static native void VR_ShutdownInternal();

    public static native byte VR_IsHmdPresent();

    @Deprecated
    public static native Pointer VR_GetGenericInterface(Pointer var0, IntByReference var1);

    public static native Pointer VR_GetGenericInterface(String var0, IntBuffer var1);

    public static native byte VR_IsRuntimeInstalled();

    public static native Pointer VR_GetVRInitErrorAsSymbol(int var0);

    public static native Pointer VR_GetVRInitErrorAsEnglishDescription(int var0);

    static
    {
        Native.register(JOpenVRLibrary.class, JNA_NATIVE_LIB);
    }

    public interface ChaperoneCalibrationState
    {
        int ChaperoneCalibrationState_OK = 1;
        int ChaperoneCalibrationState_Warning = 100;
        int ChaperoneCalibrationState_Warning_BaseStationMayHaveMoved = 101;
        int ChaperoneCalibrationState_Warning_BaseStationRemoved = 102;
        int ChaperoneCalibrationState_Warning_SeatedBoundsInvalid = 103;
        int ChaperoneCalibrationState_Error = 200;
        int ChaperoneCalibrationState_Error_BaseStationUninitialized = 201;
        int ChaperoneCalibrationState_Error_BaseStationConflict = 202;
        int ChaperoneCalibrationState_Error_PlayAreaInvalid = 203;
        int ChaperoneCalibrationState_Error_CollisionBoundsInvalid = 204;
    }

    public interface EAdditionalRadioFeatures
    {
        int EAdditionalRadioFeatures_AdditionalRadioFeatures_None = 0;
        int EAdditionalRadioFeatures_AdditionalRadioFeatures_HTCLinkBox = 1;
        int EAdditionalRadioFeatures_AdditionalRadioFeatures_InternalDongle = 2;
        int EAdditionalRadioFeatures_AdditionalRadioFeatures_ExternalDongle = 4;
    }

    public interface EChaperoneConfigFile
    {
        int EChaperoneConfigFile_Live = 1;
        int EChaperoneConfigFile_Temp = 2;
    }

    public interface EChaperoneImportFlags
    {
        int EChaperoneImportFlags_EChaperoneImport_BoundsOnly = 1;
    }

    public interface ECollisionBoundsStyle
    {
        int ECollisionBoundsStyle_COLLISION_BOUNDS_STYLE_BEGINNER = 0;
        int ECollisionBoundsStyle_COLLISION_BOUNDS_STYLE_INTERMEDIATE = 1;
        int ECollisionBoundsStyle_COLLISION_BOUNDS_STYLE_SQUARES = 2;
        int ECollisionBoundsStyle_COLLISION_BOUNDS_STYLE_ADVANCED = 3;
        int ECollisionBoundsStyle_COLLISION_BOUNDS_STYLE_NONE = 4;
        int ECollisionBoundsStyle_COLLISION_BOUNDS_STYLE_COUNT = 5;
    }

    public interface EColorSpace
    {
        int EColorSpace_ColorSpace_Auto = 0;
        int EColorSpace_ColorSpace_Gamma = 1;
        int EColorSpace_ColorSpace_Linear = 2;
    }

    public interface EDeviceActivityLevel
    {
        int EDeviceActivityLevel_k_EDeviceActivityLevel_Unknown = -1;
        int EDeviceActivityLevel_k_EDeviceActivityLevel_Idle = 0;
        int EDeviceActivityLevel_k_EDeviceActivityLevel_UserInteraction = 1;
        int EDeviceActivityLevel_k_EDeviceActivityLevel_UserInteraction_Timeout = 2;
        int EDeviceActivityLevel_k_EDeviceActivityLevel_Standby = 3;
    }

    public interface EDualAnalogWhich
    {
        int EDualAnalogWhich_k_EDualAnalog_Left = 0;
        int EDualAnalogWhich_k_EDualAnalog_Right = 1;
    }

    public interface EGamepadTextInputLineMode
    {
        int EGamepadTextInputLineMode_k_EGamepadTextInputLineModeSingleLine = 0;
        int EGamepadTextInputLineMode_k_EGamepadTextInputLineModeMultipleLines = 1;
    }

    public interface EGamepadTextInputMode
    {
        int EGamepadTextInputMode_k_EGamepadTextInputModeNormal = 0;
        int EGamepadTextInputMode_k_EGamepadTextInputModePassword = 1;
        int EGamepadTextInputMode_k_EGamepadTextInputModeSubmit = 2;
    }

    public interface EHDCPError
    {
        int EHDCPError_HDCPError_None = 0;
        int EHDCPError_HDCPError_LinkLost = 1;
        int EHDCPError_HDCPError_Tampered = 2;
        int EHDCPError_HDCPError_DeviceRevoked = 3;
        int EHDCPError_HDCPError_Unknown = 4;
    }

    public interface EHiddenAreaMeshType
    {
        int EHiddenAreaMeshType_k_eHiddenAreaMesh_Standard = 0;
        int EHiddenAreaMeshType_k_eHiddenAreaMesh_Inverse = 1;
        int EHiddenAreaMeshType_k_eHiddenAreaMesh_LineLoop = 2;
        int EHiddenAreaMeshType_k_eHiddenAreaMesh_Max = 3;
    }

    public interface EIOBufferError
    {
        int EIOBufferError_IOBuffer_Success = 0;
        int EIOBufferError_IOBuffer_OperationFailed = 100;
        int EIOBufferError_IOBuffer_InvalidHandle = 101;
        int EIOBufferError_IOBuffer_InvalidArgument = 102;
        int EIOBufferError_IOBuffer_PathExists = 103;
        int EIOBufferError_IOBuffer_PathDoesNotExist = 104;
        int EIOBufferError_IOBuffer_Permission = 105;
    }

    public interface EIOBufferMode
    {
        int EIOBufferMode_IOBufferMode_Read = 1;
        int EIOBufferMode_IOBufferMode_Write = 2;
        int EIOBufferMode_IOBufferMode_Create = 512;
    }

    public interface EOverlayDirection
    {
        int EOverlayDirection_OverlayDirection_Up = 0;
        int EOverlayDirection_OverlayDirection_Down = 1;
        int EOverlayDirection_OverlayDirection_Left = 2;
        int EOverlayDirection_OverlayDirection_Right = 3;
        int EOverlayDirection_OverlayDirection_Count = 4;
    }

    public interface EShowUIType
    {
        int EShowUIType_ShowUI_ControllerBinding = 0;
        int EShowUIType_ShowUI_ManageTrackers = 1;
        int EShowUIType_ShowUI_Pairing = 3;
        int EShowUIType_ShowUI_Settings = 4;
    }

    public interface ETextureType
    {
        int ETextureType_TextureType_Invalid = -1;
        int ETextureType_TextureType_DirectX = 0;
        int ETextureType_TextureType_OpenGL = 1;
        int ETextureType_TextureType_Vulkan = 2;
        int ETextureType_TextureType_IOSurface = 3;
        int ETextureType_TextureType_DirectX12 = 4;
        int ETextureType_TextureType_DXGISharedHandle = 5;
        int ETextureType_TextureType_Metal = 6;
    }

    public interface ETrackedControllerRole
    {
        int ETrackedControllerRole_TrackedControllerRole_Invalid = 0;
        int ETrackedControllerRole_TrackedControllerRole_LeftHand = 1;
        int ETrackedControllerRole_TrackedControllerRole_RightHand = 2;
        int ETrackedControllerRole_TrackedControllerRole_OptOut = 3;
        int ETrackedControllerRole_TrackedControllerRole_Treadmill = 4;
        int ETrackedControllerRole_TrackedControllerRole_Max = 5;
    }

    public interface ETrackedDeviceClass
    {
        int ETrackedDeviceClass_TrackedDeviceClass_Invalid = 0;
        int ETrackedDeviceClass_TrackedDeviceClass_HMD = 1;
        int ETrackedDeviceClass_TrackedDeviceClass_Controller = 2;
        int ETrackedDeviceClass_TrackedDeviceClass_GenericTracker = 3;
        int ETrackedDeviceClass_TrackedDeviceClass_TrackingReference = 4;
        int ETrackedDeviceClass_TrackedDeviceClass_DisplayRedirect = 5;
        int ETrackedDeviceClass_TrackedDeviceClass_Max = 6;
    }

    public interface ETrackedDeviceProperty
    {
        int ETrackedDeviceProperty_Prop_Invalid = 0;
        int ETrackedDeviceProperty_Prop_TrackingSystemName_String = 1000;
        int ETrackedDeviceProperty_Prop_ModelNumber_String = 1001;
        int ETrackedDeviceProperty_Prop_SerialNumber_String = 1002;
        int ETrackedDeviceProperty_Prop_RenderModelName_String = 1003;
        int ETrackedDeviceProperty_Prop_WillDriftInYaw_Bool = 1004;
        int ETrackedDeviceProperty_Prop_ManufacturerName_String = 1005;
        int ETrackedDeviceProperty_Prop_TrackingFirmwareVersion_String = 1006;
        int ETrackedDeviceProperty_Prop_HardwareRevision_String = 1007;
        int ETrackedDeviceProperty_Prop_AllWirelessDongleDescriptions_String = 1008;
        int ETrackedDeviceProperty_Prop_ConnectedWirelessDongle_String = 1009;
        int ETrackedDeviceProperty_Prop_DeviceIsWireless_Bool = 1010;
        int ETrackedDeviceProperty_Prop_DeviceIsCharging_Bool = 1011;
        int ETrackedDeviceProperty_Prop_DeviceBatteryPercentage_Float = 1012;
        int ETrackedDeviceProperty_Prop_StatusDisplayTransform_Matrix34 = 1013;
        int ETrackedDeviceProperty_Prop_Firmware_UpdateAvailable_Bool = 1014;
        int ETrackedDeviceProperty_Prop_Firmware_ManualUpdate_Bool = 1015;
        int ETrackedDeviceProperty_Prop_Firmware_ManualUpdateURL_String = 1016;
        int ETrackedDeviceProperty_Prop_HardwareRevision_Uint64 = 1017;
        int ETrackedDeviceProperty_Prop_FirmwareVersion_Uint64 = 1018;
        int ETrackedDeviceProperty_Prop_FPGAVersion_Uint64 = 1019;
        int ETrackedDeviceProperty_Prop_VRCVersion_Uint64 = 1020;
        int ETrackedDeviceProperty_Prop_RadioVersion_Uint64 = 1021;
        int ETrackedDeviceProperty_Prop_DongleVersion_Uint64 = 1022;
        int ETrackedDeviceProperty_Prop_BlockServerShutdown_Bool = 1023;
        int ETrackedDeviceProperty_Prop_CanUnifyCoordinateSystemWithHmd_Bool = 1024;
        int ETrackedDeviceProperty_Prop_ContainsProximitySensor_Bool = 1025;
        int ETrackedDeviceProperty_Prop_DeviceProvidesBatteryStatus_Bool = 1026;
        int ETrackedDeviceProperty_Prop_DeviceCanPowerOff_Bool = 1027;
        int ETrackedDeviceProperty_Prop_Firmware_ProgrammingTarget_String = 1028;
        int ETrackedDeviceProperty_Prop_DeviceClass_Int32 = 1029;
        int ETrackedDeviceProperty_Prop_HasCamera_Bool = 1030;
        int ETrackedDeviceProperty_Prop_DriverVersion_String = 1031;
        int ETrackedDeviceProperty_Prop_Firmware_ForceUpdateRequired_Bool = 1032;
        int ETrackedDeviceProperty_Prop_ViveSystemButtonFixRequired_Bool = 1033;
        int ETrackedDeviceProperty_Prop_ParentDriver_Uint64 = 1034;
        int ETrackedDeviceProperty_Prop_ResourceRoot_String = 1035;
        int ETrackedDeviceProperty_Prop_RegisteredDeviceType_String = 1036;
        int ETrackedDeviceProperty_Prop_InputProfilePath_String = 1037;
        int ETrackedDeviceProperty_Prop_NeverTracked_Bool = 1038;
        int ETrackedDeviceProperty_Prop_NumCameras_Int32 = 1039;
        int ETrackedDeviceProperty_Prop_CameraFrameLayout_Int32 = 1040;
        int ETrackedDeviceProperty_Prop_CameraStreamFormat_Int32 = 1041;
        int ETrackedDeviceProperty_Prop_AdditionalDeviceSettingsPath_String = 1042;
        int ETrackedDeviceProperty_Prop_Identifiable_Bool = 1043;
        int ETrackedDeviceProperty_Prop_BootloaderVersion_Uint64 = 1044;
        int ETrackedDeviceProperty_Prop_AdditionalSystemReportData_String = 1045;
        int ETrackedDeviceProperty_Prop_CompositeFirmwareVersion_String = 1046;
        int ETrackedDeviceProperty_Prop_ReportsTimeSinceVSync_Bool = 2000;
        int ETrackedDeviceProperty_Prop_SecondsFromVsyncToPhotons_Float = 2001;
        int ETrackedDeviceProperty_Prop_DisplayFrequency_Float = 2002;
        int ETrackedDeviceProperty_Prop_UserIpdMeters_Float = 2003;
        int ETrackedDeviceProperty_Prop_CurrentUniverseId_Uint64 = 2004;
        int ETrackedDeviceProperty_Prop_PreviousUniverseId_Uint64 = 2005;
        int ETrackedDeviceProperty_Prop_DisplayFirmwareVersion_Uint64 = 2006;
        int ETrackedDeviceProperty_Prop_IsOnDesktop_Bool = 2007;
        int ETrackedDeviceProperty_Prop_DisplayMCType_Int32 = 2008;
        int ETrackedDeviceProperty_Prop_DisplayMCOffset_Float = 2009;
        int ETrackedDeviceProperty_Prop_DisplayMCScale_Float = 2010;
        int ETrackedDeviceProperty_Prop_EdidVendorID_Int32 = 2011;
        int ETrackedDeviceProperty_Prop_DisplayMCImageLeft_String = 2012;
        int ETrackedDeviceProperty_Prop_DisplayMCImageRight_String = 2013;
        int ETrackedDeviceProperty_Prop_DisplayGCBlackClamp_Float = 2014;
        int ETrackedDeviceProperty_Prop_EdidProductID_Int32 = 2015;
        int ETrackedDeviceProperty_Prop_CameraToHeadTransform_Matrix34 = 2016;
        int ETrackedDeviceProperty_Prop_DisplayGCType_Int32 = 2017;
        int ETrackedDeviceProperty_Prop_DisplayGCOffset_Float = 2018;
        int ETrackedDeviceProperty_Prop_DisplayGCScale_Float = 2019;
        int ETrackedDeviceProperty_Prop_DisplayGCPrescale_Float = 2020;
        int ETrackedDeviceProperty_Prop_DisplayGCImage_String = 2021;
        int ETrackedDeviceProperty_Prop_LensCenterLeftU_Float = 2022;
        int ETrackedDeviceProperty_Prop_LensCenterLeftV_Float = 2023;
        int ETrackedDeviceProperty_Prop_LensCenterRightU_Float = 2024;
        int ETrackedDeviceProperty_Prop_LensCenterRightV_Float = 2025;
        int ETrackedDeviceProperty_Prop_UserHeadToEyeDepthMeters_Float = 2026;
        int ETrackedDeviceProperty_Prop_CameraFirmwareVersion_Uint64 = 2027;
        int ETrackedDeviceProperty_Prop_CameraFirmwareDescription_String = 2028;
        int ETrackedDeviceProperty_Prop_DisplayFPGAVersion_Uint64 = 2029;
        int ETrackedDeviceProperty_Prop_DisplayBootloaderVersion_Uint64 = 2030;
        int ETrackedDeviceProperty_Prop_DisplayHardwareVersion_Uint64 = 2031;
        int ETrackedDeviceProperty_Prop_AudioFirmwareVersion_Uint64 = 2032;
        int ETrackedDeviceProperty_Prop_CameraCompatibilityMode_Int32 = 2033;
        int ETrackedDeviceProperty_Prop_ScreenshotHorizontalFieldOfViewDegrees_Float = 2034;
        int ETrackedDeviceProperty_Prop_ScreenshotVerticalFieldOfViewDegrees_Float = 2035;
        int ETrackedDeviceProperty_Prop_DisplaySuppressed_Bool = 2036;
        int ETrackedDeviceProperty_Prop_DisplayAllowNightMode_Bool = 2037;
        int ETrackedDeviceProperty_Prop_DisplayMCImageWidth_Int32 = 2038;
        int ETrackedDeviceProperty_Prop_DisplayMCImageHeight_Int32 = 2039;
        int ETrackedDeviceProperty_Prop_DisplayMCImageNumChannels_Int32 = 2040;
        int ETrackedDeviceProperty_Prop_DisplayMCImageData_Binary = 2041;
        int ETrackedDeviceProperty_Prop_SecondsFromPhotonsToVblank_Float = 2042;
        int ETrackedDeviceProperty_Prop_DriverDirectModeSendsVsyncEvents_Bool = 2043;
        int ETrackedDeviceProperty_Prop_DisplayDebugMode_Bool = 2044;
        int ETrackedDeviceProperty_Prop_GraphicsAdapterLuid_Uint64 = 2045;
        int ETrackedDeviceProperty_Prop_DriverProvidedChaperonePath_String = 2048;
        int ETrackedDeviceProperty_Prop_ExpectedTrackingReferenceCount_Int32 = 2049;
        int ETrackedDeviceProperty_Prop_ExpectedControllerCount_Int32 = 2050;
        int ETrackedDeviceProperty_Prop_NamedIconPathControllerLeftDeviceOff_String = 2051;
        int ETrackedDeviceProperty_Prop_NamedIconPathControllerRightDeviceOff_String = 2052;
        int ETrackedDeviceProperty_Prop_NamedIconPathTrackingReferenceDeviceOff_String = 2053;
        int ETrackedDeviceProperty_Prop_DoNotApplyPrediction_Bool = 2054;
        int ETrackedDeviceProperty_Prop_CameraToHeadTransforms_Matrix34_Array = 2055;
        int ETrackedDeviceProperty_Prop_DistortionMeshResolution_Int32 = 2056;
        int ETrackedDeviceProperty_Prop_DriverIsDrawingControllers_Bool = 2057;
        int ETrackedDeviceProperty_Prop_DriverRequestsApplicationPause_Bool = 2058;
        int ETrackedDeviceProperty_Prop_DriverRequestsReducedRendering_Bool = 2059;
        int ETrackedDeviceProperty_Prop_MinimumIpdStepMeters_Float = 2060;
        int ETrackedDeviceProperty_Prop_AudioBridgeFirmwareVersion_Uint64 = 2061;
        int ETrackedDeviceProperty_Prop_ImageBridgeFirmwareVersion_Uint64 = 2062;
        int ETrackedDeviceProperty_Prop_ImuToHeadTransform_Matrix34 = 2063;
        int ETrackedDeviceProperty_Prop_ImuFactoryGyroBias_Vector3 = 2064;
        int ETrackedDeviceProperty_Prop_ImuFactoryGyroScale_Vector3 = 2065;
        int ETrackedDeviceProperty_Prop_ImuFactoryAccelerometerBias_Vector3 = 2066;
        int ETrackedDeviceProperty_Prop_ImuFactoryAccelerometerScale_Vector3 = 2067;
        int ETrackedDeviceProperty_Prop_ConfigurationIncludesLighthouse20Features_Bool = 2069;
        int ETrackedDeviceProperty_Prop_AdditionalRadioFeatures_Uint64 = 2070;
        int ETrackedDeviceProperty_Prop_CameraWhiteBalance_Vector4_Array = 2071;
        int ETrackedDeviceProperty_Prop_CameraDistortionFunction_Int32_Array = 2072;
        int ETrackedDeviceProperty_Prop_CameraDistortionCoefficients_Float_Array = 2073;
        int ETrackedDeviceProperty_Prop_ExpectedControllerType_String = 2074;
        int ETrackedDeviceProperty_Prop_DisplayAvailableFrameRates_Float_Array = 2080;
        int ETrackedDeviceProperty_Prop_DisplaySupportsMultipleFramerates_Bool = 2081;
        int ETrackedDeviceProperty_Prop_DashboardLayoutPathName_String = 2090;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraCorrectionMode_Int32 = 2200;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_InnerLeft_Int32 = 2201;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_InnerRight_Int32 = 2202;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_InnerTop_Int32 = 2203;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_InnerBottom_Int32 = 2204;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_OuterLeft_Int32 = 2205;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_OuterRight_Int32 = 2206;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_OuterTop_Int32 = 2207;
        int ETrackedDeviceProperty_Prop_DriverRequestedMuraFeather_OuterBottom_Int32 = 2208;
        int ETrackedDeviceProperty_Prop_AttachedDeviceId_String = 3000;
        int ETrackedDeviceProperty_Prop_SupportedButtons_Uint64 = 3001;
        int ETrackedDeviceProperty_Prop_Axis0Type_Int32 = 3002;
        int ETrackedDeviceProperty_Prop_Axis1Type_Int32 = 3003;
        int ETrackedDeviceProperty_Prop_Axis2Type_Int32 = 3004;
        int ETrackedDeviceProperty_Prop_Axis3Type_Int32 = 3005;
        int ETrackedDeviceProperty_Prop_Axis4Type_Int32 = 3006;
        int ETrackedDeviceProperty_Prop_ControllerRoleHint_Int32 = 3007;
        int ETrackedDeviceProperty_Prop_FieldOfViewLeftDegrees_Float = 4000;
        int ETrackedDeviceProperty_Prop_FieldOfViewRightDegrees_Float = 4001;
        int ETrackedDeviceProperty_Prop_FieldOfViewTopDegrees_Float = 4002;
        int ETrackedDeviceProperty_Prop_FieldOfViewBottomDegrees_Float = 4003;
        int ETrackedDeviceProperty_Prop_TrackingRangeMinimumMeters_Float = 4004;
        int ETrackedDeviceProperty_Prop_TrackingRangeMaximumMeters_Float = 4005;
        int ETrackedDeviceProperty_Prop_ModeLabel_String = 4006;
        int ETrackedDeviceProperty_Prop_CanWirelessIdentify_Bool = 4007;
        int ETrackedDeviceProperty_Prop_Nonce_Int32 = 4008;
        int ETrackedDeviceProperty_Prop_IconPathName_String = 5000;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceOff_String = 5001;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceSearching_String = 5002;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceSearchingAlert_String = 5003;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceReady_String = 5004;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceReadyAlert_String = 5005;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceNotReady_String = 5006;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceStandby_String = 5007;
        int ETrackedDeviceProperty_Prop_NamedIconPathDeviceAlertLow_String = 5008;
        int ETrackedDeviceProperty_Prop_DisplayHiddenArea_Binary_Start = 5100;
        int ETrackedDeviceProperty_Prop_DisplayHiddenArea_Binary_End = 5150;
        int ETrackedDeviceProperty_Prop_ParentContainer = 5151;
        int ETrackedDeviceProperty_Prop_UserConfigPath_String = 6000;
        int ETrackedDeviceProperty_Prop_InstallPath_String = 6001;
        int ETrackedDeviceProperty_Prop_HasDisplayComponent_Bool = 6002;
        int ETrackedDeviceProperty_Prop_HasControllerComponent_Bool = 6003;
        int ETrackedDeviceProperty_Prop_HasCameraComponent_Bool = 6004;
        int ETrackedDeviceProperty_Prop_HasDriverDirectModeComponent_Bool = 6005;
        int ETrackedDeviceProperty_Prop_HasVirtualDisplayComponent_Bool = 6006;
        int ETrackedDeviceProperty_Prop_HasSpatialAnchorsSupport_Bool = 6007;
        int ETrackedDeviceProperty_Prop_ControllerType_String = 7000;
        int ETrackedDeviceProperty_Prop_ControllerHandSelectionPriority_Int32 = 7002;
        int ETrackedDeviceProperty_Prop_VendorSpecific_Reserved_Start = 10000;
        int ETrackedDeviceProperty_Prop_VendorSpecific_Reserved_End = 10999;
        int ETrackedDeviceProperty_Prop_TrackedDeviceProperty_Max = 1000000;
    }

    public interface ETrackedPropertyError
    {
        int ETrackedPropertyError_TrackedProp_Success = 0;
        int ETrackedPropertyError_TrackedProp_WrongDataType = 1;
        int ETrackedPropertyError_TrackedProp_WrongDeviceClass = 2;
        int ETrackedPropertyError_TrackedProp_BufferTooSmall = 3;
        int ETrackedPropertyError_TrackedProp_UnknownProperty = 4;
        int ETrackedPropertyError_TrackedProp_InvalidDevice = 5;
        int ETrackedPropertyError_TrackedProp_CouldNotContactServer = 6;
        int ETrackedPropertyError_TrackedProp_ValueNotProvidedByDevice = 7;
        int ETrackedPropertyError_TrackedProp_StringExceedsMaximumLength = 8;
        int ETrackedPropertyError_TrackedProp_NotYetAvailable = 9;
        int ETrackedPropertyError_TrackedProp_PermissionDenied = 10;
        int ETrackedPropertyError_TrackedProp_InvalidOperation = 11;
        int ETrackedPropertyError_TrackedProp_CannotWriteToWildcards = 12;
        int ETrackedPropertyError_TrackedProp_IPCReadFailure = 13;
    }

    public interface ETrackingResult
    {
        int ETrackingResult_TrackingResult_Uninitialized = 1;
        int ETrackingResult_TrackingResult_Calibrating_InProgress = 100;
        int ETrackingResult_TrackingResult_Calibrating_OutOfRange = 101;
        int ETrackingResult_TrackingResult_Running_OK = 200;
        int ETrackingResult_TrackingResult_Running_OutOfRange = 201;
        int ETrackingResult_TrackingResult_Fallback_RotationOnly = 300;
    }

    public interface ETrackingUniverseOrigin
    {
        int ETrackingUniverseOrigin_TrackingUniverseSeated = 0;
        int ETrackingUniverseOrigin_TrackingUniverseStanding = 1;
        int ETrackingUniverseOrigin_TrackingUniverseRawAndUncalibrated = 2;
    }

    public interface EVRApplicationError
    {
        int EVRApplicationError_VRApplicationError_None = 0;
        int EVRApplicationError_VRApplicationError_AppKeyAlreadyExists = 100;
        int EVRApplicationError_VRApplicationError_NoManifest = 101;
        int EVRApplicationError_VRApplicationError_NoApplication = 102;
        int EVRApplicationError_VRApplicationError_InvalidIndex = 103;
        int EVRApplicationError_VRApplicationError_UnknownApplication = 104;
        int EVRApplicationError_VRApplicationError_IPCFailed = 105;
        int EVRApplicationError_VRApplicationError_ApplicationAlreadyRunning = 106;
        int EVRApplicationError_VRApplicationError_InvalidManifest = 107;
        int EVRApplicationError_VRApplicationError_InvalidApplication = 108;
        int EVRApplicationError_VRApplicationError_LaunchFailed = 109;
        int EVRApplicationError_VRApplicationError_ApplicationAlreadyStarting = 110;
        int EVRApplicationError_VRApplicationError_LaunchInProgress = 111;
        int EVRApplicationError_VRApplicationError_OldApplicationQuitting = 112;
        int EVRApplicationError_VRApplicationError_TransitionAborted = 113;
        int EVRApplicationError_VRApplicationError_IsTemplate = 114;
        int EVRApplicationError_VRApplicationError_SteamVRIsExiting = 115;
        int EVRApplicationError_VRApplicationError_BufferTooSmall = 200;
        int EVRApplicationError_VRApplicationError_PropertyNotSet = 201;
        int EVRApplicationError_VRApplicationError_UnknownProperty = 202;
        int EVRApplicationError_VRApplicationError_InvalidParameter = 203;
    }

    public interface EVRApplicationProperty
    {
        int EVRApplicationProperty_VRApplicationProperty_Name_String = 0;
        int EVRApplicationProperty_VRApplicationProperty_LaunchType_String = 11;
        int EVRApplicationProperty_VRApplicationProperty_WorkingDirectory_String = 12;
        int EVRApplicationProperty_VRApplicationProperty_BinaryPath_String = 13;
        int EVRApplicationProperty_VRApplicationProperty_Arguments_String = 14;
        int EVRApplicationProperty_VRApplicationProperty_URL_String = 15;
        int EVRApplicationProperty_VRApplicationProperty_Description_String = 50;
        int EVRApplicationProperty_VRApplicationProperty_NewsURL_String = 51;
        int EVRApplicationProperty_VRApplicationProperty_ImagePath_String = 52;
        int EVRApplicationProperty_VRApplicationProperty_Source_String = 53;
        int EVRApplicationProperty_VRApplicationProperty_ActionManifestURL_String = 54;
        int EVRApplicationProperty_VRApplicationProperty_IsDashboardOverlay_Bool = 60;
        int EVRApplicationProperty_VRApplicationProperty_IsTemplate_Bool = 61;
        int EVRApplicationProperty_VRApplicationProperty_IsInstanced_Bool = 62;
        int EVRApplicationProperty_VRApplicationProperty_IsInternal_Bool = 63;
        int EVRApplicationProperty_VRApplicationProperty_WantsCompositorPauseInStandby_Bool = 64;
        int EVRApplicationProperty_VRApplicationProperty_LastLaunchTime_Uint64 = 70;
    }

    public interface EVRApplicationTransitionState
    {
        int EVRApplicationTransitionState_VRApplicationTransition_None = 0;
        int EVRApplicationTransitionState_VRApplicationTransition_OldAppQuitSent = 10;
        int EVRApplicationTransitionState_VRApplicationTransition_WaitingForExternalLaunch = 11;
        int EVRApplicationTransitionState_VRApplicationTransition_NewAppLaunched = 20;
    }

    public interface EVRApplicationType
    {
        int EVRApplicationType_VRApplication_Other = 0;
        int EVRApplicationType_VRApplication_Scene = 1;
        int EVRApplicationType_VRApplication_Overlay = 2;
        int EVRApplicationType_VRApplication_Background = 3;
        int EVRApplicationType_VRApplication_Utility = 4;
        int EVRApplicationType_VRApplication_VRMonitor = 5;
        int EVRApplicationType_VRApplication_SteamWatchdog = 6;
        int EVRApplicationType_VRApplication_Bootstrapper = 7;
        int EVRApplicationType_VRApplication_WebHelper = 8;
        int EVRApplicationType_VRApplication_Max = 9;
    }

    public interface EVRButtonId
    {
        int EVRButtonId_k_EButton_System = 0;
        int EVRButtonId_k_EButton_ApplicationMenu = 1;
        int EVRButtonId_k_EButton_Grip = 2;
        int EVRButtonId_k_EButton_DPad_Left = 3;
        int EVRButtonId_k_EButton_DPad_Up = 4;
        int EVRButtonId_k_EButton_DPad_Right = 5;
        int EVRButtonId_k_EButton_DPad_Down = 6;
        int EVRButtonId_k_EButton_A = 7;
        int EVRButtonId_k_EButton_ProximitySensor = 31;
        int EVRButtonId_k_EButton_Axis0 = 32;
        int EVRButtonId_k_EButton_Axis1 = 33;
        int EVRButtonId_k_EButton_Axis2 = 34;
        int EVRButtonId_k_EButton_Axis3 = 35;
        int EVRButtonId_k_EButton_Axis4 = 36;
        int EVRButtonId_k_EButton_SteamVR_Touchpad = 32;
        int EVRButtonId_k_EButton_SteamVR_Trigger = 33;
        int EVRButtonId_k_EButton_Dashboard_Back = 2;
        int EVRButtonId_k_EButton_IndexController_A = 2;
        int EVRButtonId_k_EButton_IndexController_B = 1;
        int EVRButtonId_k_EButton_IndexController_JoyStick = 35;
        int EVRButtonId_k_EButton_Max = 64;
    }

    public interface EVRComponentProperty
    {
        int EVRComponentProperty_VRComponentProperty_IsStatic = 1;
        int EVRComponentProperty_VRComponentProperty_IsVisible = 2;
        int EVRComponentProperty_VRComponentProperty_IsTouched = 4;
        int EVRComponentProperty_VRComponentProperty_IsPressed = 8;
        int EVRComponentProperty_VRComponentProperty_IsScrolled = 16;
    }

    public interface EVRCompositorError
    {
        int EVRCompositorError_VRCompositorError_None = 0;
        int EVRCompositorError_VRCompositorError_RequestFailed = 1;
        int EVRCompositorError_VRCompositorError_IncompatibleVersion = 100;
        int EVRCompositorError_VRCompositorError_DoNotHaveFocus = 101;
        int EVRCompositorError_VRCompositorError_InvalidTexture = 102;
        int EVRCompositorError_VRCompositorError_IsNotSceneApplication = 103;
        int EVRCompositorError_VRCompositorError_TextureIsOnWrongDevice = 104;
        int EVRCompositorError_VRCompositorError_TextureUsesUnsupportedFormat = 105;
        int EVRCompositorError_VRCompositorError_SharedTexturesNotSupported = 106;
        int EVRCompositorError_VRCompositorError_IndexOutOfRange = 107;
        int EVRCompositorError_VRCompositorError_AlreadySubmitted = 108;
        int EVRCompositorError_VRCompositorError_InvalidBounds = 109;
    }

    public interface EVRCompositorTimingMode
    {
        int EVRCompositorTimingMode_VRCompositorTimingMode_Implicit = 0;
        int EVRCompositorTimingMode_VRCompositorTimingMode_Explicit_RuntimePerformsPostPresentHandoff = 1;
        int EVRCompositorTimingMode_VRCompositorTimingMode_Explicit_ApplicationPerformsPostPresentHandoff = 2;
    }

    public interface EVRControllerAxisType
    {
        int EVRControllerAxisType_k_eControllerAxis_None = 0;
        int EVRControllerAxisType_k_eControllerAxis_TrackPad = 1;
        int EVRControllerAxisType_k_eControllerAxis_Joystick = 2;
        int EVRControllerAxisType_k_eControllerAxis_Trigger = 3;
    }

    public interface EVRControllerEventOutputType
    {
        int EVRControllerEventOutputType_ControllerEventOutput_OSEvents = 0;
        int EVRControllerEventOutputType_ControllerEventOutput_VREvents = 1;
    }

    public interface EVRDistortionFunctionType
    {
        int EVRDistortionFunctionType_VRDistortionFunctionType_None = 0;
        int EVRDistortionFunctionType_VRDistortionFunctionType_FTheta = 1;
        int EVRDistortionFunctionType_VRDistortionFunctionType_Extended_FTheta = 2;
        int EVRDistortionFunctionType_MAX_DISTORTION_FUNCTION_TYPES = 3;
    }

    public interface EVREventType
    {
        int EVREventType_VREvent_None = 0;
        int EVREventType_VREvent_TrackedDeviceActivated = 100;
        int EVREventType_VREvent_TrackedDeviceDeactivated = 101;
        int EVREventType_VREvent_TrackedDeviceUpdated = 102;
        int EVREventType_VREvent_TrackedDeviceUserInteractionStarted = 103;
        int EVREventType_VREvent_TrackedDeviceUserInteractionEnded = 104;
        int EVREventType_VREvent_IpdChanged = 105;
        int EVREventType_VREvent_EnterStandbyMode = 106;
        int EVREventType_VREvent_LeaveStandbyMode = 107;
        int EVREventType_VREvent_TrackedDeviceRoleChanged = 108;
        int EVREventType_VREvent_WatchdogWakeUpRequested = 109;
        int EVREventType_VREvent_LensDistortionChanged = 110;
        int EVREventType_VREvent_PropertyChanged = 111;
        int EVREventType_VREvent_WirelessDisconnect = 112;
        int EVREventType_VREvent_WirelessReconnect = 113;
        int EVREventType_VREvent_ButtonPress = 200;
        int EVREventType_VREvent_ButtonUnpress = 201;
        int EVREventType_VREvent_ButtonTouch = 202;
        int EVREventType_VREvent_ButtonUntouch = 203;
        int EVREventType_VREvent_DualAnalog_Press = 250;
        int EVREventType_VREvent_DualAnalog_Unpress = 251;
        int EVREventType_VREvent_DualAnalog_Touch = 252;
        int EVREventType_VREvent_DualAnalog_Untouch = 253;
        int EVREventType_VREvent_DualAnalog_Move = 254;
        int EVREventType_VREvent_DualAnalog_ModeSwitch1 = 255;
        int EVREventType_VREvent_DualAnalog_ModeSwitch2 = 256;
        int EVREventType_VREvent_DualAnalog_Cancel = 257;
        int EVREventType_VREvent_MouseMove = 300;
        int EVREventType_VREvent_MouseButtonDown = 301;
        int EVREventType_VREvent_MouseButtonUp = 302;
        int EVREventType_VREvent_FocusEnter = 303;
        int EVREventType_VREvent_FocusLeave = 304;
        int EVREventType_VREvent_ScrollDiscrete = 305;
        int EVREventType_VREvent_TouchPadMove = 306;
        int EVREventType_VREvent_OverlayFocusChanged = 307;
        int EVREventType_VREvent_ReloadOverlays = 308;
        int EVREventType_VREvent_ScrollSmooth = 309;
        int EVREventType_VREvent_InputFocusCaptured = 400;
        int EVREventType_VREvent_InputFocusReleased = 401;
        int EVREventType_VREvent_SceneFocusLost = 402;
        int EVREventType_VREvent_SceneFocusGained = 403;
        int EVREventType_VREvent_SceneApplicationChanged = 404;
        int EVREventType_VREvent_SceneFocusChanged = 405;
        int EVREventType_VREvent_InputFocusChanged = 406;
        int EVREventType_VREvent_SceneApplicationSecondaryRenderingStarted = 407;
        int EVREventType_VREvent_SceneApplicationUsingWrongGraphicsAdapter = 408;
        int EVREventType_VREvent_ActionBindingReloaded = 409;
        int EVREventType_VREvent_HideRenderModels = 410;
        int EVREventType_VREvent_ShowRenderModels = 411;
        int EVREventType_VREvent_ConsoleOpened = 420;
        int EVREventType_VREvent_ConsoleClosed = 421;
        int EVREventType_VREvent_OverlayShown = 500;
        int EVREventType_VREvent_OverlayHidden = 501;
        int EVREventType_VREvent_DashboardActivated = 502;
        int EVREventType_VREvent_DashboardDeactivated = 503;
        int EVREventType_VREvent_DashboardRequested = 505;
        int EVREventType_VREvent_ResetDashboard = 506;
        int EVREventType_VREvent_RenderToast = 507;
        int EVREventType_VREvent_ImageLoaded = 508;
        int EVREventType_VREvent_ShowKeyboard = 509;
        int EVREventType_VREvent_HideKeyboard = 510;
        int EVREventType_VREvent_OverlayGamepadFocusGained = 511;
        int EVREventType_VREvent_OverlayGamepadFocusLost = 512;
        int EVREventType_VREvent_OverlaySharedTextureChanged = 513;
        int EVREventType_VREvent_ScreenshotTriggered = 516;
        int EVREventType_VREvent_ImageFailed = 517;
        int EVREventType_VREvent_DashboardOverlayCreated = 518;
        int EVREventType_VREvent_SwitchGamepadFocus = 519;
        int EVREventType_VREvent_RequestScreenshot = 520;
        int EVREventType_VREvent_ScreenshotTaken = 521;
        int EVREventType_VREvent_ScreenshotFailed = 522;
        int EVREventType_VREvent_SubmitScreenshotToDashboard = 523;
        int EVREventType_VREvent_ScreenshotProgressToDashboard = 524;
        int EVREventType_VREvent_PrimaryDashboardDeviceChanged = 525;
        int EVREventType_VREvent_RoomViewShown = 526;
        int EVREventType_VREvent_RoomViewHidden = 527;
        int EVREventType_VREvent_ShowUI = 528;
        int EVREventType_VREvent_ShowDevTools = 529;
        int EVREventType_VREvent_Notification_Shown = 600;
        int EVREventType_VREvent_Notification_Hidden = 601;
        int EVREventType_VREvent_Notification_BeginInteraction = 602;
        int EVREventType_VREvent_Notification_Destroyed = 603;
        int EVREventType_VREvent_Quit = 700;
        int EVREventType_VREvent_ProcessQuit = 701;
        int EVREventType_VREvent_QuitAborted_UserPrompt = 702;
        int EVREventType_VREvent_QuitAcknowledged = 703;
        int EVREventType_VREvent_DriverRequestedQuit = 704;
        int EVREventType_VREvent_RestartRequested = 705;
        int EVREventType_VREvent_ChaperoneDataHasChanged = 800;
        int EVREventType_VREvent_ChaperoneUniverseHasChanged = 801;
        int EVREventType_VREvent_ChaperoneTempDataHasChanged = 802;
        int EVREventType_VREvent_ChaperoneSettingsHaveChanged = 803;
        int EVREventType_VREvent_SeatedZeroPoseReset = 804;
        int EVREventType_VREvent_ChaperoneFlushCache = 805;
        int EVREventType_VREvent_ChaperoneRoomSetupStarting = 806;
        int EVREventType_VREvent_ChaperoneRoomSetupFinished = 807;
        int EVREventType_VREvent_AudioSettingsHaveChanged = 820;
        int EVREventType_VREvent_BackgroundSettingHasChanged = 850;
        int EVREventType_VREvent_CameraSettingsHaveChanged = 851;
        int EVREventType_VREvent_ReprojectionSettingHasChanged = 852;
        int EVREventType_VREvent_ModelSkinSettingsHaveChanged = 853;
        int EVREventType_VREvent_EnvironmentSettingsHaveChanged = 854;
        int EVREventType_VREvent_PowerSettingsHaveChanged = 855;
        int EVREventType_VREvent_EnableHomeAppSettingsHaveChanged = 856;
        int EVREventType_VREvent_SteamVRSectionSettingChanged = 857;
        int EVREventType_VREvent_LighthouseSectionSettingChanged = 858;
        int EVREventType_VREvent_NullSectionSettingChanged = 859;
        int EVREventType_VREvent_UserInterfaceSectionSettingChanged = 860;
        int EVREventType_VREvent_NotificationsSectionSettingChanged = 861;
        int EVREventType_VREvent_KeyboardSectionSettingChanged = 862;
        int EVREventType_VREvent_PerfSectionSettingChanged = 863;
        int EVREventType_VREvent_DashboardSectionSettingChanged = 864;
        int EVREventType_VREvent_WebInterfaceSectionSettingChanged = 865;
        int EVREventType_VREvent_TrackersSectionSettingChanged = 866;
        int EVREventType_VREvent_LastKnownSectionSettingChanged = 867;
        int EVREventType_VREvent_DismissedWarningsSectionSettingChanged = 868;
        int EVREventType_VREvent_StatusUpdate = 900;
        int EVREventType_VREvent_WebInterface_InstallDriverCompleted = 950;
        int EVREventType_VREvent_MCImageUpdated = 1000;
        int EVREventType_VREvent_FirmwareUpdateStarted = 1100;
        int EVREventType_VREvent_FirmwareUpdateFinished = 1101;
        int EVREventType_VREvent_KeyboardClosed = 1200;
        int EVREventType_VREvent_KeyboardCharInput = 1201;
        int EVREventType_VREvent_KeyboardDone = 1202;
        int EVREventType_VREvent_ApplicationTransitionStarted = 1300;
        int EVREventType_VREvent_ApplicationTransitionAborted = 1301;
        int EVREventType_VREvent_ApplicationTransitionNewAppStarted = 1302;
        int EVREventType_VREvent_ApplicationListUpdated = 1303;
        int EVREventType_VREvent_ApplicationMimeTypeLoad = 1304;
        int EVREventType_VREvent_ApplicationTransitionNewAppLaunchComplete = 1305;
        int EVREventType_VREvent_ProcessConnected = 1306;
        int EVREventType_VREvent_ProcessDisconnected = 1307;
        int EVREventType_VREvent_Compositor_MirrorWindowShown = 1400;
        int EVREventType_VREvent_Compositor_MirrorWindowHidden = 1401;
        int EVREventType_VREvent_Compositor_ChaperoneBoundsShown = 1410;
        int EVREventType_VREvent_Compositor_ChaperoneBoundsHidden = 1411;
        int EVREventType_VREvent_Compositor_DisplayDisconnected = 1412;
        int EVREventType_VREvent_Compositor_DisplayReconnected = 1413;
        int EVREventType_VREvent_Compositor_HDCPError = 1414;
        int EVREventType_VREvent_Compositor_ApplicationNotResponding = 1415;
        int EVREventType_VREvent_Compositor_ApplicationResumed = 1416;
        int EVREventType_VREvent_Compositor_OutOfVideoMemory = 1417;
        int EVREventType_VREvent_TrackedCamera_StartVideoStream = 1500;
        int EVREventType_VREvent_TrackedCamera_StopVideoStream = 1501;
        int EVREventType_VREvent_TrackedCamera_PauseVideoStream = 1502;
        int EVREventType_VREvent_TrackedCamera_ResumeVideoStream = 1503;
        int EVREventType_VREvent_TrackedCamera_EditingSurface = 1550;
        int EVREventType_VREvent_PerformanceTest_EnableCapture = 1600;
        int EVREventType_VREvent_PerformanceTest_DisableCapture = 1601;
        int EVREventType_VREvent_PerformanceTest_FidelityLevel = 1602;
        int EVREventType_VREvent_MessageOverlay_Closed = 1650;
        int EVREventType_VREvent_MessageOverlayCloseRequested = 1651;
        int EVREventType_VREvent_Input_HapticVibration = 1700;
        int EVREventType_VREvent_Input_BindingLoadFailed = 1701;
        int EVREventType_VREvent_Input_BindingLoadSuccessful = 1702;
        int EVREventType_VREvent_Input_ActionManifestReloaded = 1703;
        int EVREventType_VREvent_Input_ActionManifestLoadFailed = 1704;
        int EVREventType_VREvent_Input_ProgressUpdate = 1705;
        int EVREventType_VREvent_Input_TrackerActivated = 1706;
        int EVREventType_VREvent_Input_BindingsUpdated = 1707;
        int EVREventType_VREvent_SpatialAnchors_PoseUpdated = 1800;
        int EVREventType_VREvent_SpatialAnchors_DescriptorUpdated = 1801;
        int EVREventType_VREvent_SpatialAnchors_RequestPoseUpdate = 1802;
        int EVREventType_VREvent_SpatialAnchors_RequestDescriptorUpdate = 1803;
        int EVREventType_VREvent_SystemReport_Started = 1900;
        int EVREventType_VREvent_VendorSpecific_Reserved_Start = 10000;
        int EVREventType_VREvent_VendorSpecific_Reserved_End = 19999;
    }

    public interface EVREye
    {
        int EVREye_Eye_Left = 0;
        int EVREye_Eye_Right = 1;
    }

    public interface EVRFinger
    {
        int EVRFinger_VRFinger_Thumb = 0;
        int EVRFinger_VRFinger_Index = 1;
        int EVRFinger_VRFinger_Middle = 2;
        int EVRFinger_VRFinger_Ring = 3;
        int EVRFinger_VRFinger_Pinky = 4;
        int EVRFinger_VRFinger_Count = 5;
    }

    public interface EVRFingerSplay
    {
        int EVRFingerSplay_VRFingerSplay_Thumb_Index = 0;
        int EVRFingerSplay_VRFingerSplay_Index_Middle = 1;
        int EVRFingerSplay_VRFingerSplay_Middle_Ring = 2;
        int EVRFingerSplay_VRFingerSplay_Ring_Pinky = 3;
        int EVRFingerSplay_VRFingerSplay_Count = 4;
    }

    public interface EVRFirmwareError
    {
        int EVRFirmwareError_VRFirmwareError_None = 0;
        int EVRFirmwareError_VRFirmwareError_Success = 1;
        int EVRFirmwareError_VRFirmwareError_Fail = 2;
    }

    public interface EVRInitError
    {
        int EVRInitError_VRInitError_None = 0;
        int EVRInitError_VRInitError_Unknown = 1;
        int EVRInitError_VRInitError_Init_InstallationNotFound = 100;
        int EVRInitError_VRInitError_Init_InstallationCorrupt = 101;
        int EVRInitError_VRInitError_Init_VRClientDLLNotFound = 102;
        int EVRInitError_VRInitError_Init_FileNotFound = 103;
        int EVRInitError_VRInitError_Init_FactoryNotFound = 104;
        int EVRInitError_VRInitError_Init_InterfaceNotFound = 105;
        int EVRInitError_VRInitError_Init_InvalidInterface = 106;
        int EVRInitError_VRInitError_Init_UserConfigDirectoryInvalid = 107;
        int EVRInitError_VRInitError_Init_HmdNotFound = 108;
        int EVRInitError_VRInitError_Init_NotInitialized = 109;
        int EVRInitError_VRInitError_Init_PathRegistryNotFound = 110;
        int EVRInitError_VRInitError_Init_NoConfigPath = 111;
        int EVRInitError_VRInitError_Init_NoLogPath = 112;
        int EVRInitError_VRInitError_Init_PathRegistryNotWritable = 113;
        int EVRInitError_VRInitError_Init_AppInfoInitFailed = 114;
        int EVRInitError_VRInitError_Init_Retry = 115;
        int EVRInitError_VRInitError_Init_InitCanceledByUser = 116;
        int EVRInitError_VRInitError_Init_AnotherAppLaunching = 117;
        int EVRInitError_VRInitError_Init_SettingsInitFailed = 118;
        int EVRInitError_VRInitError_Init_ShuttingDown = 119;
        int EVRInitError_VRInitError_Init_TooManyObjects = 120;
        int EVRInitError_VRInitError_Init_NoServerForBackgroundApp = 121;
        int EVRInitError_VRInitError_Init_NotSupportedWithCompositor = 122;
        int EVRInitError_VRInitError_Init_NotAvailableToUtilityApps = 123;
        int EVRInitError_VRInitError_Init_Internal = 124;
        int EVRInitError_VRInitError_Init_HmdDriverIdIsNone = 125;
        int EVRInitError_VRInitError_Init_HmdNotFoundPresenceFailed = 126;
        int EVRInitError_VRInitError_Init_VRMonitorNotFound = 127;
        int EVRInitError_VRInitError_Init_VRMonitorStartupFailed = 128;
        int EVRInitError_VRInitError_Init_LowPowerWatchdogNotSupported = 129;
        int EVRInitError_VRInitError_Init_InvalidApplicationType = 130;
        int EVRInitError_VRInitError_Init_NotAvailableToWatchdogApps = 131;
        int EVRInitError_VRInitError_Init_WatchdogDisabledInSettings = 132;
        int EVRInitError_VRInitError_Init_VRDashboardNotFound = 133;
        int EVRInitError_VRInitError_Init_VRDashboardStartupFailed = 134;
        int EVRInitError_VRInitError_Init_VRHomeNotFound = 135;
        int EVRInitError_VRInitError_Init_VRHomeStartupFailed = 136;
        int EVRInitError_VRInitError_Init_RebootingBusy = 137;
        int EVRInitError_VRInitError_Init_FirmwareUpdateBusy = 138;
        int EVRInitError_VRInitError_Init_FirmwareRecoveryBusy = 139;
        int EVRInitError_VRInitError_Init_USBServiceBusy = 140;
        int EVRInitError_VRInitError_Init_VRWebHelperStartupFailed = 141;
        int EVRInitError_VRInitError_Init_TrackerManagerInitFailed = 142;
        int EVRInitError_VRInitError_Init_AlreadyRunning = 143;
        int EVRInitError_VRInitError_Init_FailedForVrMonitor = 144;
        int EVRInitError_VRInitError_Driver_Failed = 200;
        int EVRInitError_VRInitError_Driver_Unknown = 201;
        int EVRInitError_VRInitError_Driver_HmdUnknown = 202;
        int EVRInitError_VRInitError_Driver_NotLoaded = 203;
        int EVRInitError_VRInitError_Driver_RuntimeOutOfDate = 204;
        int EVRInitError_VRInitError_Driver_HmdInUse = 205;
        int EVRInitError_VRInitError_Driver_NotCalibrated = 206;
        int EVRInitError_VRInitError_Driver_CalibrationInvalid = 207;
        int EVRInitError_VRInitError_Driver_HmdDisplayNotFound = 208;
        int EVRInitError_VRInitError_Driver_TrackedDeviceInterfaceUnknown = 209;
        int EVRInitError_VRInitError_Driver_HmdDriverIdOutOfBounds = 211;
        int EVRInitError_VRInitError_Driver_HmdDisplayMirrored = 212;
        int EVRInitError_VRInitError_Driver_HmdDisplayNotFoundLaptop = 213;
        int EVRInitError_VRInitError_IPC_ServerInitFailed = 300;
        int EVRInitError_VRInitError_IPC_ConnectFailed = 301;
        int EVRInitError_VRInitError_IPC_SharedStateInitFailed = 302;
        int EVRInitError_VRInitError_IPC_CompositorInitFailed = 303;
        int EVRInitError_VRInitError_IPC_MutexInitFailed = 304;
        int EVRInitError_VRInitError_IPC_Failed = 305;
        int EVRInitError_VRInitError_IPC_CompositorConnectFailed = 306;
        int EVRInitError_VRInitError_IPC_CompositorInvalidConnectResponse = 307;
        int EVRInitError_VRInitError_IPC_ConnectFailedAfterMultipleAttempts = 308;
        int EVRInitError_VRInitError_Compositor_Failed = 400;
        int EVRInitError_VRInitError_Compositor_D3D11HardwareRequired = 401;
        int EVRInitError_VRInitError_Compositor_FirmwareRequiresUpdate = 402;
        int EVRInitError_VRInitError_Compositor_OverlayInitFailed = 403;
        int EVRInitError_VRInitError_Compositor_ScreenshotsInitFailed = 404;
        int EVRInitError_VRInitError_Compositor_UnableToCreateDevice = 405;
        int EVRInitError_VRInitError_Compositor_SharedStateIsNull = 406;
        int EVRInitError_VRInitError_Compositor_NotificationManagerIsNull = 407;
        int EVRInitError_VRInitError_Compositor_ResourceManagerClientIsNull = 408;
        int EVRInitError_VRInitError_Compositor_MessageOverlaySharedStateInitFailure = 409;
        int EVRInitError_VRInitError_Compositor_PropertiesInterfaceIsNull = 410;
        int EVRInitError_VRInitError_Compositor_CreateFullscreenWindowFailed = 411;
        int EVRInitError_VRInitError_Compositor_SettingsInterfaceIsNull = 412;
        int EVRInitError_VRInitError_Compositor_FailedToShowWindow = 413;
        int EVRInitError_VRInitError_Compositor_DistortInterfaceIsNull = 414;
        int EVRInitError_VRInitError_Compositor_DisplayFrequencyFailure = 415;
        int EVRInitError_VRInitError_Compositor_RendererInitializationFailed = 416;
        int EVRInitError_VRInitError_Compositor_DXGIFactoryInterfaceIsNull = 417;
        int EVRInitError_VRInitError_Compositor_DXGIFactoryCreateFailed = 418;
        int EVRInitError_VRInitError_Compositor_DXGIFactoryQueryFailed = 419;
        int EVRInitError_VRInitError_Compositor_InvalidAdapterDesktop = 420;
        int EVRInitError_VRInitError_Compositor_InvalidHmdAttachment = 421;
        int EVRInitError_VRInitError_Compositor_InvalidOutputDesktop = 422;
        int EVRInitError_VRInitError_Compositor_InvalidDeviceProvided = 423;
        int EVRInitError_VRInitError_Compositor_D3D11RendererInitializationFailed = 424;
        int EVRInitError_VRInitError_Compositor_FailedToFindDisplayMode = 425;
        int EVRInitError_VRInitError_Compositor_FailedToCreateSwapChain = 426;
        int EVRInitError_VRInitError_Compositor_FailedToGetBackBuffer = 427;
        int EVRInitError_VRInitError_Compositor_FailedToCreateRenderTarget = 428;
        int EVRInitError_VRInitError_Compositor_FailedToCreateDXGI2SwapChain = 429;
        int EVRInitError_VRInitError_Compositor_FailedtoGetDXGI2BackBuffer = 430;
        int EVRInitError_VRInitError_Compositor_FailedToCreateDXGI2RenderTarget = 431;
        int EVRInitError_VRInitError_Compositor_FailedToGetDXGIDeviceInterface = 432;
        int EVRInitError_VRInitError_Compositor_SelectDisplayMode = 433;
        int EVRInitError_VRInitError_Compositor_FailedToCreateNvAPIRenderTargets = 434;
        int EVRInitError_VRInitError_Compositor_NvAPISetDisplayMode = 435;
        int EVRInitError_VRInitError_Compositor_FailedToCreateDirectModeDisplay = 436;
        int EVRInitError_VRInitError_Compositor_InvalidHmdPropertyContainer = 437;
        int EVRInitError_VRInitError_Compositor_UpdateDisplayFrequency = 438;
        int EVRInitError_VRInitError_Compositor_CreateRasterizerState = 439;
        int EVRInitError_VRInitError_Compositor_CreateWireframeRasterizerState = 440;
        int EVRInitError_VRInitError_Compositor_CreateSamplerState = 441;
        int EVRInitError_VRInitError_Compositor_CreateClampToBorderSamplerState = 442;
        int EVRInitError_VRInitError_Compositor_CreateAnisoSamplerState = 443;
        int EVRInitError_VRInitError_Compositor_CreateOverlaySamplerState = 444;
        int EVRInitError_VRInitError_Compositor_CreatePanoramaSamplerState = 445;
        int EVRInitError_VRInitError_Compositor_CreateFontSamplerState = 446;
        int EVRInitError_VRInitError_Compositor_CreateNoBlendState = 447;
        int EVRInitError_VRInitError_Compositor_CreateBlendState = 448;
        int EVRInitError_VRInitError_Compositor_CreateAlphaBlendState = 449;
        int EVRInitError_VRInitError_Compositor_CreateBlendStateMaskR = 450;
        int EVRInitError_VRInitError_Compositor_CreateBlendStateMaskG = 451;
        int EVRInitError_VRInitError_Compositor_CreateBlendStateMaskB = 452;
        int EVRInitError_VRInitError_Compositor_CreateDepthStencilState = 453;
        int EVRInitError_VRInitError_Compositor_CreateDepthStencilStateNoWrite = 454;
        int EVRInitError_VRInitError_Compositor_CreateDepthStencilStateNoDepth = 455;
        int EVRInitError_VRInitError_Compositor_CreateFlushTexture = 456;
        int EVRInitError_VRInitError_Compositor_CreateDistortionSurfaces = 457;
        int EVRInitError_VRInitError_Compositor_CreateConstantBuffer = 458;
        int EVRInitError_VRInitError_Compositor_CreateHmdPoseConstantBuffer = 459;
        int EVRInitError_VRInitError_Compositor_CreateHmdPoseStagingConstantBuffer = 460;
        int EVRInitError_VRInitError_Compositor_CreateSharedFrameInfoConstantBuffer = 461;
        int EVRInitError_VRInitError_Compositor_CreateOverlayConstantBuffer = 462;
        int EVRInitError_VRInitError_Compositor_CreateSceneTextureIndexConstantBuffer = 463;
        int EVRInitError_VRInitError_Compositor_CreateReadableSceneTextureIndexConstantBuffer = 464;
        int EVRInitError_VRInitError_Compositor_CreateLayerGraphicsTextureIndexConstantBuffer = 465;
        int EVRInitError_VRInitError_Compositor_CreateLayerComputeTextureIndexConstantBuffer = 466;
        int EVRInitError_VRInitError_Compositor_CreateLayerComputeSceneTextureIndexConstantBuffer = 467;
        int EVRInitError_VRInitError_Compositor_CreateComputeHmdPoseConstantBuffer = 468;
        int EVRInitError_VRInitError_Compositor_CreateGeomConstantBuffer = 469;
        int EVRInitError_VRInitError_Compositor_CreatePanelMaskConstantBuffer = 470;
        int EVRInitError_VRInitError_Compositor_CreatePixelSimUBO = 471;
        int EVRInitError_VRInitError_Compositor_CreateMSAARenderTextures = 472;
        int EVRInitError_VRInitError_Compositor_CreateResolveRenderTextures = 473;
        int EVRInitError_VRInitError_Compositor_CreateComputeResolveRenderTextures = 474;
        int EVRInitError_VRInitError_Compositor_CreateDriverDirectModeResolveTextures = 475;
        int EVRInitError_VRInitError_Compositor_OpenDriverDirectModeResolveTextures = 476;
        int EVRInitError_VRInitError_Compositor_CreateFallbackSyncTexture = 477;
        int EVRInitError_VRInitError_Compositor_ShareFallbackSyncTexture = 478;
        int EVRInitError_VRInitError_Compositor_CreateOverlayIndexBuffer = 479;
        int EVRInitError_VRInitError_Compositor_CreateOverlayVertextBuffer = 480;
        int EVRInitError_VRInitError_Compositor_CreateTextVertexBuffer = 481;
        int EVRInitError_VRInitError_Compositor_CreateTextIndexBuffer = 482;
        int EVRInitError_VRInitError_Compositor_CreateMirrorTextures = 483;
        int EVRInitError_VRInitError_Compositor_CreateLastFrameRenderTexture = 484;
        int EVRInitError_VRInitError_VendorSpecific_UnableToConnectToOculusRuntime = 1000;
        int EVRInitError_VRInitError_VendorSpecific_WindowsNotInDevMode = 1001;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_CantOpenDevice = 1101;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_UnableToRequestConfigStart = 1102;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_NoStoredConfig = 1103;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_ConfigTooBig = 1104;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_ConfigTooSmall = 1105;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_UnableToInitZLib = 1106;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_CantReadFirmwareVersion = 1107;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_UnableToSendUserDataStart = 1108;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_UnableToGetUserDataStart = 1109;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_UnableToGetUserDataNext = 1110;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_UserDataAddressRange = 1111;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_UserDataError = 1112;
        int EVRInitError_VRInitError_VendorSpecific_HmdFound_ConfigFailedSanityCheck = 1113;
        int EVRInitError_VRInitError_Steam_SteamInstallationNotFound = 2000;
        int EVRInitError_VRInitError_LastError = 2001;
    }

    public interface EVRInputError
    {
        int EVRInputError_VRInputError_None = 0;
        int EVRInputError_VRInputError_NameNotFound = 1;
        int EVRInputError_VRInputError_WrongType = 2;
        int EVRInputError_VRInputError_InvalidHandle = 3;
        int EVRInputError_VRInputError_InvalidParam = 4;
        int EVRInputError_VRInputError_NoSteam = 5;
        int EVRInputError_VRInputError_MaxCapacityReached = 6;
        int EVRInputError_VRInputError_IPCError = 7;
        int EVRInputError_VRInputError_NoActiveActionSet = 8;
        int EVRInputError_VRInputError_InvalidDevice = 9;
        int EVRInputError_VRInputError_InvalidSkeleton = 10;
        int EVRInputError_VRInputError_InvalidBoneCount = 11;
        int EVRInputError_VRInputError_InvalidCompressedData = 12;
        int EVRInputError_VRInputError_NoData = 13;
        int EVRInputError_VRInputError_BufferTooSmall = 14;
        int EVRInputError_VRInputError_MismatchedActionManifest = 15;
        int EVRInputError_VRInputError_MissingSkeletonData = 16;
        int EVRInputError_VRInputError_InvalidBoneIndex = 17;
    }

    public interface EVRInputFilterCancelType
    {
        int EVRInputFilterCancelType_VRInputFilterCancel_Timers = 0;
        int EVRInputFilterCancelType_VRInputFilterCancel_Momentum = 1;
    }

    public interface EVRInputStringBits
    {
        int EVRInputStringBits_VRInputString_Hand = 1;
        int EVRInputStringBits_VRInputString_ControllerType = 2;
        int EVRInputStringBits_VRInputString_InputSource = 4;
        int EVRInputStringBits_VRInputString_All = -1;
    }

    public interface EVRMouseButton
    {
        int EVRMouseButton_VRMouseButton_Left = 1;
        int EVRMouseButton_VRMouseButton_Right = 2;
        int EVRMouseButton_VRMouseButton_Middle = 4;
    }

    public interface EVRMuraCorrectionMode
    {
        int EVRMuraCorrectionMode_Default = 0;
        int EVRMuraCorrectionMode_NoCorrection = 1;
    }

    public interface EVRNotificationError
    {
        int EVRNotificationError_VRNotificationError_OK = 0;
        int EVRNotificationError_VRNotificationError_InvalidNotificationId = 100;
        int EVRNotificationError_VRNotificationError_NotificationQueueFull = 101;
        int EVRNotificationError_VRNotificationError_InvalidOverlayHandle = 102;
        int EVRNotificationError_VRNotificationError_SystemWithUserValueAlreadyExists = 103;
    }

    public interface EVRNotificationStyle
    {
        int EVRNotificationStyle_None = 0;
        int EVRNotificationStyle_Application = 100;
        int EVRNotificationStyle_Contact_Disabled = 200;
        int EVRNotificationStyle_Contact_Enabled = 201;
        int EVRNotificationStyle_Contact_Active = 202;
    }

    public interface EVRNotificationType
    {
        int EVRNotificationType_Transient = 0;
        int EVRNotificationType_Persistent = 1;
        int EVRNotificationType_Transient_SystemWithUserValue = 2;
    }

    public interface EVROverlayError
    {
        int EVROverlayError_VROverlayError_None = 0;
        int EVROverlayError_VROverlayError_UnknownOverlay = 10;
        int EVROverlayError_VROverlayError_InvalidHandle = 11;
        int EVROverlayError_VROverlayError_PermissionDenied = 12;
        int EVROverlayError_VROverlayError_OverlayLimitExceeded = 13;
        int EVROverlayError_VROverlayError_WrongVisibilityType = 14;
        int EVROverlayError_VROverlayError_KeyTooLong = 15;
        int EVROverlayError_VROverlayError_NameTooLong = 16;
        int EVROverlayError_VROverlayError_KeyInUse = 17;
        int EVROverlayError_VROverlayError_WrongTransformType = 18;
        int EVROverlayError_VROverlayError_InvalidTrackedDevice = 19;
        int EVROverlayError_VROverlayError_InvalidParameter = 20;
        int EVROverlayError_VROverlayError_ThumbnailCantBeDestroyed = 21;
        int EVROverlayError_VROverlayError_ArrayTooSmall = 22;
        int EVROverlayError_VROverlayError_RequestFailed = 23;
        int EVROverlayError_VROverlayError_InvalidTexture = 24;
        int EVROverlayError_VROverlayError_UnableToLoadFile = 25;
        int EVROverlayError_VROverlayError_KeyboardAlreadyInUse = 26;
        int EVROverlayError_VROverlayError_NoNeighbor = 27;
        int EVROverlayError_VROverlayError_TooManyMaskPrimitives = 29;
        int EVROverlayError_VROverlayError_BadMaskPrimitive = 30;
        int EVROverlayError_VROverlayError_TextureAlreadyLocked = 31;
        int EVROverlayError_VROverlayError_TextureLockCapacityReached = 32;
        int EVROverlayError_VROverlayError_TextureNotLocked = 33;
    }

    public interface EVROverlayIntersectionMaskPrimitiveType
    {
        int EVROverlayIntersectionMaskPrimitiveType_OverlayIntersectionPrimitiveType_Rectangle = 0;
        int EVROverlayIntersectionMaskPrimitiveType_OverlayIntersectionPrimitiveType_Circle = 1;
    }

    public interface EVRRenderModelError
    {
        int EVRRenderModelError_VRRenderModelError_None = 0;
        int EVRRenderModelError_VRRenderModelError_Loading = 100;
        int EVRRenderModelError_VRRenderModelError_NotSupported = 200;
        int EVRRenderModelError_VRRenderModelError_InvalidArg = 300;
        int EVRRenderModelError_VRRenderModelError_InvalidModel = 301;
        int EVRRenderModelError_VRRenderModelError_NoShapes = 302;
        int EVRRenderModelError_VRRenderModelError_MultipleShapes = 303;
        int EVRRenderModelError_VRRenderModelError_TooManyVertices = 304;
        int EVRRenderModelError_VRRenderModelError_MultipleTextures = 305;
        int EVRRenderModelError_VRRenderModelError_BufferTooSmall = 306;
        int EVRRenderModelError_VRRenderModelError_NotEnoughNormals = 307;
        int EVRRenderModelError_VRRenderModelError_NotEnoughTexCoords = 308;
        int EVRRenderModelError_VRRenderModelError_InvalidTexture = 400;
    }

    public interface EVRScreenshotError
    {
        int EVRScreenshotError_VRScreenshotError_None = 0;
        int EVRScreenshotError_VRScreenshotError_RequestFailed = 1;
        int EVRScreenshotError_VRScreenshotError_IncompatibleVersion = 100;
        int EVRScreenshotError_VRScreenshotError_NotFound = 101;
        int EVRScreenshotError_VRScreenshotError_BufferTooSmall = 102;
        int EVRScreenshotError_VRScreenshotError_ScreenshotAlreadyInProgress = 108;
    }

    public interface EVRScreenshotPropertyFilenames
    {
        int EVRScreenshotPropertyFilenames_VRScreenshotPropertyFilenames_Preview = 0;
        int EVRScreenshotPropertyFilenames_VRScreenshotPropertyFilenames_VR = 1;
    }

    public interface EVRScreenshotType
    {
        int EVRScreenshotType_VRScreenshotType_None = 0;
        int EVRScreenshotType_VRScreenshotType_Mono = 1;
        int EVRScreenshotType_VRScreenshotType_Stereo = 2;
        int EVRScreenshotType_VRScreenshotType_Cubemap = 3;
        int EVRScreenshotType_VRScreenshotType_MonoPanorama = 4;
        int EVRScreenshotType_VRScreenshotType_StereoPanorama = 5;
    }

    public interface EVRSettingsError
    {
        int EVRSettingsError_VRSettingsError_None = 0;
        int EVRSettingsError_VRSettingsError_IPCFailed = 1;
        int EVRSettingsError_VRSettingsError_WriteFailed = 2;
        int EVRSettingsError_VRSettingsError_ReadFailed = 3;
        int EVRSettingsError_VRSettingsError_JsonParseFailed = 4;
        int EVRSettingsError_VRSettingsError_UnsetSettingHasNoDefault = 5;
    }

    public interface EVRSkeletalMotionRange
    {
        int EVRSkeletalMotionRange_VRSkeletalMotionRange_WithController = 0;
        int EVRSkeletalMotionRange_VRSkeletalMotionRange_WithoutController = 1;
    }

    public interface EVRSkeletalReferencePose
    {
        int EVRSkeletalReferencePose_VRSkeletalReferencePose_BindPose = 0;
        int EVRSkeletalReferencePose_VRSkeletalReferencePose_OpenHand = 1;
        int EVRSkeletalReferencePose_VRSkeletalReferencePose_Fist = 2;
        int EVRSkeletalReferencePose_VRSkeletalReferencePose_GripLimit = 3;
    }

    public interface EVRSkeletalTrackingLevel
    {
        int EVRSkeletalTrackingLevel_VRSkeletalTracking_Estimated = 0;
        int EVRSkeletalTrackingLevel_VRSkeletalTracking_Partial = 1;
        int EVRSkeletalTrackingLevel_VRSkeletalTracking_Full = 2;
        int EVRSkeletalTrackingLevel_VRSkeletalTrackingLevel_Count = 3;
        int EVRSkeletalTrackingLevel_VRSkeletalTrackingLevel_Max = 2;
    }

    public interface EVRSkeletalTransformSpace
    {
        int EVRSkeletalTransformSpace_VRSkeletalTransformSpace_Model = 0;
        int EVRSkeletalTransformSpace_VRSkeletalTransformSpace_Parent = 1;
    }

    public interface EVRSpatialAnchorError
    {
        int EVRSpatialAnchorError_VRSpatialAnchorError_Success = 0;
        int EVRSpatialAnchorError_VRSpatialAnchorError_Internal = 1;
        int EVRSpatialAnchorError_VRSpatialAnchorError_UnknownHandle = 2;
        int EVRSpatialAnchorError_VRSpatialAnchorError_ArrayTooSmall = 3;
        int EVRSpatialAnchorError_VRSpatialAnchorError_InvalidDescriptorChar = 4;
        int EVRSpatialAnchorError_VRSpatialAnchorError_NotYetAvailable = 5;
        int EVRSpatialAnchorError_VRSpatialAnchorError_NotAvailableInThisUniverse = 6;
        int EVRSpatialAnchorError_VRSpatialAnchorError_PermanentlyUnavailable = 7;
        int EVRSpatialAnchorError_VRSpatialAnchorError_WrongDriver = 8;
        int EVRSpatialAnchorError_VRSpatialAnchorError_DescriptorTooLong = 9;
        int EVRSpatialAnchorError_VRSpatialAnchorError_Unknown = 10;
        int EVRSpatialAnchorError_VRSpatialAnchorError_NoRoomCalibration = 11;
        int EVRSpatialAnchorError_VRSpatialAnchorError_InvalidArgument = 12;
        int EVRSpatialAnchorError_VRSpatialAnchorError_UnknownDriver = 13;
    }

    public interface EVRState
    {
        int EVRState_VRState_Undefined = -1;
        int EVRState_VRState_Off = 0;
        int EVRState_VRState_Searching = 1;
        int EVRState_VRState_Searching_Alert = 2;
        int EVRState_VRState_Ready = 3;
        int EVRState_VRState_Ready_Alert = 4;
        int EVRState_VRState_NotReady = 5;
        int EVRState_VRState_Standby = 6;
        int EVRState_VRState_Ready_Alert_Low = 7;
    }

    public interface EVRSubmitFlags
    {
        int EVRSubmitFlags_Submit_Default = 0;
        int EVRSubmitFlags_Submit_LensDistortionAlreadyApplied = 1;
        int EVRSubmitFlags_Submit_GlRenderBuffer = 2;
        int EVRSubmitFlags_Submit_Reserved = 4;
        int EVRSubmitFlags_Submit_TextureWithPose = 8;
        int EVRSubmitFlags_Submit_TextureWithDepth = 16;
    }

    public interface EVRSummaryType
    {
        int EVRSummaryType_VRSummaryType_FromAnimation = 0;
        int EVRSummaryType_VRSummaryType_FromDevice = 1;
    }

    public interface EVRTrackedCameraError
    {
        int EVRTrackedCameraError_VRTrackedCameraError_None = 0;
        int EVRTrackedCameraError_VRTrackedCameraError_OperationFailed = 100;
        int EVRTrackedCameraError_VRTrackedCameraError_InvalidHandle = 101;
        int EVRTrackedCameraError_VRTrackedCameraError_InvalidFrameHeaderVersion = 102;
        int EVRTrackedCameraError_VRTrackedCameraError_OutOfHandles = 103;
        int EVRTrackedCameraError_VRTrackedCameraError_IPCFailure = 104;
        int EVRTrackedCameraError_VRTrackedCameraError_NotSupportedForThisDevice = 105;
        int EVRTrackedCameraError_VRTrackedCameraError_SharedMemoryFailure = 106;
        int EVRTrackedCameraError_VRTrackedCameraError_FrameBufferingFailure = 107;
        int EVRTrackedCameraError_VRTrackedCameraError_StreamSetupFailure = 108;
        int EVRTrackedCameraError_VRTrackedCameraError_InvalidGLTextureId = 109;
        int EVRTrackedCameraError_VRTrackedCameraError_InvalidSharedTextureHandle = 110;
        int EVRTrackedCameraError_VRTrackedCameraError_FailedToGetGLTextureId = 111;
        int EVRTrackedCameraError_VRTrackedCameraError_SharedTextureFailure = 112;
        int EVRTrackedCameraError_VRTrackedCameraError_NoFrameAvailable = 113;
        int EVRTrackedCameraError_VRTrackedCameraError_InvalidArgument = 114;
        int EVRTrackedCameraError_VRTrackedCameraError_InvalidFrameBufferSize = 115;
    }

    public interface EVRTrackedCameraFrameLayout
    {
        int EVRTrackedCameraFrameLayout_Mono = 1;
        int EVRTrackedCameraFrameLayout_Stereo = 2;
        int EVRTrackedCameraFrameLayout_VerticalLayout = 16;
        int EVRTrackedCameraFrameLayout_HorizontalLayout = 32;
    }

    public interface EVRTrackedCameraFrameType
    {
        int EVRTrackedCameraFrameType_VRTrackedCameraFrameType_Distorted = 0;
        int EVRTrackedCameraFrameType_VRTrackedCameraFrameType_Undistorted = 1;
        int EVRTrackedCameraFrameType_VRTrackedCameraFrameType_MaximumUndistorted = 2;
        int EVRTrackedCameraFrameType_MAX_CAMERA_FRAME_TYPES = 3;
    }

    public interface EVSync
    {
        int EVSync_VSync_None = 0;
        int EVSync_VSync_WaitRender = 1;
        int EVSync_VSync_NoWaitRender = 2;
    }

    public static class ID3D12CommandQueue extends PointerType
    {
        public ID3D12CommandQueue(Pointer address)
        {
            super(address);
        }

        public ID3D12CommandQueue()
        {
        }
    }

    public static class ID3D12Resource extends PointerType
    {
        public ID3D12Resource(Pointer address)
        {
            super(address);
        }

        public ID3D12Resource()
        {
        }
    }

    public interface Imu_OffScaleFlags
    {
        int Imu_OffScaleFlags_OffScale_AccelX = 1;
        int Imu_OffScaleFlags_OffScale_AccelY = 2;
        int Imu_OffScaleFlags_OffScale_AccelZ = 4;
        int Imu_OffScaleFlags_OffScale_GyroX = 8;
        int Imu_OffScaleFlags_OffScale_GyroY = 16;
        int Imu_OffScaleFlags_OffScale_GyroZ = 32;
    }

    public interface VRMessageOverlayResponse
    {
        int VRMessageOverlayResponse_ButtonPress_0 = 0;
        int VRMessageOverlayResponse_ButtonPress_1 = 1;
        int VRMessageOverlayResponse_ButtonPress_2 = 2;
        int VRMessageOverlayResponse_ButtonPress_3 = 3;
        int VRMessageOverlayResponse_CouldntFindSystemOverlay = 4;
        int VRMessageOverlayResponse_CouldntFindOrCreateClientOverlay = 5;
        int VRMessageOverlayResponse_ApplicationQuit = 6;
    }

    public interface VROverlayFlags
    {
        int VROverlayFlags_None = 0;
        int VROverlayFlags_Curved = 1;
        int VROverlayFlags_RGSS4X = 2;
        int VROverlayFlags_NoDashboardTab = 3;
        int VROverlayFlags_AcceptsGamepadEvents = 4;
        int VROverlayFlags_ShowGamepadFocus = 5;
        int VROverlayFlags_SendVRDiscreteScrollEvents = 6;
        int VROverlayFlags_SendVRTouchpadEvents = 7;
        int VROverlayFlags_ShowTouchPadScrollWheel = 8;
        int VROverlayFlags_TransferOwnershipToInternalProcess = 9;
        int VROverlayFlags_SideBySide_Parallel = 10;
        int VROverlayFlags_SideBySide_Crossed = 11;
        int VROverlayFlags_Panorama = 12;
        int VROverlayFlags_StereoPanorama = 13;
        int VROverlayFlags_SortWithNonSceneOverlays = 14;
        int VROverlayFlags_VisibleInDashboard = 15;
        int VROverlayFlags_MakeOverlaysInteractiveIfVisible = 16;
        int VROverlayFlags_SendVRSmoothScrollEvents = 17;
    }

    public interface VROverlayInputMethod
    {
        int VROverlayInputMethod_None = 0;
        int VROverlayInputMethod_Mouse = 1;
        int VROverlayInputMethod_DualAnalog = 2;
    }

    public interface VROverlayTransformType
    {
        int VROverlayTransformType_VROverlayTransform_Absolute = 0;
        int VROverlayTransformType_VROverlayTransform_TrackedDeviceRelative = 1;
        int VROverlayTransformType_VROverlayTransform_SystemOverlay = 2;
        int VROverlayTransformType_VROverlayTransform_TrackedComponent = 3;
    }

    public static class VkDevice_T extends PointerType
    {
        public VkDevice_T(Pointer address)
        {
            super(address);
        }

        public VkDevice_T()
        {
        }
    }

    public static class VkInstance_T extends PointerType
    {
        public VkInstance_T(Pointer address)
        {
            super(address);
        }

        public VkInstance_T()
        {
        }
    }

    public static class VkPhysicalDevice_T extends PointerType
    {
        public VkPhysicalDevice_T(Pointer address)
        {
            super(address);
        }

        public VkPhysicalDevice_T()
        {
        }
    }

    public static class VkQueue_T extends PointerType
    {
        public VkQueue_T(Pointer address)
        {
            super(address);
        }

        public VkQueue_T()
        {
        }
    }
}
