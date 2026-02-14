package org.vivecraft.data;

import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public class ViveItems {

    /**
     * creates a new Climbing Claws item
     *
     * @return the created Climbing Claws item
     */
    public static ItemStack newClimbingClaws() {
        ItemStack claws = new ItemStack(Items.SHEARS);
        claws.setHoverName(new TranslatableComponent("vivecraft.item.climbclaws"));
        claws.getOrCreateTag().putBoolean("Unbreakable", true);
        claws.getOrCreateTag().putInt("HideFlags", ItemStack.TooltipPart.UNBREAKABLE.getMask());
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
        } else if (!itemStack.hasCustomHoverName()) {
            return false;
        } else if (itemStack.getItem() != Items.SHEARS) {
            return false;
        } else if (!itemStack.hasTag() || !itemStack.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return itemStack.getHoverName().getString().equals("Climb Claws") ||
                (itemStack.getHoverName() instanceof TranslatableComponent translatableContent &&
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
        boots.setHoverName(new TranslatableComponent("vivecraft.item.jumpboots"));
        boots.getOrCreateTag().putBoolean("Unbreakable", true);
        boots.getOrCreateTag().putInt("HideFlags", ItemStack.TooltipPart.UNBREAKABLE.getMask());
        boots.getOrCreateTagElement(ItemStack.TAG_DISPLAY).putInt(ItemStack.TAG_COLOR, 0x8CE56F);
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
        } else if (!itemStack.hasCustomHoverName()) {
            return false;
        } else if (itemStack.getItem() != Items.LEATHER_BOOTS) {
            return false;
        } else if (!itemStack.hasTag() || !itemStack.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return itemStack.getHoverName().getString().equals("Jump Boots") ||
                (itemStack.getHoverName() instanceof TranslatableComponent translatableContent &&
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
        growPie.setHoverName(new TextComponent("EAT ME"));
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
        ItemStack shrinkPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        shrinkPotion.setHoverName(new TextComponent("DRINK ME"));
        shrinkPotion.getOrCreateTag().putInt("HideFlags", ItemStack.TooltipPart.ADDITIONAL.getMask());
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
