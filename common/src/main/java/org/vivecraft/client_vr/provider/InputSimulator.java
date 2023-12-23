package org.vivecraft.client_vr.provider;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.HashSet;
import java.util.Set;

public class InputSimulator {
    private static final Set<Integer> pressedKeys = new HashSet<>();

    public static boolean isKeyDown(int key) {
        return pressedKeys.contains(key);
    }

    public static void pressKey(int key, int modifiers) {
        pressedKeys.add(key);
        Minecraft.getInstance().keyboardHandler.keyPress(Minecraft.getInstance().getWindow().getWindow(), key, 0, 1, modifiers);
    }

    public static void pressKey(int key) {
        pressKey(key, 0);
    }

    public static void releaseKey(int key, int modifiers) {
        pressedKeys.remove(key);
        Minecraft.getInstance().keyboardHandler.keyPress(Minecraft.getInstance().getWindow().getWindow(), key, 0, 0, modifiers);
    }

    public static void releaseKey(int key) {
        releaseKey(key, 0);
    }

    public static void typeChar(char character, int modifiers) {
        Minecraft.getInstance().keyboardHandler.charTyped(Minecraft.getInstance().getWindow().getWindow(), character, modifiers);
    }

    public static void typeChar(char character) {
        typeChar(character, 0);
    }

    public static void pressMouse(int button, int modifiers) {
        Minecraft.getInstance().mouseHandler.onPress(Minecraft.getInstance().getWindow().getWindow(), button, 1, modifiers);
    }

    public static void pressMouse(int button) {
        pressMouse(button, 0);
    }

    public static void releaseMouse(int button, int modifiers) {
        Minecraft.getInstance().mouseHandler.onPress(Minecraft.getInstance().getWindow().getWindow(), button, 0, modifiers);
    }

    public static void releaseMouse(int button) {
        releaseMouse(button, 0);
    }

    public static void setMousePos(double x, double y) {
        Minecraft.getInstance().mouseHandler.onMove(Minecraft.getInstance().getWindow().getWindow(), x, y);
    }

    public static void scrollMouse(double xOffset, double yOffset) {
        Minecraft.getInstance().mouseHandler.onScroll(Minecraft.getInstance().getWindow().getWindow(), xOffset, yOffset);
    }

    public static void typeChars(CharSequence characters) {
        int i = characters.length();

        for (int j = 0; j < i; ++j) {
            char c0 = characters.charAt(j);
            typeChar(c0);
        }
    }

    private static long airTypingWarningTime;

    public static void pressKeyForBind(int code) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (dataHolder.vrSettings.keyboardPressBinds) {
            if (code != GLFW.GLFW_KEY_UNKNOWN) {
                pressKey(code);
            }
        } else if (minecraft.screen == null && Utils.milliTime() - airTypingWarningTime >= 30000) {
            minecraft.gui.getChat().addMessage(Component.translatable("vivecraft.messages.airtypingwarning"));
            airTypingWarningTime = Utils.milliTime();
        }
    }


    public static void releaseKeyForBind(int code) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (dataHolder.vrSettings.keyboardPressBinds && code != GLFW.GLFW_KEY_UNKNOWN) {
            releaseKey(code);
        }
    }
}
