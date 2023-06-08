package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenVRMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow
    private EditBox searchBox;

    public CreativeModeInventoryScreenVRMixin(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;scrollOffs:F", shift = At.Shift.BEFORE), method = "refreshSearchResults")
    public void search(CallbackInfo ci) {
        addCreativeSearch(this.searchBox.getValue(), this.menu.items);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z", ordinal = 1), method = "selectTab")
    public void fill(CreativeModeTab creativeModeTab, CallbackInfo ci) {
        addCreativeItems(creativeModeTab, this.menu.items);
    }

    @Unique
    private void addCreativeItems(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (tab == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FOOD_AND_DRINKS) || tab == null) {
            ItemStack itemstack = (new ItemStack(Items.PUMPKIN_PIE)).setHoverName(Component.literal("EAT ME"));
            ItemStack itemstack1 = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER).setHoverName(Component.literal("DRINK ME"));
            itemstack1.getTag().putInt("HideFlags", 32);
            list.add(itemstack);
            list.add(itemstack1);
        }

        if (tab == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.TOOLS_AND_UTILITIES) || tab == null) {
            ItemStack itemstack3 = (new ItemStack(Items.LEATHER_BOOTS)).setHoverName(Component.translatable("vivecraft.item.jumpboots"));
            itemstack3.getTag().putBoolean("Unbreakable", true);
            itemstack3.getTag().putInt("HideFlags", 4);
            ItemStack itemstack4 = (new ItemStack(Items.SHEARS)).setHoverName(Component.translatable("vivecraft.item.climbclaws"));
            itemstack4.getTag().putBoolean("Unbreakable", true);
            itemstack4.getTag().putInt("HideFlags", 4);
            list.add(itemstack3);
            list.add(itemstack4);
        }
    }

    @Unique
    private void addCreativeSearch(String query, NonNullList<ItemStack> list) {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        addCreativeItems((CreativeModeTab)null, nonnulllist);

        for (ItemStack itemstack : nonnulllist) {
            if (query.isEmpty() || itemstack.getHoverName().toString().toLowerCase().contains(query.toLowerCase())) {
                list.add(itemstack);
            }
        }
    }

}
