package org.vivecraft.client_vr.provider;

import java.util.HashSet;
import java.util.Set;

import static org.vivecraft.client_vr.VRState.mc;

public class InputSimulator
{
    private static final Set<Integer> pressedKeys = new HashSet<>();

    public static boolean isKeyDown(int key)
    {
        return pressedKeys.contains(key);
    }

    public static void pressKey(int key, int modifiers)
    {
        mc.keyboardHandler.keyPress(mc.getWindow().getWindow(), key, 0, 1, modifiers);
        pressedKeys.add(key);
    }

    public static void pressKey(int key)
    {
        pressKey(key, 0);
    }

    public static void releaseKey(int key, int modifiers)
    {
        mc.keyboardHandler.keyPress(mc.getWindow().getWindow(), key, 0, 0, modifiers);
        pressedKeys.remove(key);
    }

    public static void releaseKey(int key)
    {
        releaseKey(key, 0);
    }

    public static void typeChar(char character, int modifiers)
    {
        mc.keyboardHandler.charTyped(mc.getWindow().getWindow(), character, modifiers);
    }

    public static void typeChar(char character)
    {
        typeChar(character, 0);
    }

    public static void pressMouse(int button, int modifiers)
    {
        mc.mouseHandler.onPress(mc.getWindow().getWindow(), button, 1, modifiers);
    }

    public static void pressMouse(int button)
    {
        pressMouse(button, 0);
    }

    public static void releaseMouse(int button, int modifiers)
    {
        mc.mouseHandler.onPress(mc.getWindow().getWindow(), button, 0, modifiers);
    }

    public static void releaseMouse(int button)
    {
        releaseMouse(button, 0);
    }

    public static void setMousePos(double x, double y)
    {
        mc.mouseHandler.onMove(mc.getWindow().getWindow(), x, y);
    }

    public static void scrollMouse(double xOffset, double yOffset)
    {
        mc.mouseHandler.onScroll(mc.getWindow().getWindow(), xOffset, yOffset);
    }

    public static void typeChars(CharSequence characters)
    {
        int i = characters.length();

        for (int j = 0; j < i; ++j)
        {
            char c0 = characters.charAt(j);
            typeChar(c0);
        }
    }
}
