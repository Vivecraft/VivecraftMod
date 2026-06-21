package org.vivecraft.mixin.client_vr.gui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(Gui.class)
public abstract class GuiVRMixin implements GuiExtension {

    @Unique
    public boolean vivecraft$showPlayerList;

    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    @Final
    private static Identifier HOTBAR_SELECTION_SPRITE;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true)
    private void vivecraft$cancelVignette(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void vivecraft$cancelTextureOverlay(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true)
    private void vivecraft$cancelPortalOverlay(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    private void vivecraft$cancelSpyglassOverlay(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true)
    private void vivecraft$cancelCrosshair(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractSleepOverlay", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noSleepOverlay(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noConfusionOverlay(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "extractTabList", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean vivecraft$toggleableTabList(boolean keyDown) {
        return keyDown || this.vivecraft$showPlayerList;
    }

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noHotbarOnScreens(CallbackInfo ci) {
        if (VRState.VR_RUNNING && this.minecraft.screen != null) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/HumanoidArm;getOpposite()Lnet/minecraft/world/entity/HumanoidArm;"))
    private HumanoidArm vivecraft$offhandSlotSide(HumanoidArm instance, Operation<HumanoidArm> original) {
        if (!VRState.VR_RUNNING) {
            return original.call(instance);
        } else {
            // show the offhand slot on the right when using reverse hands
            return ClientDataHolderVR.getInstance().vrSettings.reverseHands ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        }
    }

    @Inject(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1, shift = At.Shift.AFTER))
    private void vivecraft$hotbarContextIndicator(
        CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics)
    {
        if (VRState.VR_RUNNING && ClientDataHolderVR.getInstance().hotbarModule.hotbar >= 0 &&
            ClientDataHolderVR.getInstance().hotbarModule.hotbar < 9 &&
            this.getCameraPlayer().getInventory().getSelectedSlot() !=
                ClientDataHolderVR.getInstance().hotbarModule.hotbar &&
            ClientDataHolderVR.getInstance().interactTracker.isActive(this.minecraft.player))
        {
            int middle = graphics.guiWidth() / 2;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE,
                middle - 91 - 1 + ClientDataHolderVR.getInstance().hotbarModule.hotbar * 20,
                graphics.guiHeight() - 22 - 1, 24, 23, 0xFF00FF00);
        }
    }


    @ModifyExpressionValue(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0))
    private boolean vivecraft$offhandSlotAlwaysVisible(boolean offhandEmpty) {
        // the result is inverted, so we need to invert ours as well
        return offhandEmpty && !(VRState.VR_RUNNING && ClientDataHolderVR.getInstance().vrSettings.vrTouchHotbar &&
            !ClientDataHolderVR.getInstance().vrSettings.seated
        );
    }

    @WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 2))
    private void vivecraft$renderVRHotbarLeftIndicator(
        GuiGraphicsExtractor instance, RenderPipeline renderPipeline, Identifier sprite, int x,
        int y, int width, int height, Operation<Void> original)
    {
        vivecraft$renderColoredIcon(instance, renderPipeline, sprite, x, y, width, height, original);
    }

    @WrapOperation(method = "extractItemHotbar", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 3))
    private void vivecraft$renderVRHotbarRightIndicator(
        GuiGraphicsExtractor instance, RenderPipeline renderPipeline, Identifier sprite, int x,
        int y, int width, int height, Operation<Void> original)
    {
        vivecraft$renderColoredIcon(instance, renderPipeline, sprite, x, y, width, height, original);
    }

    @Unique
    private void vivecraft$renderColoredIcon(
        GuiGraphicsExtractor instance, RenderPipeline renderPipeline, Identifier sprite, int x,
        int y, int width, int height, Operation<Void> original)
    {
        boolean changeColor =
            VRState.VR_RUNNING && ClientDataHolderVR.getInstance().hotbarModule.hotbar == 9 &&
                ClientDataHolderVR.getInstance().interactTracker.isActive(this.minecraft.player);

        if (changeColor) {
            instance.blitSprite(renderPipeline, sprite, x, y, width, height, 0xFF0000FF);
        } else {
            original.call(instance, renderPipeline, sprite, x, y, width, height);
        }
    }

    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    private void vivecraft$renderViveIcons(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics) {
        if (VRState.VR_RUNNING) {
            this.vivecraft$renderViveHudIcons(graphics);
        }
    }

    /**
     * renders the vivecraft status icons above the hotbar
     *
     * @param graphics GuiGraphicsExtractor to render with
     */
    @Unique
    private void vivecraft$renderViveHudIcons(GuiGraphicsExtractor graphics) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            int icon = 0;
            Holder<MobEffect> mobeffect = null;

            if (player.isSprinting()) {
                mobeffect = MobEffects.SPEED;
            }

            if (player.isVisuallySwimming()) {
                mobeffect = MobEffects.DOLPHINS_GRACE;
            }

            if (player.isShiftKeyDown()) {
                mobeffect = MobEffects.BLINDNESS;
            }

            if (player.isFallFlying()) {
                icon = -1;
            }
            if (ClientDataHolderVR.getInstance().crawlTracker.crawling) {
                icon = -2;
            }

            int x = this.minecraft.getWindow().getGuiScaledWidth() / 2 - 109;
            int y = this.minecraft.getWindow().getGuiScaledHeight() - 39;

            if (icon == -1) {
                graphics.fakeItem(new ItemStack(Items.ELYTRA), x, y);
                mobeffect = null;
            } else if (icon == -2) {
                int x2 = x;
                if (player.isShiftKeyDown()) {
                    x2 -= 19;
                } else {
                    mobeffect = null;
                }
                graphics.fakeItem(new ItemStack(Items.RABBIT_FOOT), x2, y);
            }
            if (mobeffect != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(mobeffect), x, y, 18, 18);
            }
        }
    }

    @Override
    @Unique
    public boolean vivecraft$getShowPlayerList() {
        return this.vivecraft$showPlayerList;
    }

    @Override
    @Unique
    public void vivecraft$setShowPlayerList(boolean showPlayerList) {
        this.vivecraft$showPlayerList = showPlayerList;
    }
}
