package org.vivecraft.asm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;

public class ASMDelegator
{
    public static boolean containerCreativeMouseDown(int eatTheStack)
    {
        return false;
    }

    public static void addCreativeItems(CreativeModeTab tab, NonNullList<ItemStack> list)
    {
        if (tab == CreativeModeTab.TAB_FOOD || tab == null)
        {
            ItemStack itemstack = (new ItemStack(Items.PUMPKIN_PIE)).setHoverName(new TextComponent("EAT ME"));
            ItemStack itemstack1 = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER).setHoverName(new TextComponent("DRINK ME"));
            itemstack1.getTag().putInt("HideFlags", 32);
            list.add(itemstack);
            list.add(itemstack1);
        }

        if (tab == CreativeModeTab.TAB_TOOLS || tab == null)
        {
            ItemStack itemstack3 = (new ItemStack(Items.LEATHER_BOOTS)).setHoverName(new TranslatableComponent("vivecraft.item.jumpboots"));
            itemstack3.getTag().putBoolean("Unbreakable", true);
            itemstack3.getTag().putInt("HideFlags", 4);
            ItemStack itemstack4 = (new ItemStack(Items.SHEARS)).setHoverName(new TranslatableComponent("vivecraft.item.climbclaws"));
            itemstack4.getTag().putBoolean("Unbreakable", true);
            itemstack4.getTag().putInt("HideFlags", 4);
            ItemStack itemstack2 = (new ItemStack(Items.ENDER_EYE)).setHoverName(new TranslatableComponent("vivecraft.item.telescope"));
            itemstack2.getTag().putBoolean("Unbreakable", true);
            itemstack2.getTag().putInt("HideFlags", 4);
            list.add(itemstack2);
            list.add(itemstack3);
            list.add(itemstack4);
        }
    }

    public static void addCreativeSearch(String query, NonNullList<ItemStack> list)
    {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        addCreativeItems((CreativeModeTab)null, nonnulllist);

        for (ItemStack itemstack : nonnulllist)
        {
            if (query.isEmpty() || itemstack.getHoverName().toString().toLowerCase().contains(query.toLowerCase()))
            {
                list.add(itemstack);
            }
        }
    }

    public static void dummy(float f)
    {
    }
}
