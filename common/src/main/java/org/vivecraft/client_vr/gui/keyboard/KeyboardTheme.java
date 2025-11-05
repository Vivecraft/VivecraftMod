package org.vivecraft.client_vr.gui.keyboard;

import org.vivecraft.client_vr.settings.OptionEnum;
import org.vivecraft.client_vr.utils.RGBAColor;

public enum KeyboardTheme implements OptionEnum<KeyboardTheme> {
    DEFAULT((SolidTheme) color -> color.setRGB(1F, 1F, 1F)),
    RED((SolidTheme) color -> color.setRGB(1F, 0F, 0F)),
    GREEN((SolidTheme) color -> color.setRGB(0F, 1F, 0F)),
    BLUE((SolidTheme) color -> color.setRGB(0F, 0F, 1F)),
    BLACK((SolidTheme) color -> color.setRGB(0F, 0F, 0F)),
    GRASS((PositionTheme) (color, x, y) -> {
        if (y >= 0 && y < 2) {
            color.setRGB(0.321F, 0.584F, 0.184F);
        } else {
            color.setRGB(0.607F, 0.462F, 0.325F);
        }
    }),
    BEES((PositionTheme) (color, x, y) -> {
        if (x % 4 < 2) {
            color.setRGB(1F, 1F, 0F);
        } else {
            color.setRGB(0F, 0F, 0F);
        }
    }),
    AESTHETIC((IdTheme) (color, key) -> {
        if (key >= 1000) {
            color.setRGB(0F, 1F, 1F);
        } else {
            color.setRGB(1F, 0F, 1F);
        }
    }),
    DOSE((IdTheme) (color, key) -> {
        if (key % 2 == 0) {
            color.setRGB(0.5F, 0F, 1F);
        } else {
            color.setRGB(0F, 1F, 0F);
        }
    }),
    CUSTOM(new CustomKeyboardTheme());

    public final Theme theme;

    KeyboardTheme(Theme theme) {
        this.theme = theme;
    }

    @FunctionalInterface
    public interface Theme {

        /**
         * gets the color for the given key
         *
         * @param color RGBAColor Object to update
         * @param keyId id of the key, 0-499 regular keys, 500-999, shift keys, 1000+ special keys
         * @param keyX  x position of the key slot
         * @param keyY  y position of hte key slot
         */
        void updateColor(RGBAColor color, int keyId, int keyX, int keyY);

        /**
         * reloads the theme, if it needs to
         */
        default void reload() {}

        /**
         * updates the theme, if it has any kind of animation
         */
        default void update() {}
    }

    /**
     * A Theme that has the same color for each key
     */
    @FunctionalInterface
    interface SolidTheme extends Theme {
        @Override
        default void updateColor(RGBAColor color, int keyId, int keyX, int keyY) {
            updateColor(color);
        }

        void updateColor(RGBAColor color);
    }

    /**
     * A Theme that changes color based on x/y key position
     */
    @FunctionalInterface
    interface PositionTheme extends Theme {
        @Override
        default void updateColor(RGBAColor color, int keyId, int keyX, int keyY) {
            updateColor(color, keyX, keyY);
        }

        void updateColor(RGBAColor color, int keyX, int keyY);
    }

    /**
     * A Theme that changes color based on key id
     */
    @FunctionalInterface
    interface IdTheme extends Theme {
        @Override
        default void updateColor(RGBAColor color, int keyId, int keyX, int keyY) {
            updateColor(color, keyId);
        }

        void updateColor(RGBAColor color, int keyIndex);
    }
}
