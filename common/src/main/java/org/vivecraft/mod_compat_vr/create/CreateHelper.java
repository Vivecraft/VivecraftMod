package org.vivecraft.mod_compat_vr.create;

import org.vivecraft.Xloader;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

public class CreateHelper {

    private static boolean INITIALIZED = false;

    private static Field LinkedControllerClientHandler_MODE;
    private static Object Mode_IDLE;

    private static Field ControlsHandler_entityRef;

    public static boolean isLoaded() {
        return Xloader.isModLoaded("create");
    }

    public static boolean blocksMovement() {
        init();
        return isUsingLinkedController() || isControllingTrain();
    }

    private static boolean isUsingLinkedController() {
        try {
            return LinkedControllerClientHandler_MODE != null &&
                LinkedControllerClientHandler_MODE.get(null) != Mode_IDLE;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    private static boolean isControllingTrain() {
        try {
            return ControlsHandler_entityRef != null &&
                ((WeakReference<?>) ControlsHandler_entityRef.get(null)).get() != null;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static void init() {
        if (!INITIALIZED) {
            try {
                LinkedControllerClientHandler_MODE = Class.forName(
                        "com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler")
                    .getField("MODE");
                Mode_IDLE = Class.forName(
                        "com.simibubi.create.content.redstone.link.controller.LinkedControllerClientHandler$Mode")
                    .getField("IDLE").get(null);
            } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException ignored) {}

            try {
                ControlsHandler_entityRef = Class.forName(
                        "com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler")
                    .getDeclaredField("entityRef");
                ControlsHandler_entityRef.setAccessible(true);
            } catch (NoSuchFieldException | ClassNotFoundException ignored) {}
            INITIALIZED = true;
        }
    }
}
