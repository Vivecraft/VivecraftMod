package org.vivecraft.crafting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class VivecraftCrafting extends ShapedRecipe {

    public static final Serializer SERIALIZER = new Serializer();

    public VivecraftCrafting(ResourceLocation resourceLocation, String string, int i, int j, NonNullList<Ingredient> nonNullList, ItemStack itemStack) {
        super(resourceLocation, string, i, j, nonNullList, itemStack);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    static NonNullList<Ingredient> dissolvePattern(String[] strings, Map<String, Ingredient> map, int i, int j) {
        NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);
        HashSet<String> set = Sets.newHashSet(map.keySet());
        set.remove(" ");
        for (int k = 0; k < strings.length; ++k) {
            for (int l = 0; l < strings[k].length(); ++l) {
                String string = strings[k].substring(l, l + 1);
                Ingredient ingredient = map.get(string);
                if (ingredient == null) {
                    throw new JsonSyntaxException("Pattern references symbol '" + string + "' but it's not defined in the key");
                }
                set.remove(string);
                nonNullList.set(l + i * k, ingredient);
            }
        }
        if (!set.isEmpty()) {
            throw new JsonSyntaxException("Key defines symbols that aren't used in pattern: " + set);
        }
        return nonNullList;
    }


    static String[] shrink(String ... strings) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;
        for (int m = 0; m < strings.length; ++m) {
            String string = strings[m];
            i = Math.min(i, VivecraftCrafting.firstNonSpace(string));
            int n = VivecraftCrafting.lastNonSpace(string);
            j = Math.max(j, n);
            if (n < 0) {
                if (k == m) {
                    ++k;
                }
                ++l;
                continue;
            }
            l = 0;
        }
        if (strings.length == l) {
            return new String[0];
        }
        String[] strings2 = new String[strings.length - l - k];
        for (int o = 0; o < strings2.length; ++o) {
            strings2[o] = strings[o + k].substring(i, j + 1);
        }
        return strings2;
    }

    private static int firstNonSpace(String string) {
        int i;
        for (i = 0; i < string.length() && string.charAt(i) == ' '; ++i) {
        }
        return i;
    }

    private static int lastNonSpace(String string) {
        int i;
        for (i = string.length() - 1; i >= 0 && string.charAt(i) == ' '; --i) {
        }
        return i;
    }

    static String[] patternFromJson(JsonArray jsonArray) {
        String[] strings = new String[jsonArray.size()];
        if (strings.length > 3) {
            throw new JsonSyntaxException("Invalid pattern: too many rows, 3 is maximum");
        }
        if (strings.length == 0) {
            throw new JsonSyntaxException("Invalid pattern: empty pattern not allowed");
        }
        for (int i = 0; i < strings.length; ++i) {
            String string = GsonHelper.convertToString(jsonArray.get(i), "pattern[" + i + "]");
            if (string.length() > 3) {
                throw new JsonSyntaxException("Invalid pattern: too many columns, 3 is maximum");
            }
            if (i > 0 && strings[0].length() != string.length()) {
                throw new JsonSyntaxException("Invalid pattern: each row must be the same width");
            }
            strings[i] = string;
        }
        return strings;
    }

    static Map<String, Ingredient> keyFromJson(JsonObject jsonObject) {
        HashMap<String, Ingredient> map = Maps.newHashMap();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getKey().length() != 1) {
                throw new JsonSyntaxException("Invalid key entry: '" + entry.getKey() + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(entry.getKey())) {
                throw new JsonSyntaxException("Invalid key entry: ' ' is a reserved symbol.");
            }
            map.put(entry.getKey(), Ingredient.fromJson(entry.getValue()));
        }
        map.put(" ", Ingredient.EMPTY);
        return map;
    }

    public static class Serializer implements RecipeSerializer<VivecraftCrafting> {

        @Override
        public VivecraftCrafting fromJson(ResourceLocation resourceLocation, JsonObject jsonObject) {
            String string = GsonHelper.getAsString(jsonObject, "group", "");
            Map<String, Ingredient> map = VivecraftCrafting.keyFromJson(GsonHelper.getAsJsonObject(jsonObject, "key"));
            String[] strings = VivecraftCrafting.shrink(VivecraftCrafting.patternFromJson(GsonHelper.getAsJsonArray(jsonObject, "pattern")));
            int i = strings[0].length();
            int j = strings.length;
            NonNullList<Ingredient> nonNullList = VivecraftCrafting.dissolvePattern(strings, map, i, j);
            ItemStack itemStack = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(jsonObject, "result"));
            String name = jsonObject.get("name").getAsString();
            itemStack.setHoverName(Component.translatable(name));
            int flags = jsonObject.get("hideflags").getAsInt();
            boolean unbreakable = jsonObject.get("unbreakable").getAsBoolean();
            itemStack.getOrCreateTag().putBoolean("Unbreakable", unbreakable);
            itemStack.getOrCreateTag().putInt("HideFlags", flags);
            return new VivecraftCrafting(resourceLocation, string, i, j, nonNullList, itemStack);
        }

        @Override
        public VivecraftCrafting fromNetwork(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf) {
            int i = friendlyByteBuf.readVarInt();
            int j = friendlyByteBuf.readVarInt();
            String string = friendlyByteBuf.readUtf();
            NonNullList<Ingredient> nonNullList = NonNullList.withSize(i * j, Ingredient.EMPTY);
            for (int k = 0; k < nonNullList.size(); ++k) {
                nonNullList.set(k, Ingredient.fromNetwork(friendlyByteBuf));
            }
            ItemStack itemStack = friendlyByteBuf.readItem();
            return new VivecraftCrafting(resourceLocation, string, i, j, nonNullList, itemStack);
        }

        @Override
        public void toNetwork(FriendlyByteBuf friendlyByteBuf, VivecraftCrafting recipe) {
            friendlyByteBuf.writeVarInt(recipe.getWidth());
            friendlyByteBuf.writeVarInt(recipe.getHeight());
            friendlyByteBuf.writeUtf(recipe.getGroup());
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredient.toNetwork(friendlyByteBuf);
            }
            friendlyByteBuf.writeItem(recipe.getResultItem());
        }

        //Forge
        private ResourceLocation name = null;
        public Object setRegistryName(ResourceLocation arg){
            name = arg;
            return this;
        }

        public ResourceLocation getRegistryName(){
            return name;
        }

        public Class<Serializer> getRegistryType(){
            return Serializer.class;
        }
    }
}
