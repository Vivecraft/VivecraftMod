package org.vivecraft.mod_compat_vr.modmenu;

import java.lang.reflect.Method;

public class ModMenuHelper {
    public static boolean shouldOffsetButtons() {
        try {
            Class<?> modMenuConfig = Class.forName("com.terraformersmc.modmenu.config.ModMenuConfig");
            Class<?> enumConfig = Class.forName("com.terraformersmc.modmenu.config.option.EnumConfigOption");
            Method value = enumConfig.getDeclaredMethod("getValue");

            Object style = modMenuConfig.getDeclaredField("GAME_MENU_BUTTON_STYLE").get(null);

            // check for enum name, previously that was REPLACE_BUGS
            return value.invoke(style).toString().equals("REPLACE");

        } catch (Exception e) {
            return false;
        }
    }
}
