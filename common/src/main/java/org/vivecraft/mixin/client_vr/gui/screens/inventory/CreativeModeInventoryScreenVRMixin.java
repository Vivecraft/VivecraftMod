package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.Unbreakable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenVRMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow
    private EditBox searchBox;

    @Shadow
    private static CreativeModeTab selectedTab;

    public CreativeModeInventoryScreenVRMixin(
        CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
    }

    @Inject(method = "refreshSearchResults", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;scrollOffs:F"))
    private void vivecraft$addVivecraftItemsSearch(CallbackInfo ci) {
        // only add to actual search
        if (selectedTab == null || selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            vivecraft$addCreativeSearch(this.searchBox.getValue(), this.menu.items);
        }
    }

    @Inject(method = "selectTab", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z", ordinal = 1, shift = At.Shift.AFTER))
    private void vivecraft$addVivecraftItemsCategory(CreativeModeTab tab, CallbackInfo ci) {
        vivecraft$addCreativeItems(tab, this.menu.items);
    }

    @Unique
    private void vivecraft$addCreativeItems(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (tab == BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(CreativeModeTabs.FOOD_AND_DRINKS) ||
            tab == null)
        {
            ItemStack eatMeCake = new ItemStack(Items.PUMPKIN_PIE);
            eatMeCake.set(DataComponents.CUSTOM_NAME, Component.literal("EAT ME"));

            ItemStack drinkMePotion = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            drinkMePotion.set(DataComponents.CUSTOM_NAME, Component.literal("DRINK ME"));

            items.add(eatMeCake);
            items.add(drinkMePotion);
        }

        if (tab == BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(CreativeModeTabs.TOOLS_AND_UTILITIES) ||
            tab == null)
        {
            ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
            boots.set(DataComponents.CUSTOM_NAME,
                Component.translatableWithFallback("vivecraft.item.jumpboots", "Jump Boots"));
            boots.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
            boots.set(DataComponents.DYED_COLOR, new DyedItemColor(0x8CE56F, false));

            ItemStack claws = new ItemStack(Items.SHEARS);
            claws.set(DataComponents.CUSTOM_NAME,
                Component.translatableWithFallback("vivecraft.item.climbclaws", "Climb Claws"));
            claws.set(DataComponents.UNBREAKABLE, new Unbreakable(false));

            items.add(boots);
            items.add(claws);
        }
    }

    @Unique
    private void vivecraft$addCreativeSearch(String query, NonNullList<ItemStack> itmes) {
        NonNullList<ItemStack> vivecraftItems = NonNullList.create();
        vivecraft$addCreativeItems(null, vivecraftItems);

        for (ItemStack item : vivecraftItems) {
            if (query.isEmpty() || item.getHoverName().toString().toLowerCase().contains(query.toLowerCase())) {
                itmes.add(item);
            }
        }
    }
}
