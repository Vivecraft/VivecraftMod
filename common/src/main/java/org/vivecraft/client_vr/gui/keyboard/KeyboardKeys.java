package org.vivecraft.client_vr.gui.keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLKeycode;
import org.lwjgl.sdl.SDLScancode;
import org.vivecraft.client.gui.settings.GuiActiveKeyboardLayoutSelector;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KeyboardKeys {

    public static final int COLUMNS = 13;
    public static final int ROWS = 4;
    public static final int MAX_ROWS = 7;
    public static final int SPECIAL_KEY_WIDTH = 2;

    private static final List<Key> SPECIAL_KEYS = new ArrayList<>();
    private static int SPECIAL_INDEX = 1000;

    public static final Key SHIFT_1;
    public static final Key SHIFT_2;

    public static final Key LAYOUT_SELECT;

    static {
        SHIFT_1 = addSpecial(Key.wide(SPECIAL_INDEX++, 0, 4, "shift", () -> {}));
        SHIFT_2 = addSpecial(Key.wide(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + COLUMNS, 4, "shift", () -> {}));

        addSpecial(
            new Key(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + (COLUMNS - 5) / 2, -1, 5, 1, " ",
                SDLScancode.SDL_SCANCODE_SPACE, ' '));
        addSpecial(Key.wide(SPECIAL_INDEX++, 0, 2, "tab", SDLScancode.SDL_SCANCODE_TAB));
        addSpecial(Key.wide(SPECIAL_INDEX++, 0, 1, "esc", SDLScancode.SDL_SCANCODE_ESCAPE));
        addSpecial(
            Key.wide(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + COLUMNS, 1, "backspace", SDLScancode.SDL_SCANCODE_BACKSPACE));
        addSpecial(
            Key.wide(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + COLUMNS, 3, "enter", SDLScancode.SDL_SCANCODE_RETURN));

        // Arrow keys
        addSpecial(
            Key.single(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + COLUMNS + 1, 5, "↑", SDLScancode.SDL_SCANCODE_UP));
        addSpecial(
            Key.single(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + COLUMNS + 1, 6, "↓", SDLScancode.SDL_SCANCODE_DOWN));
        addSpecial(
            Key.single(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + COLUMNS, 6, "←", SDLScancode.SDL_SCANCODE_LEFT));
        addSpecial(
            Key.single(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH + COLUMNS + 2, 6, "→", SDLScancode.SDL_SCANCODE_RIGHT));

        addSpecial(Key.wide(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH, 0, "cut", () -> {
            InputSimulator.pressKey(SDLScancode.SDL_SCANCODE_LCTRL);
            InputSimulator.pressKey(SDLScancode.SDL_SCANCODE_X, SDLKeycode.SDL_KMOD_LCTRL);
            InputSimulator.releaseKey(SDLScancode.SDL_SCANCODE_X, SDLKeycode.SDL_KMOD_LCTRL);
            InputSimulator.releaseKey(SDLScancode.SDL_SCANCODE_LCTRL);
        }));
        addSpecial(Key.wide(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH * 2, 0, "copy", () -> {
            InputSimulator.pressKey(SDLScancode.SDL_SCANCODE_LCTRL);
            InputSimulator.pressKey(SDLScancode.SDL_SCANCODE_C);
            InputSimulator.releaseKey(SDLScancode.SDL_SCANCODE_C);
            InputSimulator.releaseKey(SDLScancode.SDL_SCANCODE_LCTRL);
        }));
        addSpecial(Key.wide(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH * 3, 0, "paste", () -> {
            InputSimulator.pressKey(SDLScancode.SDL_SCANCODE_LCTRL);
            InputSimulator.pressKey(SDLScancode.SDL_SCANCODE_V);
            InputSimulator.releaseKey(SDLScancode.SDL_SCANCODE_V);
            InputSimulator.releaseKey(SDLScancode.SDL_SCANCODE_LCTRL);
        }));
        addSpecial(Key.wide(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH * 4, 0, "switch",
            () -> ClientDataHolderVR.getInstance().vrSettings.nextKeyboardLayout()));
        LAYOUT_SELECT = addSpecial(Key.singleIcon(SPECIAL_INDEX++, SPECIAL_KEY_WIDTH * 5, 0,
            "options.language", Identifier.withDefaultNamespace("icon/language"),
            () -> {
                Minecraft mc = Minecraft.getInstance();
                // don't open it multiple times
                if (!(mc.gui.screen() instanceof GuiActiveKeyboardLayoutSelector)) {
                    mc.gui.setScreen(new GuiActiveKeyboardLayoutSelector(mc.gui.screen()));
                }
            }));
    }

    private static Key addSpecial(Key key) {
        SPECIAL_KEYS.add(key);
        return key;
    }

    public static List<Key> getSpecialKeys(Runnable shiftTask) {
        List<Key> keys = new ArrayList<>(SPECIAL_KEYS.size());
        for (Key key : SPECIAL_KEYS) {
            if (key.isShift()) {
                keys.add(
                    new Key(key.id, key.x, key.y, key.width, key.height, key.label, key.icon, shiftTask, () -> {}));
            } else {
                keys.add(key);
            }
        }
        return keys;
    }

    public static List<Key> getSpecialKeys() {
        return Collections.unmodifiableList(SPECIAL_KEYS);
    }

    /**
     * returns all current regular keys of the current active keyboard layout, and the size of the key grid
     *
     * @param shift      if shift or regular keys should be generated
     * @param afterPress a task that is executed after any key is pressed
     * @return Layout containing the keys, and the size
     */
    public static Layout getRegularKeys(boolean shift, Runnable afterPress) {
        return getRegularKeys(ClientDataHolderVR.getInstance().vrSettings.getKeyboardLayout(), shift, afterPress);
    }

    /**
     * returns all current regular keys for the give nKeyboard layout, and the size of the key grid
     *
     * @param layout     KeyboardLayout to get the keys for
     * @param shift      if shift or regular keys should be generated
     * @param afterPress a task that is executed after any key is pressed
     * @return Layout containing the keys, and the size
     */
    public static Layout getRegularKeys(
        VRSettings.KeyboardLayout layout, boolean shift, Runnable afterPress)
    {
        VRSettings vrSettings = ClientDataHolderVR.getInstance().vrSettings;
        String chars = shift ? layout.shift().get() : layout.regular().get();

        int rows = ROWS;
        float calcRows = (float) chars.length() / (float) COLUMNS;
        if (Math.abs(ROWS - calcRows) > 0.01F) {
            rows = Mth.ceil(calcRows);
        }

        List<Key> keys = new ArrayList<>(rows * COLUMNS);

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int index = row * COLUMNS + column;
                char ch = index < chars.length() ? chars.charAt(index) : '\u0000';
                final int code =
                    index < vrSettings.keyboardCodes.length ? vrSettings.keyboardCodes[index] :
                        SDLScancode.SDL_SCANCODE_UNKNOWN;
                keys.add(
                    Key.single(shift ? index + 500 : index, column + KeyboardKeys.SPECIAL_KEY_WIDTH, 1 + row, code, ch,
                        afterPress));
            }
        }
        return new Layout(keys, COLUMNS, rows);
    }


    /**
     * @param id        ID of the key, 0-499 regular keys, 500-999 shifted keys, 1000+ special keys
     * @param x         X position of the key
     * @param y         Y position of the key, if negative it is below the keyboard
     * @param label     label of the key
     * @param icon      if set will show the icon instead of the label
     * @param onPress   action to do when the key is pressed
     * @param onRelease action to do when the key is released
     */
    public record Key(int id, int x, int y, int width, int height, Component label, @Nullable Identifier icon,
                      Runnable onPress, Runnable onRelease)
    {

        /**
         * key that types a char, and presses a key if set in the settings
         */
        private Key(
            int id, int x, int y, int width, int height, String label, int keyCode, char keyChar)
        {
            this(id, x, y, width, height, label, keyCode, keyChar, () -> {});
        }

        /**
         * key that types a char, and presses a key if set in the settings
         */
        private Key(
            int id, int x, int y, int width, int height, String label, int keyCode, char keyChar, Runnable afterPress)
        {
            this(id, x, y, width, height, Component.literal(label), null, () -> {
                InputSimulator.pressKeyForBind(keyCode);
                if (keyChar != '\u0000') {
                    InputSimulator.typeChar(keyChar);
                }

                if (keyChar == '/' && Minecraft.getInstance().gui.screen() == null) {
                    // this is dumb but whatever
                    InputSimulator.pressKey(SDLScancode.SDL_SCANCODE_SLASH);
                    InputSimulator.releaseKey(SDLScancode.SDL_SCANCODE_SLASH);
                }
                afterPress.run();
            }, () -> InputSimulator.releaseKeyForBind(keyCode));
        }

        /**
         * 1x1 key that types a char, and presses a key if set in the settings
         */
        private static Key single(int id, int x, int y, int keyCode, char keyChar, Runnable afterPress) {
            return new Key(id, x, y, 1, 1, keyChar != '\u0000' ? String.valueOf(keyChar) : "", keyCode, keyChar,
                afterPress);
        }

        /**
         * 1x1 key that always presses a key
         */
        private static Key single(int id, int x, int y, String label, int keyCode) {
            return new Key(id, x, y, 1, 1, Component.literal(label), null, () -> InputSimulator.pressKey(keyCode),
                () -> InputSimulator.releaseKey(keyCode));
        }

        /**
         * 1x1 key that does an action with an icon
         */
        private static Key singleIcon(int id, int x, int y, String tooltipKey, Identifier icon, Runnable onPress) {
            return new Key(id, x, y, 1, 1, Component.translatable(tooltipKey), icon, onPress, () -> {});
        }

        /**
         * 2x1 key that always presses a key
         */
        private static Key wide(int id, int x, int y, String langKey, int keyCode) {
            return new Key(id, x, y, 2, 1, Component.translatable("vivecraft.keyboard.key." + langKey),
                null, () -> InputSimulator.pressKey(keyCode), () -> InputSimulator.releaseKey(keyCode));
        }

        /**
         * 2x1 key that does an action
         */
        private static Key wide(int id, int x, int y, String langKey, Runnable onPress) {
            return new Key(id, x, y, 2, 1, Component.translatable("vivecraft.keyboard.key." + langKey),
                null, onPress, () -> {});
        }

        public boolean isShift() {
            return this.id == SHIFT_1.id || this.id == SHIFT_2.id;
        }
    }

    public record Layout(List<Key> keys, int columns, int rows) {}

    ;
}
