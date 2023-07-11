package org.vivecraft.mixin.client_vr.blaze3d.audio;

import com.mojang.blaze3d.audio.Library;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.SOFTHRTF;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.Objects;

@Mixin(Library.class)
public class LibraryVRMixin {
    @Shadow
    @Final
    static Logger LOGGER;

    @Shadow
    private long currentDevice;

    @Unique
    private boolean checkALError(String string) {
        int i = AL10.alGetError();
        if (i != 0) {
            LOGGER.error("{}: {}", string, alErrorToString(i));
            return true;
        } else {
            return false;
        }
    }

    @Unique
    private String alErrorToString(int i) {
        return switch (i) {
            case 40961 -> "Invalid name parameter.";
            case 40962 -> "Invalid enumerated parameter value.";
            case 40963 -> "Invalid parameter parameter value.";
            case 40964 -> "Invalid operation.";
            case 40965 -> "Unable to allocate memory.";
            default -> "An unrecognized error occurred.";
        };
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/audio/OpenAlUtil;checkALError(Ljava/lang/String;)Z", ordinal = 0, shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void setHRTF(String string, CallbackInfo ci, ALCCapabilities aLCCapabilities, int i, int j, int k, ALCapabilities aLCapabilities) {
        if (!VRState.vrRunning) {
            return;
        }
        ClientDataHolderVR.hrtfList.clear();

        if (aLCCapabilities.ALC_SOFT_HRTF) {
            int l = ALC10.alcGetInteger(this.currentDevice, 6548);

            if (l > 0) {
                LOGGER.info("Available HRTFs:");

                for (int i1 = 0; i1 < l; i1++) {
                    String s = Objects.requireNonNull(SOFTHRTF.alcGetStringiSOFT(this.currentDevice, 6549, i1));
                    ClientDataHolderVR.hrtfList.add(s);
                    LOGGER.info("{}: {}", i1, s);
                }

                int k1 = ClientDataHolderVR.getInstance().vrSettings.hrtfSelection;
                int l1;

                if (k1 == -1) {
                    l1 = 0;
                } else {
                    l1 = 1;
                }

                IntBuffer intbuffer = BufferUtils.createIntBuffer(10).put(6546).put(l1);

                if (k1 != -1) {
                    if (k1 > 0 && k1 <= ClientDataHolderVR.hrtfList.size()) {
                        LOGGER.info("Using HRTF: {}", ClientDataHolderVR.hrtfList.get(k1 - 1));
                        intbuffer.put(6550).put(k1 - 1);
                    } else {
                        if (k1 > ClientDataHolderVR.hrtfList.size()) {
                            LOGGER.warn("Invalid HRTF index: {}", k1);
                        }

                        LOGGER.info("Using default HRTF");
                    }
                } else {
                    LOGGER.info("Disabling HRTF");
                }

                ((Buffer) intbuffer.put(0)).flip();
                SOFTHRTF.alcResetDeviceSOFT(this.currentDevice, intbuffer);

                if (!checkALError("HRTF initialization")) {
                    LOGGER.info("HRTF initialized.");
                    int j1 = ALC10.alcGetInteger(this.currentDevice, 6547);

                    switch (j1) {
                        case 0:
                            LOGGER.info("HRTF status: disabled");
                            break;

                        case 1:
                            LOGGER.info("HRTF status: enabled");
                            break;

                        case 2:
                            LOGGER.info("HRTF status: denied");
                            break;

                        case 3:
                            LOGGER.info("HRTF status: required");
                            break;

                        case 4:
                            LOGGER.info("HRTF status: headphones detected");
                            break;

                        case 5:
                            LOGGER.info("HRTF status: unsupported format");
                    }
                }
            } else {
                LOGGER.warn("No HRTFs found.");
            }
        } else {
            LOGGER.warn("ALC_SOFT_HRTF is not supported.");
        }
    }
}
