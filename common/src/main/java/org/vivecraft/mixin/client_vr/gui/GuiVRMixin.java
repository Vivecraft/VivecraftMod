package org.vivecraft.mixin.client_vr.gui;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import static org.vivecraft.client_vr.VRState.*;

@Mixin(net.minecraft.client.gui.Gui.class)
public abstract class GuiVRMixin implements org.vivecraft.client_vr.extensions.GuiExtension {

    @Unique
    public boolean vivecraft$showPlayerList;
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;

    @Final
    @Shadow
    private static ResourceLocation CROSSHAIR_SPRITE;
    @Final
    @Shadow
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
    public void vivecraft$cancelRenderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (RenderPassType.isGuiOnly()) {
            ci.cancel();
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I"), method = "render")
    public int vivecraft$noSleepOverlay(LocalPlayer instance) {
        return vrRunning ? 0 : instance.getSleepTimer();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z"), method = "render")
    public boolean vivecraft$toggleableTabList(KeyMapping instance) {
        return instance.isDown() || this.vivecraft$showPlayerList;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1, shift = Shift.AFTER), method = "renderHotbar")
    public void vivecraft$hotbarContext(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning && dh.interactTracker.hotbar >= 0 && dh.interactTracker.hotbar < 9 && this.getCameraPlayer().getInventory().selected != dh.interactTracker.hotbar && dh.interactTracker.isActive()) {
            int i = this.screenWidth / 2;
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 1.0F);
            guiGraphics.blitSprite(HOTBAR_SELECTION_SPRITE, i - 91 - 1 + dh.interactTracker.hotbar * 20, this.screenHeight - 22 - 1, 24, 23);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0), method = "renderHotbar")
    public boolean vivecraft$slotSwap(ItemStack instance) {
        return !(!instance.isEmpty() || (vrRunning && dh.vrSettings.vrTouchHotbar));
    }

    @Inject(at = @At("HEAD"), method = "renderHotbar", cancellable = true)
    public void vivecraft$notHotbarOnScreens(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning && mc.screen != null) {
            ci.cancel();
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 2, shift = Shift.BEFORE), method = "renderHotbar")
    public void vivecraft$renderVRHotbarLeft(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning && dh.interactTracker.hotbar == 9 && dh.interactTracker.isActive()) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 2, shift = Shift.AFTER), method = "renderHotbar")
    public void vivecraft$renderVRHotbarLeftReset(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning && dh.interactTracker.hotbar == 9) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 3, shift = Shift.BEFORE), method = "renderHotbar")
    public void vivecraft$renderVRHotbarRight(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning && dh.interactTracker.hotbar == 9 && dh.interactTracker.isActive()) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 3, shift = Shift.AFTER), method = "renderHotbar")
    public void vivecraft$renderVRHotbarRightReset(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning && dh.interactTracker.hotbar == 9) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    // do remap because of forge
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"), method = "renderHotbar")
    public void vivecraft$renderVive(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (vrRunning) {
            this.vivecraft$renderViveHudIcons(guiGraphics);
        }
    }

    @Unique
    private void vivecraft$renderViveHudIcons(GuiGraphics guiGraphics) {
        if (mc.getCameraEntity() instanceof Player player) {
            int k = 0;
            MobEffect mobeffect = null;

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
            if (dh.crawlTracker.crawling) {
                k = -2;
            }

            int x = mc.getWindow().getGuiScaledWidth() / 2 - 109;
            int y = mc.getWindow().getGuiScaledHeight() - 39;

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
                TextureAtlasSprite textureatlassprite = mc.getMobEffectTextures().get(mobeffect);
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
        TextureAtlasSprite crosshairSprite = mc.getGuiSprites().getSprite(CROSSHAIR_SPRITE);
        RenderSystem.setShaderTexture(0, crosshairSprite.atlasLocation());
        float f = 16.0F * dh.vrSettings.menuCrosshairScale;
        RenderSystem.blendFuncSeparate(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ZERO, SourceFactor.ONE, DestFactor.ONE);
        this.vivecraft$drawCentredTexturedModalRect(mouseX, mouseY, f, f, crosshairSprite.getU0(), crosshairSprite.getV0(), crosshairSprite.getU1(), crosshairSprite.getV1());
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    }

    @Unique
    public void vivecraft$drawCentredTexturedModalRect(int centreX, int centreY, float width, float height, float uMin, float vMin, float uMax, float vMax) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
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
