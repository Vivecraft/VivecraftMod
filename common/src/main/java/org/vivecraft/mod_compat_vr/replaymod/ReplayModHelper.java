package org.vivecraft.mod_compat_vr.replaymod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReplayModHelper {

    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Method RecordingEventSender_getRecordingEventHandler;
    private static Method RecordingEventHandler_onPacket;

    private static Method ConnectionEventHandler_getRecordingEventHandler;
    private static Field ReplayModRecording_instance;
    private static Method ReplayModRecording_getConnectionEventHandler;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("replaymod") || Xloader.INSTANCE.isModLoaded("reforgedplaymod");
    }

    public static void storePacket(Packet<?> packet) {
        if (init()) {
            try {
                Object recorder;
                if (RecordingEventSender_getRecordingEventHandler != null) {
                    recorder = RecordingEventSender_getRecordingEventHandler.invoke(
                        Minecraft.getInstance().levelRenderer);
                } else {
                    recorder = ConnectionEventHandler_getRecordingEventHandler
                        .invoke(ReplayModRecording_getConnectionEventHandler
                            .invoke(ReplayModRecording_instance.get(null)));
                }

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
            try {
                Class<?> RecordingEventSender = Class.forName(
                    "com.replaymod.recording.handler.RecordingEventHandler$RecordingEventSender");
                RecordingEventSender_getRecordingEventHandler = RecordingEventSender.getMethod(
                    "getRecordingEventHandler");
            } catch (ClassNotFoundException e) {
                // recording event handler might be in ConnectionEventHandler
                Class<?> ConnectionEventHandler = Class.forName(
                    "com.replaymod.recording.handler.ConnectionEventHandler");
                ConnectionEventHandler_getRecordingEventHandler = ConnectionEventHandler.getMethod(
                    "getRecordingEventHandler");

                Class<?> ReplayModRecording = Class.forName(
                    "com.replaymod.recording.ReplayModRecording");
                ReplayModRecording_getConnectionEventHandler = ReplayModRecording.getMethod(
                    "getConnectionEventHandler");
                ReplayModRecording_instance = ReplayModRecording.getField("instance");
            }

            Class<?> RecordingEventHandler = Class.forName(
                "com.replaymod.recording.handler.RecordingEventHandler");
            RecordingEventHandler_onPacket = RecordingEventHandler.getMethod("onPacket", Packet.class);
        } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException e) {
            INIT_FAILED = true;
            VRSettings.LOGGER.error("Vivecraft: Failed to initialize ReplayMod compat", e);
        }
        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
