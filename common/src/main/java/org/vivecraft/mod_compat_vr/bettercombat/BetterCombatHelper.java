package org.vivecraft.mod_compat_vr.bettercombat;

import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.item.ItemStack;
import org.vivecraft.Xloader;

public class BetterCombatHelper {

    public static boolean isLoaded() {
        return Xloader.isModLoaded("bettercombat");
    }

    /**
     * checks if the ItemStack has attributes, and returns the attackRange attribute
     *
     * @param rangeIn   original range that gets returned when no attributes are present
     * @param itemStack ItemStack to check
     * @return attackRange of the ItemStack, or {@code rangeIn} when no attribute is present
     */
    public static double getItemRange(double rangeIn, ItemStack itemStack) {
        WeaponAttributes attribute = WeaponRegistry.getAttributes(itemStack);
        // try to avoid breaking swings completely
        if (attribute != null && attribute.attackRange() != 0) {
            return attribute.attackRange();
        }
        return rangeIn;
    }
}
