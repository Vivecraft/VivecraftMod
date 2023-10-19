package org.vivecraft.mixin.accessor.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {

    @Accessor
    void setMainRenderTarget(RenderTarget target);

    // for quicktorch
    @Invoker
    void callStartUseItem();

    // for pausemenu buttons
    @Invoker
    boolean callIsMultiplayerServer();
}
