package org.vivecraft.client_vr.provider;

import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simulates GLFW inputs and keeps track of them
 */
public class InputSimulator {
    private static final Set<Integer> PRESSED_KEYS = new HashSet<>();
    private static final Map<Integer, Integer> PRESSED_MODIFIERS = new HashMap<>();

    public static boolean isKeyDown(int key) {
        return PRESSED_KEYS.contains(key) || (PRESSED_MODIFIERS.getOrDefault(key, 0) > 0);
    }

    private static void handleKeyAction(int key, int modifiers, int action) {
        Minecraft.getInstance().keyboardHandler.keyPress(Minecraft.getInstance().getWindow().handle(), action,
            new KeyEvent(key, 0, modifiers));
    }

    public static void pressKey(int key, int modifiers) {
        PRESSED_KEYS.add(key);
        handleKeyAction(key, modifiers, GLFW.GLFW_PRESS);
    }

    public static void pressKey(int key) {
        pressKey(key, getActiveModifier());
    }

    public static void releaseKey(int key, int modifiers) {
        PRESSED_KEYS.remove(key);
        handleKeyAction(key, modifiers, GLFW.GLFW_RELEASE);
    }

    public static void releaseKey(int key) {
        releaseKey(key, getActiveModifier());
    }

    public static void pressModifier(int key, int modifiers) {
        PRESSED_MODIFIERS.merge(key, 1, Integer::sum);
        handleKeyAction(key, modifiers, GLFW.GLFW_PRESS);
    }

    public static void pressModifier(int key) {
        pressModifier(key, 0);
    }

    public static void releaseModifier(int key, int modifiers) {
        PRESSED_MODIFIERS.merge(key, -1, Integer::sum);
        handleKeyAction(key, modifiers, GLFW.GLFW_RELEASE);
    }

    public static void releaseModifier(int key) {
        releaseModifier(key, 0);
    }

    public static void typeChar(char character, int modifiers) {
        Minecraft.getInstance().keyboardHandler.charTyped(Minecraft.getInstance().getWindow().handle(),
            new CharacterEvent(character));
    }

    public static void typeChar(char character) {
        typeChar(character, 0);
    }

    public static void pressMouse(int button, int modifiers) {
        Minecraft.getInstance().mouseHandler.onButton(Minecraft.getInstance().getWindow().handle(),
            new MouseButtonInfo(button, modifiers), GLFW.GLFW_PRESS);
    }

    public static void pressMouse(int button) {
        pressMouse(button, getActiveModifier());
    }

    public static void releaseMouse(int button, int modifiers) {
        Minecraft.getInstance().mouseHandler.onButton(Minecraft.getInstance().getWindow().handle(),
            new MouseButtonInfo(button, modifiers), GLFW.GLFW_RELEASE);
    }

    public static void releaseMouse(int button) {
        releaseMouse(button, getActiveModifier());
    }

    public static void setMousePos(double x, double y) {
        Minecraft.getInstance().mouseHandler.onMove(Minecraft.getInstance().getWindow().handle(), x, y);
    }

    public static void scrollMouse(double xOffset, double yOffset) {
        Minecraft.getInstance().mouseHandler.onScroll(Minecraft.getInstance().getWindow().handle(), xOffset, yOffset);
    }

    public static void typeChars(CharSequence characters) {
        for (int i = 0; i < characters.length(); i++) {
            char character = characters.charAt(i);
            typeChar(character);
        }
    }

    private static long AIR_TYPING_WARNING_TIME;

    public static void pressKeyForBind(int code) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (dataHolder.vrSettings.keyboardPressBinds) {
            if (code != GLFW.GLFW_KEY_UNKNOWN) {
                pressKey(code);
            }
        } else if (minecraft.screen == null && ClientUtils.milliTime() - AIR_TYPING_WARNING_TIME >= 30000) {
            ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.airtypingwarning"));
            AIR_TYPING_WARNING_TIME = ClientUtils.milliTime();
        }
    }


    public static void releaseKeyForBind(int code) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (dataHolder.vrSettings.keyboardPressBinds && code != GLFW.GLFW_KEY_UNKNOWN) {
            releaseKey(code);
        }
    }

    private static int getActiveModifier() {
        return (shiftDown() ? GLFW.GLFW_MOD_SHIFT : 0) |
            (controlDown() ? InputQuirks.EDIT_SHORTCUT_KEY_MODIFIER : 0) |
            (altDown() ? GLFW.GLFW_MOD_ALT : 0);
    }

    private static boolean shiftDown() {
        return MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT) || MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private static boolean controlDown() {
        return MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT_CONTROL) ||
            MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    private static boolean altDown() {
        return MethodHolder.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) || MethodHolder.isKeyDown(GLFW.GLFW_KEY_RIGHT_ALT);
    }
}
