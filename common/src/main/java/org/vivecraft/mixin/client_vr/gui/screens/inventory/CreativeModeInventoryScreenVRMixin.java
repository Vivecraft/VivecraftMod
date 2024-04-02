package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
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
public abstract class CreativeModeInventoryScreenVRMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow
    private EditBox searchBox;

    @Shadow
    private static CreativeModeTab selectedTab;

    public CreativeModeInventoryScreenVRMixin(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;scrollOffs:F", shift = At.Shift.BEFORE), method = "refreshSearchResults")
    public void vivecraft$search(CallbackInfo ci) {
        // only add to actual search
        if (selectedTab == null || selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            vivecraft$addCreativeSearch(this.searchBox.getValue(), this.menu.items);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z", ordinal = 1, shift = At.Shift.AFTER), method = "selectTab")
    public void vivecraft$fill(CreativeModeTab creativeModeTab, CallbackInfo ci) {
        vivecraft$addCreativeItems(creativeModeTab, this.menu.items);
    }

    @Unique
    private void vivecraft$addCreativeItems(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (tab == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FOOD_AND_DRINKS) || tab == null) {
            ItemStack pie = new ItemStack(Items.PUMPKIN_PIE);
            pie.set(DataComponents.CUSTOM_NAME, Component.literal("EAT ME"));

            ItemStack drink = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            drink.set(DataComponents.CUSTOM_NAME, Component.literal("DRINK ME"));

            list.add(pie);
            list.add(drink);
        }

        if (tab == BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.TOOLS_AND_UTILITIES) || tab == null) {
            ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
            boots.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback("vivecraft.item.jumpboots", "Jump Boots"));
            boots.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
            boots.set(DataComponents.DYED_COLOR, new DyedItemColor(9233775, false));

            ItemStack claws = new ItemStack(Items.SHEARS);
            claws.set(DataComponents.CUSTOM_NAME, Component.translatableWithFallback("vivecraft.item.climbclaws", "Climb Claws"));
            claws.set(DataComponents.UNBREAKABLE, new Unbreakable(false));

            list.add(boots);
            list.add(claws);
        }
    }

    @Unique
    private void vivecraft$addCreativeSearch(String query, NonNullList<ItemStack> list) {
        NonNullList<ItemStack> nonnulllist = NonNullList.create();
        vivecraft$addCreativeItems(null, nonnulllist);

        for (ItemStack itemstack : nonnulllist) {
            if (query.isEmpty() || itemstack.getHoverName().toString().toLowerCase().contains(query.toLowerCase())) {
                list.add(itemstack);
            }
        }
    }
}
