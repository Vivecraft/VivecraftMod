package org.vivecraft.mixin.client_vr.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.SpectatorGuiExtension;

import javax.annotation.Nullable;

@Mixin(SpectatorGui.class)
public abstract class SpectatorGuiVRMixin implements SpectatorGuiExtension {
    @Shadow
    private long lastSelectionTime;

    @Shadow
    @Nullable
    private SpectatorMenu menu;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void onHotbarSelected(int slot);

    @Shadow
    @Final
    private static ResourceLocation WIDGETS_LOCATION;

    @Override
    @Unique
    public void vivecraft$showMenu() {
        this.lastSelectionTime = Util.getMillis();
        if (this.menu == null) {
            this.menu = new SpectatorMenu((SpectatorMenuListener) this);
        }
    }

    @Override
    @Unique
    public void vivecraft$selectAndActivateSlot(int slot) {
        boolean doubleClick = this.menu == null || this.menu.getSelectedSlot() != slot;

        this.onHotbarSelected(slot);
        if (doubleClick) {
            // click a second time to actually select the item
            this.onHotbarSelected(slot);
        }
    }

    @Inject(method = "renderPage", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderColor(FFFF)V", ordinal = 1))
    private void vivecraft$hotbarContextIndicator(
        CallbackInfo ci, @Local(argsOnly = true) PoseStack poseStack)
    {
        if (VRState.VR_RUNNING && ClientDataHolderVR.getInstance().hotbarModule.hotbar >= 0 &&
            ClientDataHolderVR.getInstance().hotbarModule.hotbar < 9 &&
            ClientDataHolderVR.getInstance().interactTracker.isActive(this.minecraft.player))
        {
            int middle = this.minecraft.getWindow().getGuiScaledWidth() / 2;
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 1.0F);
            GuiComponent.blit(poseStack,
                middle - 91 - 1 + ClientDataHolderVR.getInstance().hotbarModule.hotbar * 20,
                this.minecraft.getWindow().getGuiScaledHeight() - 22 - 1, 0, 22, 24, 22);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @ModifyExpressionValue(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/spectator/SpectatorMenu;getSelectedItem()Lnet/minecraft/client/gui/spectator/SpectatorMenuItem;"))
    private SpectatorMenuItem vivecraft$hotbarContextText(SpectatorMenuItem original) {
        if (VRState.VR_RUNNING && ClientDataHolderVR.getInstance().hotbarModule.hotbar >= 0 &&
            ClientDataHolderVR.getInstance().hotbarModule.hotbar < 9 &&
            ClientDataHolderVR.getInstance().interactTracker.isActive(this.minecraft.player))
        {
            return this.menu.getItem(ClientDataHolderVR.getInstance().hotbarModule.hotbar);
        } else {
            return original;
        }
    }
}
