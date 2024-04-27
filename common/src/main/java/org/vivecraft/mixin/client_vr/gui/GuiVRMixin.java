package org.vivecraft.mixin.client_vr.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Final
    @Shadow
    public static ResourceLocation CROSSHAIR_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation HOTBAR_SELECTION_SPRITE;

    @Shadow
    protected abstract Player getCameraPlayer();

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    public void vivecraft$cancelRenderVignette(GuiGraphics guiGraphics, Entity entity, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            RenderSystem.enableDepthTest();
            ci.cancel();
        }
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void vivecraft$cancelRenderOverlay(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void vivecraft$cancelRenderPortalOverlay(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSpyglassOverlay", cancellable = true)
    public void vivecraft$cancelRenderSpyglassOverlay(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    public void vivecraft$cancelRenderCrosshair(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSleepOverlay", cancellable = true)
    public void vivecraft$noSleepOverlay(CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"), method = "renderTabList")
    public boolean vivecraft$toggleableTabList(KeyMapping instance) {
        return instance.isDown() || vivecraft$showPlayerList;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1, shift = At.Shift.AFTER), method = "renderItemHotbar")
    public void vivecraft$hotbarContext(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar >= 0 && ClientDataHolderVR.getInstance().interactTracker.hotbar < 9 && this.getCameraPlayer().getInventory().selected != ClientDataHolderVR.getInstance().interactTracker.hotbar && ClientDataHolderVR.getInstance().interactTracker.isActive(minecraft.player)) {
            int i = guiGraphics.guiWidth() / 2;
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 1.0F);
            guiGraphics.blitSprite(HOTBAR_SELECTION_SPRITE, i - 91 - 1 + ClientDataHolderVR.getInstance().interactTracker.hotbar * 20, guiGraphics.guiHeight() - 22 - 1, 24, 23);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0), method = "renderItemHotbar")
    public boolean vivecraft$slotSwap(ItemStack instance) {
        return !(!instance.isEmpty() || (VRState.vrRunning && ClientDataHolderVR.getInstance().vrSettings.vrTouchHotbar));
    }

    @Inject(at = @At("HEAD"), method = "renderItemHotbar", cancellable = true)
    public void vivecraft$notHotbarOnScreens(CallbackInfo ci) {
        if (VRState.vrRunning && minecraft.screen != null) {
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 2, shift = At.Shift.BEFORE), method = "renderItemHotbar")
    public void vivecraft$renderVRHotbarLeft(CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9 && ClientDataHolderVR.getInstance().interactTracker.isActive(minecraft.player)) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 2, shift = At.Shift.AFTER), method = "renderItemHotbar")
    public void vivecraft$renderVRHotbarLeftReset(CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 3, shift = At.Shift.BEFORE), method = "renderItemHotbar")
    public void vivecraft$renderVRHotbarRight(CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9 && ClientDataHolderVR.getInstance().interactTracker.isActive(minecraft.player)) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 3, shift = At.Shift.AFTER), method = "renderItemHotbar")
    public void vivecraft$renderVRHotbarRightReset(CallbackInfo ci) {
        if (VRState.vrRunning && ClientDataHolderVR.getInstance().interactTracker.hotbar == 9) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V", remap = false), method = "renderItemHotbar")
    public void vivecraft$renderVive(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (VRState.vrRunning) {
            this.vivecraft$renderViveHudIcons(guiGraphics);
        }
    }

    @Unique
    private void vivecraft$renderViveHudIcons(GuiGraphics guiGraphics) {
        if (this.minecraft.getCameraEntity() instanceof Player player) {
            int k = 0;
            Holder<MobEffect> mobeffect = null;

            if (player.isSprinting()) {
                mobeffect = MobEffects.MOVEMENT_SPEED;
            }

            if (player.isVisuallySwimming()) {
                mobeffect = MobEffects.DOLPHINS_GRACE;
            }

            if (player.isShiftKeyDown()) {
                mobeffect = MobEffects.BLINDNESS;
            }

            if (player.isFallFlying()) {
                k = -1;
            }
            if (ClientDataHolderVR.getInstance().crawlTracker.crawling) {
                k = -2;
            }

            int x = this.minecraft.getWindow().getGuiScaledWidth() / 2 - 109;
            int y = this.minecraft.getWindow().getGuiScaledHeight() - 39;

            if (k == -1) {
                guiGraphics.renderFakeItem(new ItemStack(Items.ELYTRA), x, y);
                mobeffect = null;
            } else if (k == -2) {
                int x2 = x;
                if (player.isShiftKeyDown()) {
                    x2 -= 19;
                } else {
                    mobeffect = null;
                }
                guiGraphics.renderFakeItem(new ItemStack(Items.RABBIT_FOOT), x2, y);
            }
            if (mobeffect != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getMobEffectTextures().get(mobeffect);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blit(x, y, 0, 18, 18, textureatlassprite);
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

    @Override
    @Unique
    public void vivecraft$drawMouseMenuQuad(int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        //uhhhh //RenderSystem.disableLighting();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        TextureAtlasSprite crosshairSprite = minecraft.getGuiSprites().getSprite(CROSSHAIR_SPRITE);
        RenderSystem.setShaderTexture(0, crosshairSprite.atlasLocation());
        float f = 16.0F * ClientDataHolderVR.getInstance().vrSettings.menuCrosshairScale;
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        this.vivecraft$drawCentredTexturedModalRect(mouseX, mouseY, f, f, crosshairSprite.getU0(), crosshairSprite.getV0(), crosshairSprite.getU1(), crosshairSprite.getV1());
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Unique
    public void vivecraft$drawCentredTexturedModalRect(int centreX, int centreY, float width, float height, float uMin, float vMin, float uMax, float vMax) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex((float) centreX - width / 2.0F, (float) centreY + height / 2.0F, 0)
            .uv(uMin, vMin).endVertex();
        bufferbuilder.vertex((float) centreX + width / 2.0F, (float) centreY + height / 2.0F, 0)
            .uv(uMin, vMax).endVertex();
        bufferbuilder.vertex((float) centreX + width / 2.0F, (float) centreY - height / 2.0F, 0)
            .uv(uMax, vMax).endVertex();
        bufferbuilder.vertex((float) centreX - width / 2.0F, (float) centreY - height / 2.0F, 0)
            .uv(uMax, vMin).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
    }
}
