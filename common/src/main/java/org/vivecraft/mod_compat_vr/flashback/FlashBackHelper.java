package org.vivecraft.mod_compat_vr.flashback;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FlashBackHelper {

    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Field Flashback_RECORDER;
    private static Method Recorder_writePacketAsync;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("flashback");
    }

    public static void storePacket(Packet<?> packet) {
        if (init()) {
            try {
                Object recorder = Flashback_RECORDER.get(null);
                if (recorder != null) {
                    Recorder_writePacketAsync.invoke(recorder, packet, ConnectionProtocol.PLAY);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("Failed to store flashback player data", e);
            }
        }
    }

    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_FAILED;
        }
        try {
            Class<?> Flashback = Class.forName("com.moulberry.flashback.Flashback");
            Flashback_RECORDER = Flashback.getField("RECORDER");
            Class<?> Recorder = Class.forName("com.moulberry.flashback.record.Recorder");
            Recorder_writePacketAsync = Recorder.getMethod("writePacketAsync", Packet.class, ConnectionProtocol.class
            );
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
            INIT_FAILED = true;
            VRSettings.LOGGER.error("Vivecraft: Failed to initialize FlashBack compat", e);
        }
        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
