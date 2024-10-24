package org.vivecraft.mixin.client_vr.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LocalPlayer_EntityVRMixin {

    @Shadow
    protected boolean wasTouchingWater;
    @Shadow
    protected Vec3 stuckSpeedMultiplier;

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract boolean isSilent();

    @Shadow
    protected abstract BlockPos getBlockPosBelowThatAffectsMyMovement();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract boolean onGround();

    @Shadow
    public abstract void setDeltaMovement(double x, double y, double z);

    @Shadow
    public abstract @Nullable Entity getVehicle();

    @Shadow
    public abstract void setPos(Vec3 pos);

    @Shadow
    public abstract void setOnGround(boolean onGround);

    @Shadow
    protected abstract float getBlockJumpFactor();

    @Shadow
    public abstract void setMaxUpStep(float maxUpStep);

    @Shadow
    public abstract Vec3 getDeltaMovement();

    @Shadow
    public void move(MoverType type, Vec3 pos) {}

    @Shadow
    public abstract boolean isPassenger();

    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getX();

    /**
     * dummy to be overridden in {@link LocalPlayerVRMixin}
     */
    @WrapOperation(method = "moveRelative", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"))
    protected Vec3 vivecraft$controllerMovement(Vec3 relative, float amount, float facing, Operation<Vec3> original) {
        return original.call(relative, amount, facing);
    }

    /**
     * dummy to be overridden in {@link LocalPlayerVRMixin}
     */
    @WrapMethod(method = "setPos(DDD)V")
    protected void vivecraft$wrapSetPos(double x, double y, double z, Operation<Void> original) {
        original.call(x, y, z);
    }

    /**
     * dummy to be overridden in {@link LocalPlayerVRMixin}
     */
    @Inject(method = {"absMoveTo(DDDFF)V", "moveTo(DDDFF)V"}, at = @At("TAIL"))
    protected void vivecraft$afterAbsMoveTo(CallbackInfo ci) {}
}
