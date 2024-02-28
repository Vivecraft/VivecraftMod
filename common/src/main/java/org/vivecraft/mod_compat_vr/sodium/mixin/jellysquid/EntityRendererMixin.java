package org.vivecraft.mod_compat_vr.sodium.mixin.jellysquid;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.mod_compat_vr.sodium.extensions.ModelCuboidExtension;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer")
public class EntityRendererMixin {
    @Shadow(remap = false)
    private static void buildVertexTexCoord(Vector2f[] uvs, float u1, float v1, float u2, float v2) {
    }

    @Shadow(remap = false)
    @Final
    private static Vector2f[][] VERTEX_TEXTURES;

    @Inject(at = @At("TAIL"), method = "prepareVertices", remap = false)
    private static void vivecraft$overrideVrHands(PoseStack.Pose matrices, ModelCuboid cuboid, CallbackInfo ci) {
        float[][] overrides = ((ModelCuboidExtension) cuboid).vivecraft$getOverrides();
        if (overrides != null) {
            for (int i = 0; i < overrides.length; i++) {
                if (overrides[i][0] > 0F) {
                    buildVertexTexCoord(VERTEX_TEXTURES[i], overrides[i][1], overrides[i][2], overrides[i][3], overrides[i][4]);
                }
            }
        }
    }
}
