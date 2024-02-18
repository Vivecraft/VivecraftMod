package org.vivecraft.mod_compat_vr.epicfight;

import com.mojang.blaze3d.platform.InputConstants;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.provider.InputSimulator;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.input.EpicFightKeyMappings;

public class EpicFightHelper {

    public static boolean isLoaded() {
        return Xplat.isModLoaded("epicfight");
    }

    public static boolean attack() {
        if (ClientEngine.getInstance().controllEngine.getPlayerPatch().isBattleMode()) {
            InputConstants.Key key = EpicFightKeyMappings.ATTACK.key;
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
            return true;
        }
        return false;
    }
}
