package org.vivecraft.mixin.client.gui;

import org.vivecraft.ClientDataHolder;
import org.vivecraft.SodiumHelper;
import org.vivecraft.Xplat;
import org.vivecraft.extensions.GuiExtension;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiVRMixin extends GuiComponent implements GuiExtension {

    @Unique
    public boolean showPlayerList;
    @Shadow
    private int screenWidth;
    @Shadow
    private int screenHeight;
    @Final
    @Shadow
    private Minecraft minecraft;

    @Shadow
    protected abstract Player getCameraPlayer();

    //Moved to render for sodium
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVignette(Lnet/minecraft/world/entity/Entity;)V"), method = "render")
    public void noVignette(Gui instance, Entity entity) {
        if(Xplat.isModLoaded("sodium") || Xplat.isModLoaded("rubidium")) {
            SodiumHelper.vignette(false);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z"), method = "render")
    public boolean noFirstPerson(CameraType instance) {
        return false;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I"), method = "render")
    public int noFrozen(LocalPlayer instance) {
        return 0;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;hasEffect(Lnet/minecraft/world/effect/MobEffect;)Z"), method = "render")
    public boolean noConfusion(LocalPlayer instance, MobEffect mobEffect) {
        return true;
    }

//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "render")
//    public void noCrosshair(Gui instance, PoseStack poseStack) {
//        return ;
//    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;render(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"), method = "render")
    public void noTabList(PlayerTabOverlay instance, PoseStack poseStack, int i, Scoreboard scoreboard, Objective objective) {
        return ;
    }

    @Inject(at = @At("HEAD"), method = "renderCrosshair", cancellable = true)
    public void noRenderCrosshair(PoseStack poseStack, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 1, shift = At.Shift.AFTER), method = "renderHotbar")
    public void hotbarContext(float f, PoseStack poseStack, CallbackInfo ci) {
        int i = this.screenWidth / 2;
        if (ClientDataHolder.getInstance().interactTracker.hotbar >= 0 && ClientDataHolder.getInstance().interactTracker.hotbar < 9 && this.getCameraPlayer().getInventory().selected != ClientDataHolder.getInstance().interactTracker.hotbar) {
            RenderSystem.setShaderColor(0.0F, 1.0F, 0.0F, 1.0F);
            this.blit(poseStack, i - 91 - 1 + ClientDataHolder.getInstance().interactTracker.hotbar * 20, this.screenHeight - 22 - 1, 0, 22, 24, 22);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0), method = "renderHotbar")
    public boolean slotSwap(ItemStack instance) {
        return !(!instance.isEmpty() || ClientDataHolder.getInstance().vrSettings.vrTouchHotbar);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 2), method = "renderHotbar")
    public void renderVRHotbarLeft(Gui instance, PoseStack poseStack, int x, int y, int uOffset, int vOffset, int uWidth, int vWidth) {
        if (ClientDataHolder.getInstance().interactTracker.hotbar == 9) {
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
            this.blit(poseStack, x, y, uOffset, vOffset, uWidth, vWidth);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        else {
            this.blit(poseStack, x, y, uOffset, vOffset, uWidth, vWidth);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;blit(Lcom/mojang/blaze3d/vertex/PoseStack;IIIIII)V", ordinal = 3), method = "renderHotbar")
    public void renderVRHotbarRight(Gui instance, PoseStack poseStack, int x, int y, int uOffset, int vOffset, int uWidth, int vWidth) {
        if (ClientDataHolder.getInstance().interactTracker.hotbar == 9){
            RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
            this.blit(poseStack, x, y, uOffset, vOffset, uWidth, vWidth);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            this.blit(poseStack, x, y, uOffset, vOffset, uWidth, vWidth);
        }
    }

    // do remap because of forge
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V"), method = "renderHotbar")
    public void renderVive(float f, PoseStack poseStack, CallbackInfo ci){
        this.renderViveHudIcons(poseStack);
    }

    private void renderViveHudIcons(PoseStack matrixstack) {
        if (this.minecraft.getCameraEntity() instanceof Player) {
            int i = this.minecraft.getWindow().getGuiScaledWidth();
            int j = this.minecraft.getWindow().getGuiScaledHeight();
            Font font = this.minecraft.gui.getFont();
            Player player = (Player)this.minecraft.getCameraEntity();
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
            if (ClientDataHolder.getInstance().crawlTracker.crawling) {
                k = -2;
            }

            int l = this.minecraft.getWindow().getGuiScaledWidth() / 2 - 109;
            int i1 = this.minecraft.getWindow().getGuiScaledHeight() - 39;

            if (k == -1) {
                this.minecraft.getItemRenderer().renderGuiItem(new ItemStack(Items.ELYTRA), l, i1);
                mobeffect = null;
            }
            else if (k == -2) {
                if (player.isShiftKeyDown()) {
                    l -= 19;
                }
                else {
                    mobeffect = null;
                }
                this.minecraft.getItemRenderer().renderGuiItem(new ItemStack(Items.RABBIT_FOOT), l, i1);
            }
            if (mobeffect != null) {
                TextureAtlasSprite textureatlassprite = this.minecraft.getMobEffectTextures().get(mobeffect);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.setShaderTexture(0, textureatlassprite.atlas().location());
                GuiComponent.blit(matrixstack, l, i1, 0, 18, 18, textureatlassprite);
            }
        }
    }

    @Override
    public boolean getShowPlayerList() {
        return this.showPlayerList;
    }

    @Override
    public void setShowPlayerList(boolean showPlayerList) {
        this.showPlayerList = showPlayerList;
    }

    @Override
    public void drawMouseMenuQuad(int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        //uhhhh //RenderSystem.disableLighting();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, Screen.GUI_ICONS_LOCATION);
        float f = 16.0F * ClientDataHolder.getInstance().vrSettings.menuCrosshairScale;
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        this.drawCentredTexturedModalRect(mouseX, mouseY, f, f, 0, 0, 15, 15);
        RenderSystem.disableBlend();
    }

    public void drawCentredTexturedModalRect(int centreX, int centreY, float width, float height, int u, int v, int texWidth, int texHeight) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex((double)((float)centreX - width / 2.0F), (double)((float)centreY + height / 2.0F), (double)this.getBlitOffset()).uv((float)(u + 0) * f, (float)(v + texHeight) * f1).endVertex();
        bufferbuilder.vertex((double)((float)centreX + width / 2.0F), (double)((float)centreY + height / 2.0F), (double)this.getBlitOffset()).uv((float)(u + texWidth) * f, (float)(v + texHeight) * f1).endVertex();
        bufferbuilder.vertex((double)((float)centreX + width / 2.0F), (double)((float)centreY - height / 2.0F), (double)this.getBlitOffset()).uv((float)(u + texWidth) * f, (float)(v + 0) * f1).endVertex();bufferbuilder.vertex((double)((float)centreX - width / 2.0F), (double)((float)centreY - height / 2.0F), (double)this.getBlitOffset()).uv((float)(u + 0) * f, (float)(v + 0) * f1).endVertex();
        bufferbuilder.end();
        BufferUploader.end(bufferbuilder);
    }
}
