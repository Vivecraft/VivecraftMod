package org.vivecraft.data;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;

public class ViveItems {

    /**
     * creates a new Climbing Claws item
     *
     * @return the created Climbing Claws item
     */
    public static ItemStack newClimbingClaws() {
        ItemStack claws = new ItemStack(Items.SHEARS);
        claws.set(DataComponents.CUSTOM_NAME,
            Component.translatableWithFallback("vivecraft.item.climbclaws", "Climb Claws"));
        claws.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        claws.set(DataComponents.TOOLTIP_DISPLAY,
            TooltipDisplay.DEFAULT.withHidden(DataComponents.UNBREAKABLE, true));
        return claws;
    }

    /**
     * Checks if the given Item is a Climbing Claws item
     *
     * @param itemStack ItemStack to check
     * @return if the given {@code itemStack} is a Climbing Claws item
     */
    public static boolean isClimbingClaws(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        } else if (!itemStack.has(DataComponents.CUSTOM_NAME)) {
            return false;
        } else if (itemStack.getItem() != Items.SHEARS) {
            return false;
        } else if (!itemStack.has(DataComponents.UNBREAKABLE)) {
            return false;
        } else {
            return itemStack.getHoverName().getString().equals("Climb Claws") ||
                (itemStack.getHoverName().getContents() instanceof TranslatableContents translatableContent &&
                    translatableContent.getKey().equals("vivecraft.item.climbclaws")
                );
        }
    }

    /**
     * creates a new Jump Boots item
     *
     * @return the created Jump Boots item
     */
    public static ItemStack newJumpBoots() {
        ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
        boots.set(DataComponents.CUSTOM_NAME,
            Component.translatableWithFallback("vivecraft.item.jumpboots", "Jump Boots"));
        boots.set(DataComponents.UNBREAKABLE, Unit.INSTANCE);
        boots.set(DataComponents.DYED_COLOR, new DyedItemColor(0x8CE56F));
        boots.set(DataComponents.TOOLTIP_DISPLAY,
            TooltipDisplay.DEFAULT.withHidden(DataComponents.UNBREAKABLE, true)
                .withHidden(DataComponents.DYED_COLOR, true));
        return boots;
    }

    /**
     * Checks if the given Item is a Jump Boots item
     *
     * @param itemStack ItemStack to check
     * @return if the given {@code itemStack} is a Jump Boots item
     */
    public static boolean isJumpBoots(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else if (!itemStack.has(DataComponents.CUSTOM_NAME)) {
            return false;
        } else if (itemStack.getItem() != Items.LEATHER_BOOTS) {
            return false;
        } else if (!itemStack.has(DataComponents.UNBREAKABLE)) {
            return false;
        } else {
            return itemStack.getHoverName().getString().equals("Jump Boots") ||
                (itemStack.getHoverName().getContents() instanceof TranslatableContents translatableContent &&
                    translatableContent.getKey().equals("vivecraft.item.jumpboots")
                );
        }
    }

    /**
     * creates a new Grow Pie item
     *
     * @return the created Grow Pie item
     */
    public static ItemStack newGrowPie() {
        ItemStack growPie = new ItemStack(Items.PUMPKIN_PIE);
        growPie.set(DataComponents.CUSTOM_NAME, Component.literal("EAT ME"));
        return growPie;
    }

    /**
     * Checks if the given Item is a Grow Pie item
     *
     * @param itemStack ItemStack to check
     * @return if the given {@code itemStack} is a Grow Pie item
     */
    public static boolean isGrowPie(ItemStack itemStack) {
        return itemStack.is(Items.PUMPKIN_PIE) && itemStack.getHoverName().getString().equals("EAT ME");
    }


    /**
     * creates a new Shrinking Potion item
     *
     * @return the created Shrinking Potion item
     */
    public static ItemStack newShrinkPotion() {
        ItemStack shrinkPotion = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        shrinkPotion.set(DataComponents.CUSTOM_NAME, Component.literal("DRINK ME"));
        return shrinkPotion;
    }

    /**
     * Checks if the given Item is a Shrinking Potion item
     *
     * @param itemStack ItemStack to check
     * @return if the given {@code itemStack} is a Shrinking Potion item
     */
    public static boolean isShrinkPotion(ItemStack itemStack) {
        return itemStack.is(Items.POTION) && itemStack.getHoverName().getString().equals("DRINK ME");
    }
}
