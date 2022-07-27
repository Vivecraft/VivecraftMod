package org.vivecraft.asm;

import org.vivecraft.api.VRData;

import com.example.vivecraftfabric.DataHolder;

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

    public static float itemRayTracePitch(Player player, float orig)
    {
        if (player instanceof LocalPlayer)
        {
            VRData.VRDevicePose vrdata$vrdevicepose = DataHolder.getInstance().vrPlayer.vrdata_world_pre.getController(0);
            Vec3 vec3 = vrdata$vrdevicepose.getDirection();
            return (float)Math.toDegrees(Math.asin(-vec3.y / vec3.length()));
        }
        else
        {
            return orig;
        }
    }

    public static float itemRayTraceYaw(Player player, float orig)
    {
        if (player instanceof LocalPlayer)
        {
            VRData.VRDevicePose vrdata$vrdevicepose = DataHolder.getInstance().vrPlayer.vrdata_world_pre.getController(0);
            Vec3 vec3 = vrdata$vrdevicepose.getDirection();
            return (float)Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
        }
        else
        {
            return orig;
        }
    }

    public static Vec3 itemRayTracePos(Player player, Vec3 orig)
    {
        if (player instanceof LocalPlayer)
        {
            VRData.VRDevicePose vrdata$vrdevicepose = DataHolder.getInstance().vrPlayer.vrdata_world_pre.getController(0);
            return vrdata$vrdevicepose.getPosition();
        }
        else
        {
            return orig;
        }
    }

    public static void dummy(float f)
    {
    }
}
