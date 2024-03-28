package org.vivecraft.mod_compat_vr.bettercombat;

import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.item.ItemStack;

public class BetterCombatHelper {

    public static double getItemRange(double rangeIn, ItemStack itemStack) {
        WeaponAttributes attribute = WeaponRegistry.getAttributes(itemStack);
        if (attribute != null) {
            return attribute.attackRange();
        }
        return rangeIn;
    }
}
