package org.vivecraft.mod_compat_vr.sodium;

import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector2f;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.sodium.extensions.ModelCuboidExtension;

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


    private static boolean hasModelCuboidQuads;
    private static boolean hasModelCuboidFloats;
    private static boolean checkedForModelCuboid;
    private static Field sodium$cuboids;
    private static Field cuboidQuads;
    private static Field cuboidQuadTextures;

    public static void copyModelCuboidUV(ModelPart source, ModelPart dest, int sourcePoly, int destPoly) {
        if (!checkedForModelCuboid) {
            checkedForModelCuboid = true;
            try {
                Class<?> cube = Class.forName("me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid");
                sodium$cuboids = ModelPart.class.getDeclaredField("sodium$cuboids");
                sodium$cuboids.setAccessible(true);
                try {
                    Class<?> cuboidQuad = Class.forName("me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid$Quad");
                    cuboidQuads = cube.getDeclaredField("quads");
                    cuboidQuadTextures = cuboidQuad.getDeclaredField("textures");
                    hasModelCuboidQuads = true;
                } catch (ClassNotFoundException noQuads) {
                    hasModelCuboidFloats = true;
                }
            } catch (ClassNotFoundException ignored) {
                return;
            } catch (NoSuchFieldException e) {
                VRSettings.logger.error("sodium version has ModelCuboids, but field was not found. VR hands will probably look wrong");
                return;
            }
        }
        if (hasModelCuboidQuads) {
            try {
                Object sourceQuad = ((Object[]) cuboidQuads.get(((ModelCuboid[]) sodium$cuboids.get(source))[0]))[sourcePoly];
                Object destQuad = ((Object[]) cuboidQuads.get(((ModelCuboid[]) sodium$cuboids.get(dest))[0]))[destPoly];

                Vector2f[] sourceTextures = (Vector2f[]) cuboidQuadTextures.get(sourceQuad);
                Vector2f[] destTextures = (Vector2f[]) cuboidQuadTextures.get(destQuad);

                for (int i = 0; i < sourceTextures.length; i++) {
                    destTextures[i].x = sourceTextures[i].x;
                    destTextures[i].y = sourceTextures[i].y;
                }
            } catch (IllegalAccessException | ClassCastException ignored) {
                VRSettings.logger.error("sodium version has ModelCuboids, but field has wrong type. VR hands will probably look wrong");
                hasModelCuboidQuads = false;
            }
        } else if (hasModelCuboidFloats) {
            try {
                ((ModelCuboidExtension) ((ModelCuboid[]) sodium$cuboids.get(dest))[0]).vivecraft$addOverrides(
                    mapDirection(destPoly),
                    mapDirection(sourcePoly),
                    ((ModelCuboid[]) sodium$cuboids.get(source))[0]
                );
            } catch (IllegalAccessException | ClassCastException ignored) {
                VRSettings.logger.error("sodium version has ModelCuboids, but field has wrong type. VR hands will probably look wrong");
                hasModelCuboidFloats = false;
            }
        }
    }

    private static int mapDirection(int old) {
        return switch (old) {
            default -> 4;
            case 1 -> 2;
            case 2 -> 0;
            case 3 -> 1;
            case 4 -> 3;
            case 5 -> 5;
        };
    }
}
