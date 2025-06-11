package org.vivecraft.mod_compat_vr.sereneseasons.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "sereneseasons.handler.season.SeasonColorHandlers")
public class SeasonColorHandlersMixin {

    @Shadow(remap = false)
    private static ColorResolver originalGrassColorResolver;
    @Shadow(remap = false)
    private static ColorResolver originalFoliageColorResolver;

    /**
     * menuworld fix
     */
    @Inject(method = "lambda$registerGrassAndFoliageColorHandlers$1", at = @At("HEAD"), remap = false, cancellable = true)
    private static void vivecraft$grassColor(Biome biome, double x, double y, CallbackInfoReturnable<Integer> cir) {
        if (Minecraft.getInstance().level == null) {
            cir.setReturnValue(originalGrassColorResolver.getColor(biome, x, y));
        }
    }

    /**
     * menuworld fix
     */
    @Inject(method = "lambda$registerGrassAndFoliageColorHandlers$3", at = @At("HEAD"), remap = false, cancellable = true)
    private static void vivecraft$foliageColor(Biome biome, double x, double y, CallbackInfoReturnable<Integer> cir) {
        if (Minecraft.getInstance().level == null) {
            cir.setReturnValue(originalFoliageColorResolver.getColor(biome, x, y));
        }
    }

    /**
     * menuworld fix
     */
    @Inject(method = "lambda$registerBirchColorHandler$4", at = @At("HEAD"), remap = false, cancellable = true)
    private static void vivecraft$birchColor(
        BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, int tintIndex,
        CallbackInfoReturnable<Integer> cir)
    {
        if (Minecraft.getInstance().level == null) {
            // this is now hardcoded since 1.21.4
            cir.setReturnValue(0xFF80A755);
        }
    }
}
