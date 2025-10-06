package org.vivecraft.mixin.client_vr.blaze3d.audio;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.audio.Library;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.SOFTHRTF;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.VRSettings;

import java.nio.IntBuffer;
import java.util.Objects;

@Mixin(Library.class)
public class LibraryVRMixin {

    @Shadow
    private long currentDevice;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/OpenAlUtil;checkALError(Ljava/lang/String;)Z", ordinal = 0, shift = At.Shift.AFTER))
    private void vivecraft$setHRTF(String string, CallbackInfo ci, @Local ALCCapabilities aLCCapabilities) {
        if (!VRState.VR_RUNNING) return;

        ClientDataHolderVR.getInstance().hrtfList.clear();

        if (aLCCapabilities.ALC_SOFT_HRTF) {
            int hrtfCount = ALC10.alcGetInteger(this.currentDevice, 6548);

            if (hrtfCount > 0) {
                VRSettings.LOGGER.info("Vivecraft: Available HRTFs:");

                for (int i = 0; i < hrtfCount; i++) {
                    String name = Objects.requireNonNull(
                        SOFTHRTF.alcGetStringiSOFT(this.currentDevice, SOFTHRTF.ALC_HRTF_SPECIFIER_SOFT, i));
                    ClientDataHolderVR.getInstance().hrtfList.add(name);
                    VRSettings.LOGGER.info("Vivecraft: {}: {}", i, name);
                }

                int selectedIndex = ClientDataHolderVR.getInstance().vrSettings.hrtfSelection;

                int hrtfEnable;
                if (selectedIndex == -1) {
                    hrtfEnable = ALC10.ALC_FALSE;
//                } else if (selectedIndex == 0) {
//                    // This usually just results in disabled
//                    hrtfEnable = SOFTHRTF.ALC_DONT_CARE_SOFT;
                } else {
                    hrtfEnable = ALC10.ALC_TRUE;
                }

                IntBuffer buf = BufferUtils.createIntBuffer(10).put(SOFTHRTF.ALC_HRTF_SOFT).put(hrtfEnable);

                if (selectedIndex != -1) {
                    if (selectedIndex > 0 && selectedIndex <= ClientDataHolderVR.getInstance().hrtfList.size()) {
                        VRSettings.LOGGER.info("Using HRTF: {}",
                            ClientDataHolderVR.getInstance().hrtfList.get(selectedIndex - 1));
                        buf.put(SOFTHRTF.ALC_HRTF_ID_SOFT).put(selectedIndex - 1);
                    } else {
                        if (selectedIndex > ClientDataHolderVR.getInstance().hrtfList.size()) {
                            VRSettings.LOGGER.warn("Invalid HRTF index: {}", selectedIndex);
                        }
                        VRSettings.LOGGER.info("Using default HRTF");
                    }
                } else {
                    VRSettings.LOGGER.info("Disabling HRTF");
                }

                buf.put(0).flip();
                SOFTHRTF.alcResetDeviceSOFT(this.currentDevice, buf);

                if (!vivecraft$checkALError("HRTF initialization")) {
                    VRSettings.LOGGER.info("Vivecraft: HRTF initialized.");
                    switch (ALC10.alcGetInteger(this.currentDevice, SOFTHRTF.ALC_HRTF_STATUS_SOFT)) {
                        case SOFTHRTF.ALC_HRTF_DISABLED_SOFT ->
                            VRSettings.LOGGER.info("Vivecraft: HRTF status: disabled");
                        case SOFTHRTF.ALC_HRTF_ENABLED_SOFT ->
                            VRSettings.LOGGER.info("Vivecraft: HRTF status: enabled");
                        case SOFTHRTF.ALC_HRTF_DENIED_SOFT -> VRSettings.LOGGER.info("Vivecraft: HRTF status: denied");
                        case SOFTHRTF.ALC_HRTF_REQUIRED_SOFT ->
                            VRSettings.LOGGER.info("Vivecraft: HRTF status: required");
                        case SOFTHRTF.ALC_HRTF_HEADPHONES_DETECTED_SOFT ->
                            VRSettings.LOGGER.info("Vivecraft: HRTF status: headphones detected");
                        case SOFTHRTF.ALC_HRTF_UNSUPPORTED_FORMAT_SOFT ->
                            VRSettings.LOGGER.info("Vivecraft: HRTF status: unsupported format");
                    }
                }
            } else {
                VRSettings.LOGGER.warn("Vivecraft: No HRTFs found.");
            }
        } else {
            VRSettings.LOGGER.warn("Vivecraft: ALC_SOFT_HRTF is not supported.");
        }
    }

    /**
     * this is a copy of {@link com.mojang.blaze3d.audio.OpenAlUtil#checkALError}
     */
    @Unique
    private boolean vivecraft$checkALError(String operationState) {
        int error = AL10.alGetError();
        if (error != 0) {
            VRSettings.LOGGER.error("{}: {}", operationState, vivecraft$alErrorToString(error));
            return true;
        } else {
            return false;
        }
    }

    /**
     * this is a copy of {@link com.mojang.blaze3d.audio.OpenAlUtil#alErrorToString(int)}
     */
    @Unique
    private String vivecraft$alErrorToString(int errorCode) {
        return switch (errorCode) {
            case ALC10.ALC_INVALID_DEVICE -> "Invalid name parameter.";
            case ALC10.ALC_INVALID_CONTEXT -> "Invalid enumerated parameter value.";
            case ALC10.ALC_INVALID_ENUM -> "Invalid parameter parameter value.";
            case ALC10.ALC_INVALID_VALUE -> "Invalid operation.";
            case ALC10.ALC_OUT_OF_MEMORY -> "Unable to allocate memory.";
            default -> "An unrecognized error occurred.";
        };
    }
}
