package org.vivecraft.mixin.client_vr;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.ClientVRPlayers;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.gui.VivecraftClickEvent;
import org.vivecraft.client.gui.framework.screens.ChangeableParentScreen;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client.gui.screens.ServerVrChangesScreen;
import org.vivecraft.client.gui.screens.UpdateScreen;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client.utils.TextUtils;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.client_vr.extensions.WindowExtension;
import org.vivecraft.client_vr.gameplay.KeybindHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.render.MirrorNotification;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.render.helpers.RenderHelper;
import org.vivecraft.client_vr.render.helpers.ShaderHelper;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.common.network.packet.c2s.VRActivePayloadC2S;
import org.vivecraft.mod_compat_vr.ReplayHelper;
import org.vivecraft.mod_compat_vr.immersiveportals.ImmersivePortalsHelper;
import org.vivecraft.mod_compat_vr.shaders.ShadersHelper;

// inject late, to let other mods disable the hud rendering
@Mixin(value = Minecraft.class, priority = 1100)
public abstract class MinecraftVRMixin implements MinecraftExtension {

    // keeps track if an attack was initiated by pressing the attack key
    @Unique
    private boolean vivecraft$attackKeyDown;

    @Unique
    private CameraType vivecraft$lastCameraType;

    @Shadow
    @Final
    public Options options;

    @Shadow
    public Screen screen;

    @Shadow
    @Final
    private Window window;

    @Final
    @Shadow
    public GameRenderer gameRenderer;

    @Shadow
    public ClientLevel level;

    @Shadow
    public RenderTarget mainRenderTarget;

    @Shadow
    public LocalPlayer player;

    @Shadow
    @Final
    private EntityRenderDispatcher entityRenderDispatcher;

    @Shadow
    @Final
    public MouseHandler mouseHandler;

    @Shadow
    public abstract Entity getCameraEntity();

    @Shadow
    public abstract void setScreen(Screen guiScreen);

    @Shadow
    public abstract SoundManager getSoundManager();

    @Shadow
    @Final
    private DeltaTracker.Timer deltaTracker;

    @Shadow
    public HitResult hitResult;

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/server/packs/resources/ReloadableResourceManager"))
    private ReloadableResourceManager vivecraft$initVivecraft(
        PackType packType, Operation<ReloadableResourceManager> original)
    {
        RenderPassManager.INSTANCE = new RenderPassManager((MainTarget) this.mainRenderTarget);
        VRSettings.initSettings();
        new Thread(UpdateChecker::checkForUpdates, "VivecraftUpdateThread").start();
        ShadersHelper.registerPipelines();

        return original.call(packType);
    }

    @Inject(method = "onGameLoadFinished", at = @At("TAIL"))
    private void vivecraft$showGarbageCollectorScreen(CallbackInfo ci) {
        // set the Garbage collector screen here, when it got reset after loading, but don't set it when using quickplay, because it would be removed after loading has finished
        if (ClientDataHolderVR.getInstance().cachedScreen != null &&
            !(this.screen instanceof LevelLoadingScreen ||
                this.screen instanceof ConnectScreen
            ))
        {
            if (this.screen.getClass() != ClientDataHolderVR.getInstance().cachedScreen.getClass()) {
                if (ClientDataHolderVR.getInstance().cachedScreen instanceof ChangeableParentScreen child) {
                    child.setParent(this.screen);
                }
                setScreen(ClientDataHolderVR.getInstance().cachedScreen);
            }
            ClientDataHolderVR.getInstance().cachedScreen = null;
        }
    }

