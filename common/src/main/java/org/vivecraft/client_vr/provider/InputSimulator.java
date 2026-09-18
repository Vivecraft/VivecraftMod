package org.vivecraft.client_vr.provider;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.lwjgl.sdl.SDLKeycode;
import org.lwjgl.sdl.SDLScancode;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simulates SDL inputs and keeps track of them
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
        handleKeyAction(key, modifiers, InputConstants.PRESS);
    }

    public static void pressKey(int key) {
        pressKey(key, getActiveModifier());
    }

    public static void releaseKey(int key, int modifiers) {
        PRESSED_KEYS.remove(key);
        handleKeyAction(key, modifiers, InputConstants.RELEASE);
    }

    public static void releaseKey(int key) {
        releaseKey(key, getActiveModifier());
    }

    public static void pressModifier(int key, int modifiers) {
        PRESSED_MODIFIERS.merge(key, 1, Integer::sum);
        handleKeyAction(key, modifiers, InputConstants.PRESS);
    }

    public static void pressModifier(int key) {
        pressModifier(key, 0);
    }

    public static void releaseModifier(int key, int modifiers) {
        PRESSED_MODIFIERS.merge(key, -1, Integer::sum);
        handleKeyAction(key, modifiers, InputConstants.RELEASE);
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
            new MouseButtonInfo(button, modifiers), InputConstants.PRESS);
    }

    public static void pressMouse(int button) {
        pressMouse(button, getActiveModifier());
    }

    public static void releaseMouse(int button, int modifiers) {
        Minecraft.getInstance().mouseHandler.onButton(Minecraft.getInstance().getWindow().handle(),
            new MouseButtonInfo(button, modifiers), InputConstants.RELEASE);
    }

    public static void releaseMouse(int button) {
        releaseMouse(button, getActiveModifier());
    }

    public static void setMousePos(double x, double y) {
        Minecraft.getInstance().mouseHandler.onMove(Minecraft.getInstance().getWindow().handle(), x, y, 0, 0);
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
            if (code != SDLScancode.SDL_SCANCODE_UNKNOWN) {
                pressKey(code);
            }
        } else if (minecraft.gui.screen() == null && ClientUtils.milliTime() - AIR_TYPING_WARNING_TIME >= 30000) {
            ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.airtypingwarning"));
            AIR_TYPING_WARNING_TIME = ClientUtils.milliTime();
        }
    }


    public static void releaseKeyForBind(int code) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();

        if (dataHolder.vrSettings.keyboardPressBinds && code != SDLScancode.SDL_SCANCODE_UNKNOWN) {
            releaseKey(code);
        }
    }

    public static final int LEFT_CTRL_QUIRK =
        InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY ? SDLKeycode.SDL_KMOD_LGUI : SDLKeycode.SDL_KMOD_LCTRL;
    public static final int RIGHT_CTRL_QUIRK =
        InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY ? SDLKeycode.SDL_KMOD_RGUI : SDLKeycode.SDL_KMOD_RCTRL;

    private static int getActiveModifier() {
        return (MethodHolder.isKeyDown(SDLScancode.SDL_SCANCODE_LSHIFT) ? SDLKeycode.SDL_KMOD_LSHIFT : 0) |
            (MethodHolder.isKeyDown(SDLScancode.SDL_SCANCODE_RSHIFT) ? SDLKeycode.SDL_KMOD_RSHIFT : 0) |
            (MethodHolder.isKeyDown(SDLScancode.SDL_SCANCODE_LCTRL) ? LEFT_CTRL_QUIRK : 0) |
            (MethodHolder.isKeyDown(SDLScancode.SDL_SCANCODE_RCTRL) ? RIGHT_CTRL_QUIRK : 0) |
            (MethodHolder.isKeyDown(SDLScancode.SDL_SCANCODE_LALT) ? SDLKeycode.SDL_KMOD_LALT : 0) |
            (MethodHolder.isKeyDown(SDLScancode.SDL_SCANCODE_RALT) ? SDLKeycode.SDL_KMOD_RALT : 0);
    }
}
