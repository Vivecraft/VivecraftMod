package org.vivecraft.client_vr;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client_vr.provider.InputSimulator;

public abstract class MethodHolder {


    public static boolean isKeyDown(InputConstants.Key key) {
        return key.getType() == InputConstants.Type.KEYSYM && key.getValue() != GLFW.GLFW_KEY_UNKNOWN &&
            isKeyDown(key.getValue());
    }

    public static boolean isKeyDown(int i) {
        return GLFW.glfwGetKey(Minecraft.getInstance().getWindow().handle(), i) == 1 || InputSimulator.isKeyDown(i);
    }

    public static boolean isInMenuRoom() {
        return willBeInMenuRoom(Minecraft.getInstance().screen);
    }

    public static boolean willBeInMenuRoom(Screen newScreen) {
        return Minecraft.getInstance().level == null ||
            newScreen instanceof WinScreen ||
            newScreen instanceof LevelLoadingScreen ||
            newScreen instanceof ProgressScreen ||
            newScreen instanceof GenericMessageScreen ||
            Minecraft.getInstance().getOverlay() != null;
    }
}