    @Inject(method = "destroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;hasDelayedCrash()Z"))
    private void vivecraft$destroyVR(CallbackInfo ci) {
        try {
            // the game crashed probably not because of us, so keep the vr choice
            VRState.destroyVR(false);
        } catch (Exception ignored) {}
    }

    @Inject(method = "renderFrame", at = @At("HEAD"))
    private void vivecraft$toggleVRState(CallbackInfo callback) {
        if (ClientDataHolderVR.getInstance().completelyDisabled) {
            VRState.VR_ENABLED = false;
        }
        if (VRState.VR_ENABLED) {
            VRState.initializeVR();
        } else if (VRState.VR_INITIALIZED) {
            // turn off VR if it was on before
            VRState.destroyVR(true);
            vivecraft$switchVRState(false);
        }
        if (!VRState.VR_INITIALIZED) {
            return;
        }
        boolean vrActive = !ClientDataHolderVR.getInstance().vrSettings.vrHotswitchingEnabled ||
            ClientDataHolderVR.getInstance().vr.isActive();
        if (VRState.VR_RUNNING != vrActive && (ClientNetworking.SERVER_ALLOWS_VR_SWITCHING || this.player == null)) {
            // switch vr in the menu, or when allowed by the server
            vivecraft$switchVRState(vrActive);
        }
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().frameIndex++;
            RenderPassManager.setGUIRenderPass();
            // reset camera position, if there is one, since it only gets set at the start of rendering, and the last renderpass can be anywhere
            if (this.gameRenderer != null && this.gameRenderer.getMainCamera() != null && this.level != null &&
                this.getCameraEntity() != null)
            {
                this.gameRenderer.getMainCamera().update(this.deltaTracker);
            }

            Profiler.get().push("VR Poll/VSync");
            ClientDataHolderVR.getInstance().vr.poll(ClientDataHolderVR.getInstance().frameIndex);
            Profiler.get().pop();
            ClientDataHolderVR.getInstance().vrPlayer.postPoll();
        }
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V"))
    private void vivecraft$preTickTasks(CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().vrPlayer.preTick();
        }
        if (VRState.VR_INITIALIZED) {
            if (ClientDataHolderVR.getInstance().menuWorldRenderer != null) {
                ClientDataHolderVR.getInstance().menuWorldRenderer.checkTask();
                if (ClientDataHolderVR.getInstance().menuWorldRenderer.isBuilding()) {
                    Profiler.get().push("Build Menu World");
                    ClientDataHolderVR.getInstance().menuWorldRenderer.buildNext();
                    Profiler.get().pop();
                }
            }
        }
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;tick()V", shift = Shift.AFTER))
    private void vivecraft$postTickTasks(CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().vrPlayer.postTick();
        }
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;update(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void vivecraft$preRender(CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            Profiler.get().push("preRender");
            ClientDataHolderVR.getInstance().vrPlayer.preRender(this.deltaTracker.getGameTimeDeltaPartialTick(true));
            VRHotkeys.updateMovingThirdPersonCam();
            Profiler.get().pop();
        }
    }


    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pick(F)V"))
    private void vivecraft$onlyOnePick(Minecraft instance, float partialTick, Operation<Void> original) {
        if (RenderPassType.isVanilla()) {
            original.call(instance, partialTick);
        } else {
            ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
            if (!(ImmersivePortalsHelper.isLoaded() && ImmersivePortalsHelper.isRenderingPortal())) {
                original.call(instance, partialTick);

                if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
                    dataHolder.vrPlayer.crossVec = this.hitResult.getLocation();
                }

                if (this.screen == null) {
                    dataHolder.teleportTracker.updateTeleportDestinations(this.player);
                }
            }
        }
    }

    @ModifyArg(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extract(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private boolean vivecraft$setupRenderGUI(boolean renderLevel) {
        if (VRState.VR_RUNNING) {
            try {
                Profiler.get().push("setupRenderConfiguration");
                RenderHelper.checkGLError("pre render setup");
                ClientDataHolderVR.getInstance().vrRenderer.setupRenderConfiguration();
                RenderHelper.checkGLError("post render setup");
            } catch (Exception e) {
                // something went wrong, disable VR
                VRState.destroyVR(true);
                vivecraft$switchVRState(false);
                VRSettings.LOGGER.error("Vivecraft: setupRenderConfiguration failed:", e);
                if (e instanceof RenderConfigException renderConfigException) {
                    setScreen(new ErrorScreen(renderConfigException.title, renderConfigException.error));
                } else {
                    setScreen(new ErrorScreen(Component.translatable("vivecraft.messages.vrrendererror"),
                        TextUtils.throwableToComponent(e)));
                }
                return renderLevel;
            } finally {
                Profiler.get().pop();
            }

            // set gui pass before setup, to always be in that pass and not a random one from last frame
            RenderPassManager.setGUIRenderPass();

            // only draw the gui when the level was rendered once, since some mods expect that
            ((GameRendererExtension) this.gameRenderer).vivecraft$setShouldDrawGui(
                renderLevel && this.entityRenderDispatcher.camera != null);
            // don't draw the level when we only want the GUI
            ((GameRendererExtension) this.gameRenderer).vivecraft$setShouldDrawScreen(true);
            return false;
        } else {
            return renderLevel;
        }
    }

    @ModifyArg(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private boolean vivecraft$renderGUI(boolean renderLevel) {
        if (VRState.VR_RUNNING) {
            // draw screen/gui to buffer
            // push pose so we can pop it later
            RenderSystem.getModelViewStack().pushMatrix();
            // don't draw the level when we only want the GUI
            return false;
        } else {
            return renderLevel;
        }
    }

    @WrapOperation(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen()V"))
    private void vivecraft$blitMirror(RenderTarget instance, Operation<Void> original) {
        if (VRState.VR_RUNNING) {
            Profiler.get().popPush("vrMirror");
            RenderPassManager.setMirrorRenderPass();
            ShaderHelper.drawMirror();
            RenderHelper.checkGLError("post-mirror");
            original.call(this.mainRenderTarget);
            RenderPassManager.setGUIRenderPass();
        } else {
            if (VRState.VR_ENABLED && !VRState.VR_INITIALIZED) {
                // show message that the game is connecting to the vr runtime
                RenderHelper.drawVRConnectingMessage();
            }
            original.call(instance);
        }
    }

    @WrapMethod(method = "pick(F)V")
    private void vivecraft$vrPick(float partialTick, Operation<Void> original) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
            // don't update the hitresult when chat is open
            if (this.screen != null && this.hitResult != null) {
                return;
            }
            // skip when data not available yet
            else if (dataHolder.vrPlayer.vrdata_world_render == null ||
                this.getCameraEntity() == null)
            {
                // some mods don't like it when the hitresult is null, so set it to a miss
                if (this.player != null) {
                    this.hitResult = BlockHitResult.miss(this.player.position(),
                        this.player.getDirection(), this.player.blockPosition());
                } else {
                    this.hitResult = BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
                }
                return;
            }

            AABB originalBB = this.getCameraEntity().getBoundingBox();
            // set the entity position and view to the controller
            ((GameRendererExtension) this.gameRenderer).vivecraft$cacheRVEPos(this.getCameraEntity());
            ((GameRendererExtension) this.gameRenderer).vivecraft$setupRVEAtDevice(
                dataHolder.vrPlayer.vrdata_world_render.getAim());
            // move the bounding box as well, this is used for entity hits
            this.getCameraEntity().setBoundingBox(originalBB.move(
                this.getCameraEntity().position()
                    .subtract(((GameRendererExtension) this.gameRenderer).vivecraft$getRvePos())));

            // call the vanilla method
            original.call(partialTick);

            // restore entity
            ((GameRendererExtension) this.gameRenderer).vivecraft$restoreRVEPos(this.getCameraEntity());
            this.getCameraEntity().setBoundingBox(originalBB);
        } else {
            // call the vanilla method
            original.call(partialTick);
        }
    }

    @Inject(method = "setCameraEntity", at = @At("HEAD"))
    private void vivecraft$rideEntity(Entity entity, CallbackInfo ci) {
        if (VRState.VR_INITIALIZED && entity != null) {
            if (entity != this.getCameraEntity()) {
                // snap to entity, if it changed
                ClientDataHolderVR.getInstance().vrPlayer.snapRoomOriginToPlayerEntity(entity, true, false);
                if (entity != this.player) {
                    // ride the new camera entity
                    ClientDataHolderVR.getInstance().vehicleTracker.onStartRiding(entity);
                } else {
                    ClientDataHolderVR.getInstance().vehicleTracker.onStopRiding();
                }
            }
        }
    }

    // the VR runtime handles the frame limit, no need to manually limit it 60fps
    @ModifyExpressionValue(method = "doWorldLoad", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/TimeUnit;toNanos(J)J"))
    private long vivecraft$noWaitOnLevelLoad(long original) {
        return VRState.VR_RUNNING ? 0L : original;
    }

    @WrapOperation(method = {"continueAttack", "startAttack"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void vivecraft$swingArmAttack(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().swingType = VRFirstPersonArmSwing.ATTACK;
        }
        original.call(instance, hand);
    }

    @WrapWithCondition(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;stopDestroyBlock()V"))
    private boolean vivecraft$destroyReset(MultiPlayerGameMode instance) {
        // only stop destroying blocks when triggered with a button
        boolean call =
            !VRState.VR_RUNNING || ClientDataHolderVR.getInstance().vrSettings.seated || this.vivecraft$attackKeyDown;
        this.vivecraft$attackKeyDown = false;
        return call;
    }

    @WrapWithCondition(method = "continueAttack", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;missTime:I", opcode = Opcodes.PUTFIELD))
    private boolean vivecraft$noDelayReset(Minecraft instance, int value) {
        // don't reset the miss timer, while we are not repressing keys
        return !VRState.VR_RUNNING || !ClientDataHolderVR.getInstance().vr.ignorePressesNextFrame;
    }

    @ModifyExpressionValue(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"))
    private boolean vivecraft$skipDestroyCheck(boolean isDestroying) {
        // in standing the player can use items even when a block is being destroyed
        // the result of this is inverted
        // this final result is '!isDestroying || (VRState.vrRunning && !seated)'
        return isDestroying && (!VRState.VR_RUNNING || ClientDataHolderVR.getInstance().vrSettings.seated);
    }

    @ModifyExpressionValue(method = "startUseItem", at = @At(value = "CONSTANT", args = "intValue=4"))
    private int vivecraft$customUseDelay(int delay) {
        if (VRState.VR_RUNNING) {
            return switch (ClientDataHolderVR.getInstance().vrSettings.rightclickDelay) {
                case VANILLA -> delay;
                case SLOW -> 6;
                case SLOWER -> 8;
                case SLOWEST -> 10;
            };
        } else {
            return delay;
        }
    }

    @WrapOperation(method = "startUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1))
    private HitResult vivecraft$sendActiveHandStart(
        Minecraft instance, Operation<HitResult> original, @Local InteractionHand hand, @Local ItemStack itemstack)
    {
        if (VRState.VR_RUNNING) {
            if (ClientDataHolderVR.getInstance().vrSettings.seated || !TelescopeTracker.isTelescope(itemstack)) {
                if (ClientDataHolderVR.getInstance().vrSettings.seated &&
                    (itemstack.getItem() instanceof BucketItem || itemstack.getItem() instanceof BoatItem))
                {
                    // these need to aim from the head or they mismatch
                    ClientNetworking.sendActiveBodyPart(VRBodyPart.HEAD, true);
                } else {
                    ClientNetworking.sendActiveHand(hand, false);
                }
            } else {
                // no telescope use in standing vr
                return null;
            }
        }

        return original.call(instance);
    }

    @Inject(method = "startUseItem", at = @At("RETURN"))
    private void vivecraft$sendActiveHandStartReset(CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            ClientNetworking.resetActiveBodyPart();
        }
    }

    @WrapOperation(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void vivecraft$swingArmUse(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().swingType = VRFirstPersonArmSwing.USE;
        }
        original.call(instance, hand);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void vivecraft$vrTick(CallbackInfo ci) {
        ClientDataHolderVR dataHolder = ClientDataHolderVR.getInstance();
        dataHolder.tickCounter++;

        // general chat notifications
        if (this.level != null) {
            // update notification
            if (!dataHolder.showedUpdateNotification && UpdateChecker.HAS_UPDATE &&
                (dataHolder.vrSettings.alwaysShowUpdates ||
                    !UpdateChecker.NEWEST_VERSION.equals(dataHolder.vrSettings.lastUpdate)
                ))
            {
                dataHolder.vrSettings.lastUpdate = UpdateChecker.NEWEST_VERSION;
                dataHolder.vrSettings.saveOptions();
                dataHolder.showedUpdateNotification = true;
                ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.updateAvailable",
                    Component.literal(UpdateChecker.NEWEST_VERSION)
                        .withStyle(ChatFormatting.ITALIC, ChatFormatting.GREEN)).withStyle(
                    style -> style.withClickEvent(
                            new VivecraftClickEvent(VivecraftClickEvent.VivecraftAction.OPEN_SCREEN, new UpdateScreen()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("vivecraft.messages.click")))));
            }

            // cached screen screen
            if (dataHolder.cachedScreen != null) {
                if (this.screen.getClass() != dataHolder.cachedScreen.getClass()) {
                    // set cached screens here, in case Quickplay is used, this shouldn't be triggered in other cases, since the cached screen gets cleared if it's the same screen
                    if (dataHolder.cachedScreen instanceof ChangeableParentScreen child) {
                        child.setParent(this.screen);
                    }
                    setScreen(dataHolder.cachedScreen);
                }
                dataHolder.cachedScreen = null;
            }

            // VR only chat notifications
            // server warnings
            if (ClientNetworking.CHAT_WARNING_TIMER >= 0 && --ClientNetworking.CHAT_WARNING_TIMER == 0) {
                ClientNetworking.ABLE_TO_DISPLAY_CHAT_WARNINGS = true;
                if (ClientNetworking.TELEPORT_WARNING) {
                    // allow vr switching on vanilla server
                    ClientNetworking.SERVER_ALLOWS_VR_SWITCHING = true;
                }
            }
            if (ClientNetworking.CHAT_WARNING_TIMER < 0) {
                // only show messages when vr is activated
                if (VRState.VR_INITIALIZED) {
                    // old server plugin that doesn't support head aim correctly
                    if (ClientNetworking.HEAD_AIM_WARNING && !ClientNetworking.DISPLAYED_HEAD_AIM_WARNING &&
                        dataHolder.vrSettings.aimDevice == VRSettings.AimDevice.HMD)
                    {
                        ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.noheadaimserverplugin"));
                        ClientNetworking.HEAD_AIM_WARNING = false;
                        ClientNetworking.DISPLAYED_HEAD_AIM_WARNING = true;
                    }

                    // other server messages that should only be shown once when joining
                    if (ClientNetworking.ABLE_TO_DISPLAY_CHAT_WARNINGS) {
                        boolean showMessage = !ClientNetworking.DISPLAYED_CHAT_WARNING ||
                            dataHolder.vrSettings.showServerPluginMissingMessageAlways;

                        // no server mod
                        if (ClientNetworking.TELEPORT_WARNING) {
                            if (showMessage) {
                                ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.noserverplugin"));
                            }
                            ClientNetworking.TELEPORT_WARNING = false;
                        }
                        // old server mod
                        if (ClientNetworking.VR_SWITCHING_WARNING) {
                            if (showMessage) {
                                ClientUtils.addChatMessage(
                                    Component.translatable("vivecraft.messages.novrhotswitchinglegacy"));
                            }
                            ClientNetworking.VR_SWITCHING_WARNING = false;
                        }

                        ClientNetworking.DISPLAYED_CHAT_WARNING = true;
                        ClientNetworking.ABLE_TO_DISPLAY_CHAT_WARNINGS = false;
                    }
                }
            }
            if (VRState.VR_INITIALIZED) {
                // fbt calibration notification
                if (!dataHolder.showedFbtCalibrationNotification &&
                    ((MCVR.get().hasFBT() && !dataHolder.vrSettings.fbtCalibrated) ||
                        (MCVR.get().hasExtendedFBT() && !dataHolder.vrSettings.fbtExtendedCalibrated)
                    ))
                {
                    dataHolder.showedFbtCalibrationNotification = true;
                    ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.calibratefbtchat"));
                }

                // non default server settings
                if (!ClientNetworking.DISPLAYED_VR_CHANGES && ClientNetworking.SERVER_VR_CHANGES_LIST != null &&
                    dataHolder.vrSettings.showServerVrChangesMessage.getAsBoolean())
                {
                    ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.nondefaultvrchanges",
                        Component.translatable("vivecraft.messages.click").withStyle(style -> style
                            .withClickEvent(new VivecraftClickEvent(VivecraftClickEvent.VivecraftAction.OPEN_SCREEN,
                                new ServerVrChangesScreen(ClientNetworking.SERVER_VR_CHANGES_LIST)))
                            .withHoverEvent(new HoverEvent.ShowText(Component.translatable("vivecraft.messages.click")))
                            .withColor(ChatFormatting.GREEN))));
                    ClientNetworking.SERVER_VR_CHANGES_LIST = null;
                    ClientNetworking.DISPLAYED_VR_CHANGES = true;
                }

                // no teleport message
                if (ClientNetworking.SHOW_NO_TELEPORT_MESSAGE) {
                    MCVR vr = dataHolder.vr;
                    // only if the player is using teleports
                    if (!vr.getOrigins(vr.getInputAction(VivecraftVRMod.INSTANCE.keyTeleport)).isEmpty() ||
                        !vr.getOrigins(vr.getInputAction(VivecraftVRMod.INSTANCE.keyTeleportFallback)).isEmpty() ||
                        (dataHolder.vrSettings.seated && !dataHolder.vrSettings.seatedFreeMove))
                    {
                        ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.noteleport"));
                        ClientNetworking.SHOW_NO_TELEPORT_MESSAGE = false;
                    }
                }
            }
        }

        // process keybinds before ticking input actions, to make sure they are processed before they are unpressed
        Profiler.get().push("Vivecraft Keybindings");
        KeybindHandler.processKeybindings();
        Profiler.get().pop();

        if (VRState.VR_RUNNING) {
            if (dataHolder.menuWorldRenderer.isReady() && MethodHolder.isInMenuRoom()) {
                dataHolder.menuWorldRenderer.tick();
            }

            Profiler.get().push("vrProcessBindings");
            dataHolder.vr.processBindings();

            Profiler.get().popPush("vrInputActionsTick");
            for (VRInputAction vrinputaction : dataHolder.vr.getInputActions()) {
                vrinputaction.tick();
            }

            if (this.level != null && dataHolder.vrPlayer != null) {
                dataHolder.vrPlayer.updateFreeMove();
            }

            Profiler.get().pop();
        }

        Profiler.get().push("vrPlayers");
        ClientVRPlayers.getInstance().tick();

        Profiler.get().pop();
    }

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pick(F)V"))
    private boolean vivecraft$removePick(Minecraft instance, float partialTicks) {
        // not exactly why we remove that, probably to safe some performance
        // don't cancel it though if the hitresult is null
        return !VRState.VR_RUNNING || this.hitResult == null;
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"))
    private void vivecraft$changeVrMirror(Options instance, CameraType pointOfView, Operation<Void> original) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
            MirrorNotification.notify(
                ClientDataHolderVR.getInstance().vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY),
                false, 3000);
        } else {
            original.call(instance, pointOfView);
        }
    }

    @Inject(method = "handleKeybinds", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Options;hideGui:Z", ordinal = 1, shift = At.Shift.AFTER))
    private void vivecraft$saveHideGuiOption(CallbackInfo ci) {
        ClientDataHolderVR.getInstance().vrSettings.saveOptions();
    }

    @WrapWithCondition(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;checkEntityPostEffect(Lnet/minecraft/world/entity/Entity;)V"))
    private boolean vivecraft$noPostEffectVR(GameRenderer instance, Entity entity) {
        return !VRState.VR_RUNNING;
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private void vivecraft$swingArmDrop(LocalPlayer instance, InteractionHand hand, Operation<Void> original) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().swingType = VRFirstPersonArmSwing.ATTACK;
        }
        original.call(instance, hand);
    }

    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;isDown()Z", ordinal = 2))
    private boolean vivecraft$useKeyOverride(boolean useKeyDown) {
        if (!VRState.VR_RUNNING || ClientDataHolderVR.getInstance().vrSettings.seated) {
            return useKeyDown;
        } else {
            return useKeyDown || ClientDataHolderVR.getInstance().isTrackerUsingItem(this.player);
        }
    }

    @WrapWithCondition(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startUseItem()V"))
    private boolean vivecraft$noUseWithRoomscaleBow(Minecraft instance) {
        return !VRState.VR_RUNNING || !ClientDataHolderVR.getInstance().bowTracker.isActive(this.player);
    }

    @WrapOperation(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;releaseUsingItem(Lnet/minecraft/world/entity/player/Player;)V"))
    private void vivecraft$sendActiveHandRelease(
        MultiPlayerGameMode instance, Player player, Operation<Void> original)
    {
        if (VRState.VR_RUNNING) {
            ClientNetworking.sendActiveHand(this.player.getUsedItemHand(), false);
        }
        original.call(instance, player);
        if (VRState.VR_RUNNING) {
            ClientNetworking.resetActiveBodyPart();
        }
    }

    @Inject(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;startAttack()Z"))
    private void vivecraft$markAttackKeyDown(CallbackInfo ci) {
        // detect, if the attack button was used to destroy blocks
        this.vivecraft$attackKeyDown = true;
    }

    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MouseHandler;isMouseGrabbed()Z"))
    private boolean vivecraft$vrAlwaysGrabbed(boolean isMouseGrabbed) {
        return isMouseGrabbed || VRState.VR_RUNNING;
    }

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void vivecraft$resetRoomOrigin(CallbackInfo ci) {
        if (VRState.VR_RUNNING) {
            ClientDataHolderVR.getInstance().vrPlayer.setRoomOrigin(0.0D, 0.0D, 0.0D, true);
        }
    }

    @Inject(method = "setOverlay", at = @At("TAIL"))
    private void vivecraft$onOverlaySet(CallbackInfo ci) {
        GuiHandler.onScreenChanged(this.screen, this.screen, true);
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void vivecraft$onScreenChange(
        Screen guiScreen, CallbackInfo ci, @Share("guiScale") LocalIntRef guiScaleRef)
    {
        if (guiScreen == null) {
            GuiHandler.GUI_APPEAR_OVER_BLOCK_ACTIVE = false;
        }
        // cache gui scale so it can be checked after screen apply
        guiScaleRef.set(this.options.guiScale().get());
    }

    @Inject(method = "setScreen", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0))
    private void vivecraft$onScreenSet(Screen guiScreen, CallbackInfo ci) {
        GuiHandler.onScreenChanged(this.screen, guiScreen, true);
    }

    @Inject(method = "setScreen", at = @At("RETURN"))
    private void vivecraft$checkGuiScaleChangePost(CallbackInfo ci, @Share("guiScale") LocalIntRef guiScaleRef) {
        if (guiScaleRef.get() != this.options.guiScale().get()) {
            // checks if something changed the GuiScale during screen change
            // and tries to adjust the VR GuiScale accordingly
            int maxScale = VRState.VR_RUNNING ? GuiHandler.GUI_SCALE_FACTOR_MAX :
                this.window.calculateScale(0, this.options.forceUnicodeFont().get());

            // auto uses max scale
            if (guiScaleRef.get() == 0) {
                guiScaleRef.set(maxScale);
            }

            int newScale = this.options.guiScale().get() == 0 ? maxScale : this.options.guiScale().get();

            if (newScale < guiScaleRef.get()) {
                // if someone reduced the gui scale, try to reduce the VR gui scale by the same steps
                int newVRScale = VRState.VR_RUNNING ? newScale :
                    Math.max(1, GuiHandler.GUI_SCALE_FACTOR_MAX - (guiScaleRef.get() - newScale));
                GuiHandler.GUI_SCALE_FACTOR = GuiHandler.calculateScale(newVRScale,
                    this.options.forceUnicodeFont().get(),
                    GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT);
            } else {
                // new gui scale is bigger than before, so just reset to the default
                VRSettings vrSettings = ClientDataHolderVR.getInstance().vrSettings;
                GuiHandler.GUI_SCALE_FACTOR = GuiHandler.calculateScale(
                    vrSettings.doubleGUIResolution ? vrSettings.guiScale : (int) Math.ceil(vrSettings.guiScale * 0.5f),
                    this.options.forceUnicodeFont().get(), GuiHandler.GUI_WIDTH, GuiHandler.GUI_HEIGHT);
            }

            // resize the screen for the new gui scale
            if (VRState.VR_RUNNING && this.screen != null) {
                this.screen.resize(GuiHandler.SCALED_WIDTH, GuiHandler.SCALED_HEIGHT);
            }
        }
    }

    /**
     * switches the VR state
     *
     * @param vrActive if VR is now on or off
     */
    @Unique
    private void vivecraft$switchVRState(boolean vrActive) {
        boolean changed = VRState.VR_RUNNING != vrActive;
        VRState.VR_RUNNING = vrActive;
        if (changed) {
            if (vrActive) {
                // force first person camera in VR
                this.vivecraft$lastCameraType = this.options.getCameraType();
                this.options.setCameraType(CameraType.FIRST_PERSON);

                if (this.player != null) {
                    // snap room origin to the player
                    ClientDataHolderVR.getInstance().vrPlayer.snapRoomOriginToPlayerEntity(this.player, false, false);
                }
                // release mouse when switching to standing
                if (!ClientDataHolderVR.getInstance().vrSettings.seated || this.screen != null || this.level == null) {
                    InputConstants.grabOrReleaseMouse(this.window, GLFW.GLFW_CURSOR_NORMAL,
                        this.mouseHandler.xpos(), this.mouseHandler.ypos());
                    this.mouseHandler.onMove(this.window.handle(), this.mouseHandler.xpos(),
                        this.mouseHandler.ypos());
                }
                if (ClientDataHolderVR.getInstance().vrSettings.displayMirrorMode == VRSettings.MirrorMode.OFF) {
                    // make sure this is shown at lest once when the text is disabled
                    MirrorNotification.notify(I18n.get("vivecraft.messages.mirroroff"), true, 1000);
                }
            } else {
                // VR got disabled
                RenderPassManager.setVanillaRenderPass();
                // reset gui
                GuiHandler.GUI_POS_ROOM = null;
                GuiHandler.GUI_ROTATION_ROOM = null;
                GuiHandler.GUI_SCALE = 1.0F;

                // reset camera
                if (this.vivecraft$lastCameraType != null) {
                    this.options.setCameraType(this.vivecraft$lastCameraType);
                }

                if (this.player != null) {
                    // remove vr player instance
                    ClientVRPlayers.getInstance().disableVR(this.player.getUUID());
                }
                if (this.gameRenderer != null) {
                    // update active effect, since VR does block t hem
                    this.gameRenderer.checkEntityPostEffect(
                        this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
                }

                // scale vr mouse position to the window position
                double mouseX = this.mouseHandler.xpos() / (double) GuiHandler.GUI_WIDTH * this.window.getScreenWidth();
                double mouseY =
                    this.mouseHandler.ypos() / (double) GuiHandler.GUI_HEIGHT * this.window.getScreenHeight();

                if (this.screen != null || this.level == null) {
                    // release mouse
                    InputConstants.grabOrReleaseMouse(this.window, GLFW.GLFW_CURSOR_NORMAL, mouseX, mouseY);
                    this.mouseHandler.onMove(this.window.handle(), mouseX, mouseY);
                    this.mouseHandler.releaseMouse();
                } else {
                    // grab mouse when in a menu
                    InputConstants.grabOrReleaseMouse(this.window, GLFW.GLFW_CURSOR_DISABLED, mouseX,
                        mouseY);
                    this.mouseHandler.grabMouse();
                }
                // unpress any keys we simulated for VR
                if (MCVR.get() != null) {
                    for (VRInputAction action : MCVR.get().getInputActions()) {
                        action.unpressBindingImmediately();
                    }
                }
            }

            // send new VR state to the server
            ClientNetworking.sendServerPacket(new VRActivePayloadC2S(vrActive));
            if (ReplayHelper.isLoaded()) {
                // replay mod / flashback compat, on servers without the plugin
                ReplayHelper.storeVRActive(vrActive);
            }

            // send options, since we override the main hand setting
            this.options.broadcastOptions();

            // reload sound manager, to toggle HRTF between VR and NONVR one
            if (!getSoundManager().getAvailableSounds().isEmpty()) {
                getSoundManager().reload();
            }
        }
        // always resize, since that also rebuild the screen
        ((WindowExtension) (Object) this.window).vivecraft$resize();
        this.window.updateVsync(this.options.enableVsync().get());
    }

    /**
     * return current partialTick
     */
    @Unique
    @Override
    public float vivecraft$getPartialTick() {
        return this.deltaTracker.getGameTimeDeltaPartialTick(false);
    }
}
