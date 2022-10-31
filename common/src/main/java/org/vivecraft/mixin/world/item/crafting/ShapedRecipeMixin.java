package org.vivecraft.mixin.world.item.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Inject(method = "itemFromJson", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;getOptional(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void getVivecraftVanillaItemInject(JsonObject jsonObject, CallbackInfoReturnable<Item> cir, String resourceLocation){
        if (resourceLocation.startsWith("vivecraft")) {
            cir.setReturnValue(getVivecraftVanillaItem(jsonObject, resourceLocation));
        }
    }

    private static Item getVivecraftVanillaItem(JsonObject jsonObject, String resourceLocation){
        String vanillaItem = GsonHelper.getAsString(jsonObject, "vanillaitem");
        return Registry.ITEM.getOptional(new ResourceLocation(vanillaItem)).orElseThrow(() -> new JsonSyntaxException("Unknown item '" + vanillaItem + "'"));
    }

    @Inject(method = "itemStackFromJson", at = @At("HEAD"), cancellable = true)
    private static void customizeVanillaItemStackFabric(JsonObject jsonObject, CallbackInfoReturnable<ItemStack> cir){
        if (GsonHelper.getAsString(jsonObject, "item").startsWith("vivecraft")) {
            Item vanillaItem = getVivecraftVanillaItem(jsonObject, GsonHelper.getAsString(jsonObject, "item"));
            cir.setReturnValue(customizeVanillaItemStack(jsonObject, vanillaItem));
        }
    }

    private static ItemStack customizeVanillaItemStack(JsonObject jsonObject, Item vanillaItem){
        if (jsonObject.has("data")) {
            throw new JsonParseException("Disallowed data tag found");
        } else {
            int i = GsonHelper.getAsInt(jsonObject, "count", 1);
            if (i < 1) {
                throw new JsonSyntaxException("Invalid output count: " + i);
            } else {
                ItemStack itemStack = new ItemStack(vanillaItem, i);
                itemStack.setHoverName(Component.translatable(jsonObject.get("name").getAsString()));
                itemStack.getOrCreateTag().putBoolean("Unbreakable", GsonHelper.getAsBoolean(jsonObject, "unbreakable", false));
                itemStack.getOrCreateTag().putInt("HideFlags", GsonHelper.getAsInt(jsonObject, "hideflags", 0));
                return itemStack;
            }
        }
    }

}
