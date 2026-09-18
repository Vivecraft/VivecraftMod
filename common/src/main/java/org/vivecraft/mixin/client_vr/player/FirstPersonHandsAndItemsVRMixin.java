package org.vivecraft.mixin.client_vr.player;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.render.renderstates.ArmsRenderState;
import org.vivecraft.client_vr.render.renderstates.FirstPersonHandsAdditions;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsVRMixin {

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;clear()V", ordinal = 0))
    private void vivecraft$VRhandItems(
        LocalPlayer player, float partialTicks, FirstPersonHandsAndItemsRenderState state, CallbackInfo ci,
        @Local(name = "mainHandDisplayContext") LocalRef<ItemDisplayContext> mainHandDisplayContext,
        @Local(name = "offHandDisplayContext") LocalRef<ItemDisplayContext> offHandDisplayContext)
    {
        if (VRState.VR_RUNNING) {
            FirstPersonHandsAdditions additions = ArmsRenderState.extractAdditions(state, player, partialTicks);

            mainHandDisplayContext.set(additions.mainHandItemDisplayContext);
            offHandDisplayContext.set(additions.offHandItemDisplayContext);
        }
    }
}
