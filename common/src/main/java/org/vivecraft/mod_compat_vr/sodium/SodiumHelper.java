package org.vivecraft.mod_compat_vr.sodium;

import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.vivecraft.client.Xplat;

public class SodiumHelper {

    public static boolean isLoaded() {
        return Xplat.isModLoaded("sodium") || Xplat.isModLoaded("rubidium");
    }

    public static boolean hasIssuesWithParallelBlockBuilding() {
        try {
            Class.forName("me.jellysquid.mods.sodium.client.render.immediate.model.BakedModelEncoder");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static void markTextureAsActive(TextureAtlasSprite sprite) {
        SpriteUtil.markSpriteActive(sprite);
    }

// NotFixed
//    public static void vignette(boolean b) {
//        SodiumClientMod.options().quality.enableVignette = b;
//    }
}
