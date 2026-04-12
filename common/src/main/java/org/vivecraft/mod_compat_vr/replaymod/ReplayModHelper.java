package org.vivecraft.mod_compat_vr.replaymod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReplayModHelper {

    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Method RecordingEventSender_getRecordingEventHandler;
    private static Method RecordingEventHandler_onPacket;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("replaymod") || Xloader.INSTANCE.isModLoaded("reforgedplaymod");
    }

    public static void storePacket(Packet<?> packet) {
        if (init()) {
            try {
                Object recorder = RecordingEventSender_getRecordingEventHandler.invoke(
                    Minecraft.getInstance().levelRenderer);
                if (recorder != null) {
                    RecordingEventHandler_onPacket.invoke(recorder, packet);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                VRSettings.LOGGER.error("Failed to store replaymod player data", e);
            }
        }
    }

    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_FAILED;
        }
        try {
            Class<?> RecordingEventSender = Class.forName(
                "com.replaymod.recording.handler.RecordingEventHandler$RecordingEventSender");
            RecordingEventSender_getRecordingEventHandler = RecordingEventSender.getMethod("getRecordingEventHandler");

            Class<?> RecordingEventHandler = Class.forName(
                "com.replaymod.recording.handler.RecordingEventHandler");
            RecordingEventHandler_onPacket = RecordingEventHandler.getMethod("onPacket", Packet.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            INIT_FAILED = true;
            VRSettings.LOGGER.error("Vivecraft: Failed to initialize ReplayMod compat", e);
        }
        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
