package org.vivecraft.mod_compat_vr.epicfight;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.vivecraft.Xloader;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.common.utils.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EpicFightHelper {

    private static boolean INITIALIZED = false;
    private static boolean INIT_FAILED = false;

    private static Object ClientEngine_instance;
    private static Field ClientEngine_controlEngine;

    private static Method ControlEngine_getPlayerPatch;
    private static Method PlayerPatch_isBattleMode;

    private static KeyMapping EpicFight_ATTACK;

    public static boolean isLoaded() {
        return Xloader.INSTANCE.isModLoaded("epicfight");
    }

    /**
     * triggers the epic fight attack
     *
     * @return if this handled the attack
     */
    public static boolean attack() {
        if (init()) {
            try {
                if ((boolean) PlayerPatch_isBattleMode.invoke(
                    ControlEngine_getPlayerPatch.invoke(ClientEngine_controlEngine.get(ClientEngine_instance))))
                {
                    InputConstants.Key key = EpicFight_ATTACK.key;
                    switch (key.getType()) {
                        case MOUSE -> {
                            InputSimulator.pressMouse(key.getValue());
                            InputSimulator.releaseMouse(key.getValue());
                        }
                        case KEYSYM -> {
                            InputSimulator.pressKey(key.getValue());
                            InputSimulator.releaseKey(key.getValue());
                        }
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    private static boolean init() {
        if (INITIALIZED) {
            return !INIT_FAILED;
        }
        try {
            Class<?> ClientEngine = Class.forName("yesman.epicfight.client.ClientEngine");
            ClientEngine_instance = ClientEngine.getMethod("getInstance").invoke(null);
            ClientEngine_controlEngine = ClassUtils.getFieldWithAlternative(ClientEngine, "controlEngine",
                "controllEngine");


            Class<?> ControlEngine = ClassUtils.getClassWithAlternative(
                "yesman.epicfight.client.events.engine.ControlEngine",
                "yesman.epicfight.client.events.engine.ControllEngine");

            ControlEngine_getPlayerPatch = ControlEngine.getMethod("getPlayerPatch");
            PlayerPatch_isBattleMode = Class.forName(
                "yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch").getMethod("isBattleMode");

            EpicFight_ATTACK = (KeyMapping) Class.forName("yesman.epicfight.client.input.EpicFightKeyMappings")
                .getField("ATTACK").get(null);
        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
            VRSettings.LOGGER.error("Vivecraft: failed to initiialize EpicFight compat", e);
            INIT_FAILED = true;
        }
        INITIALIZED = true;
        return !INIT_FAILED;
    }
}
