package org.vivecraft.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    // XR Camera
    @Accessor
    void setInitialized(boolean initialized);
    @Accessor
    BlockGetter getLevel();
    @Accessor
    void setLevel(BlockGetter level);
    @Accessor
    Entity getEntity();
    @Accessor
    void setEntity(Entity entity);
    @Accessor
    void setXRot(float xRot);
    @Accessor
    float getXRot();
    @Accessor
    void setYRot(float yRot);
    @Accessor
    float getYRot();

    // to set reset camera before tick
    @Invoker
    void callSetPosition(Vec3 v);
}
