package org.vivecraft.mod_compat_vr.sodium;

import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.settings.VRSettings;

import java.lang.reflect.Field;

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


    private static boolean hasModelCuboid;
    private static boolean checkedForModelCuboid;
    private static Field sodium$cuboids;

    public static void copyModelCuboidUV(ModelPart source, ModelPart dest, int sourcePoly, int destPoly) {
        if (!checkedForModelCuboid) {
            checkedForModelCuboid = true;
            try {
                Class.forName("me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid");
                sodium$cuboids = ModelPart.class.getDeclaredField("sodium$cuboids");
                sodium$cuboids.setAccessible(true);
                hasModelCuboid = true;
            } catch (ClassNotFoundException ignored) {
                return;
            } catch (NoSuchFieldException e) {
                VRSettings.logger.error("sodium version has ModelCuboids, but field was not found. VR hands will probably look wrong");
                return;
            }
        }
        if (hasModelCuboid) {
            try {
                ModelCuboid.Quad sourceQuad = ((ModelCuboid[]) sodium$cuboids.get(source))[0].quads[sourcePoly];
                ModelCuboid.Quad destQuad = ((ModelCuboid[]) sodium$cuboids.get(dest))[0].quads[destPoly];

                for (int i = 0; i < sourceQuad.textures.length; i++) {
                    destQuad.textures[i].x = sourceQuad.textures[i].x;
                    destQuad.textures[i].y = sourceQuad.textures[i].y;
                }
            } catch (IllegalAccessException | ClassCastException ignored) {
                VRSettings.logger.error("sodium version has ModelCuboids, but field has wrong type. VR hands will probably look wrong");
                hasModelCuboid = false;
            }
        }
    }
}
