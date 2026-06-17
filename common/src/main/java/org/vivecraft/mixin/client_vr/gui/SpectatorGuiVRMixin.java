package org.vivecraft.mixin.client_vr.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
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

@Mixin(SpectatorGui.class)
public abstract class SpectatorGuiVRMixin implements SpectatorGuiExtension {
    @Shadow
    private long lastSelectionTime;

    @Shadow
    @Nullable
    private SpectatorMenu menu;

    @Shadow
    @Final
    private static Identifier HOTBAR_SELECTION_SPRITE;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    public abstract void onHotbarSelected(int slot);

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

    @Inject(method = "extractPage", at = @At(value = "TAIL"))
    private void vivecraft$hotbarContextIndicator(
        CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics)
    {
        if (VRState.VR_RUNNING && ClientDataHolderVR.getInstance().hotbarModule.hotbar >= 0 &&
            ClientDataHolderVR.getInstance().hotbarModule.hotbar < 9 &&
            ClientDataHolderVR.getInstance().interactTracker.isActive(this.minecraft.player))
        {
            int middle = graphics.guiWidth() / 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE,
                middle - 91 - 1 + ClientDataHolderVR.getInstance().hotbarModule.hotbar * 20,
                graphics.guiHeight() - 22 - 1, 24, 23, 0xFF00FF00);
        }
    }

    @ModifyExpressionValue(method = "extractAction", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/spectator/SpectatorMenu;getSelectedItem()Lnet/minecraft/client/gui/spectator/SpectatorMenuItem;"))
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
