package org.vivecraft.client_vr;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLScancode;
import org.vivecraft.client_vr.provider.InputSimulator;

import java.nio.ByteBuffer;

public abstract class MethodHolder {


    public static boolean isKeyDown(InputConstants.Key key) {
        return key.getType() == InputConstants.Type.KEYBOARD && key.getValue() != SDLScancode.SDL_SCANCODE_UNKNOWN &&
            isKeyDown(key.getValue());
    }

    public static boolean isKeyDown(int scancode) {
        ByteBuffer keyboardState = SDLKeyboard.SDL_GetKeyboardState();
        return keyboardState != null && keyboardState.get(scancode) != 0 || InputSimulator.isKeyDown(scancode);
    }

    public static boolean isInMenuRoom() {
        return willBeInMenuRoom(Minecraft.getInstance().gui.screen());
    }

    public static boolean willBeInMenuRoom(Screen newScreen) {
        return Minecraft.getInstance().level == null ||
            newScreen instanceof WinScreen ||
            newScreen instanceof LevelLoadingScreen ||
            newScreen instanceof ProgressScreen ||
            newScreen instanceof GenericMessageScreen ||
            Minecraft.getInstance().gui.overlay() != null;
    }
}
