package org.vivecraft.mixin.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderMan.class)
public abstract class EndermanMixin extends Monster {

    protected EndermanMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "isBeingStaredBy", at = @At(value = "CONSTANT", args = "doubleValue=0.025"))
    private double vivecraft$biggerViewCone(double original, @Share("hmdPos") LocalRef<Vec3> hmdPos) {
        // increase the view cone check from 1.4° to 5.7°, makes it easier to stop enderman,
        // since it's hard to know where the center of the view is
        return hmdPos.get() != null ? 0.1 : original;
    }
}
