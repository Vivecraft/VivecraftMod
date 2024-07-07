package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.HashSet;

public class InteractTracker extends Tracker {
    public boolean[] bukkit = new boolean[2];
    public int hotbar = -1;
    // indicates if the bow can be drawn
    private final boolean[] inBow = new boolean[2];
    public BlockHitResult[] inBlockHit = new BlockHitResult[2];
    BlockPos[] inBlockPos = new BlockPos[2];
    Entity[] inEntity = new Entity[2];
    private final EntityHitResult[] inEntityHit = new EntityHitResult[2];
    private final boolean[] inCamera = new boolean[2];
    private final boolean[] inHandheldCamera = new boolean[2];
    boolean[] active = new boolean[2];
    boolean[] wasactive = new boolean[2];
    private HashSet<Class> rightClickable = null;

    public InteractTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
    }

    public boolean isActive(LocalPlayer p) {
        if (this.mc.gameMode == null) {
            return false;
        } else if (p == null) {
            return false;
        } else if (!p.isAlive()) {
            return false;
        } else if (p.isSleeping()) {
            return false;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

            if (dataholder.vrSettings.seated) {
                return false;
            } else {
                return !(p.isBlocking() && this.hotbar < 0);
            }
        }
    }

    public void reset(LocalPlayer player) {
        for (int i = 0; i < 2; ++i) {
            this.reset(player, i);
        }
    }

    private void reset(LocalPlayer player, int c) {
        if (this.inCamera[c] && VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.INTERACTION && VRHotkeys.getMovingThirdPersonCamController() == c) {
            VRHotkeys.stopMovingThirdPersonCam();
        }

        if (this.inHandheldCamera[c] && this.dh.cameraTracker.isMoving() && this.dh.cameraTracker.getMovingController() == c && !this.dh.cameraTracker.isQuickMode()) {
            this.dh.cameraTracker.stopMoving();
        }

        this.inBow[c] = false;
        this.inBlockPos[c] = null;
        this.inBlockHit[c] = null;
        this.inEntity[c] = null;
        this.inEntityHit[c] = null;
        this.inCamera[c] = false;
        this.inHandheldCamera[c] = false;
        this.active[c] = false;
        this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract).setEnabled(ControllerType.values()[c], false);
    }

    public void doProcess(LocalPlayer player) {
        if (this.rightClickable == null) {
            this.rightClickable = new HashSet<>();

            String name = Xplat.getUseMethodName();
            for (Object object : BuiltInRegistries.BLOCK) {
                Class<?> oclass = object.getClass();

                try {
                    oclass.getDeclaredMethod(name,
                        BlockState.class,
                        net.minecraft.world.level.Level.class,
                        BlockPos.class,
                        net.minecraft.world.entity.player.Player.class,
                        BlockHitResult.class);
                    this.rightClickable.add(oclass);
                } catch (Throwable throwable1) {
                }

                oclass = oclass.getSuperclass();

                try {
                    oclass.getDeclaredMethod(name,
                        BlockState.class,
                        net.minecraft.world.level.Level.class,
                        BlockPos.class,
                        net.minecraft.world.entity.player.Player.class,
                        BlockHitResult.class);
                    this.rightClickable.add(oclass);
                } catch (Throwable throwable) {
                }
            }

            this.rightClickable.remove(Block.class);
            this.rightClickable.remove(BlockBehaviour.class);
            this.rightClickable.remove(BlockBehaviour.BlockStateBase.class);
        }

        Vec3 vec34 = new Vec3(0.0D, 0.0D, -1.0D);

        for (int j = 0; j < 2; ++j) {
            if (!this.inCamera[j] && !this.inHandheldCamera[j] && !this.inBow[j] || !VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.values()[j])) {
                this.reset(player, j);

                if (j == 0 && this.hotbar >= 0) {
                    this.active[j] = true;
                }

                // roomscale Bow shooting, only activate for the hand with the arrow
                if (!this.active[j] && this.dh.bowTracker.isNotched() && j == 0) {
                    this.inBow[j] = true;
                    this.active[j] = true;
                }

                Vec3 vec35 = this.dh.vrPlayer.vrdata_world_pre.getHeadPivot();
                Vec3 vec3 = this.dh.vrPlayer.vrdata_world_pre.getController(j).getPosition();
                Vec3 vec31 = this.dh.vrPlayer.vrdata_world_pre.getHand(j).getCustomVector(vec34);
                ItemStack itemstack = player.getItemInHand(j == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                Item item = null;

                if (!this.active[j] && (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) && this.dh.vrSettings.mixedRealityRenderCameraModel) {
                    VRData.VRDevicePose vrdata$vrdevicepose = this.dh.vrPlayer.vrdata_world_pre.getEye(RenderPass.THIRD);
                    Vec3 vec32 = vrdata$vrdevicepose.getPosition();
                    vec32 = vec32.subtract(vrdata$vrdevicepose.getCustomVector(new Vec3(0.0D, 0.0D, -1.0D)).scale((double) 0.15F * this.dh.vrPlayer.vrdata_world_pre.worldScale));
                    vec32 = vec32.subtract(vrdata$vrdevicepose.getCustomVector(new Vec3(0.0D, -1.0D, 0.0D)).scale((double) 0.05F * this.dh.vrPlayer.vrdata_world_pre.worldScale));

                    if (vec3.distanceTo(vec32) < (double) 0.15F * this.dh.vrPlayer.vrdata_world_pre.worldScale) {
                        this.inCamera[j] = true;
                        this.active[j] = true;
                    }
                }

                if (!this.active[j] && this.dh.cameraTracker.isVisible() && !this.dh.cameraTracker.isQuickMode()) {
                    VRData.VRDevicePose vrdata$vrdevicepose1 = this.dh.vrPlayer.vrdata_world_pre.getEye(RenderPass.CAMERA);
                    Vec3 vec36 = vrdata$vrdevicepose1.getPosition();
                    vec36 = vec36.subtract(vrdata$vrdevicepose1.getCustomVector(new Vec3(0.0D, 0.0D, -1.0D)).scale((double) 0.08F * this.dh.vrPlayer.vrdata_world_pre.worldScale));

                    if (vec3.distanceTo(vec36) < (double) 0.11F * this.dh.vrPlayer.vrdata_world_pre.worldScale) {
                        this.inHandheldCamera[j] = true;
                        this.active[j] = true;
                    }
                }

                if (this.dh.vrSettings.realisticEntityInteractEnabled && !this.active[j]) {
                    int k = Mth.floor(vec3.x);
                    int l = Mth.floor(vec3.y);
                    int i = Mth.floor(vec3.z);
                    Vec3 vec33 = new Vec3(vec3.x + vec31.x * -0.1D, vec3.y + vec31.y * -0.1D, vec3.z + vec31.z * -0.1D);
                    AABB aabb = new AABB(vec3, vec33);
                    this.inEntityHit[j] = ProjectileUtil.getEntityHitResult(this.mc.getCameraEntity(), vec35, vec3, aabb, (e) ->
                    {
                        return !e.isSpectator() && e.isPickable() && e != this.mc.getCameraEntity().getVehicle();
                    }, 0.0D);

                    if (this.inEntityHit[j] != null) {
                        Entity entity = this.inEntityHit[j].getEntity();
                        this.inEntity[j] = entity;
                        this.active[j] = true;
                    }
                }

                if (this.dh.vrSettings.realisticBlockInteractEnabled && !this.active[j]) {
                    BlockPos blockpos = null;
                    blockpos = BlockPos.containing(vec3);
                    BlockState blockstate = this.mc.level.getBlockState(blockpos);
                    BlockHitResult blockhitresult = blockstate.getShape(this.mc.level, blockpos).clip(vec35, vec3, blockpos);
                    this.inBlockPos[j] = blockpos;
                    this.inBlockHit[j] = blockhitresult;
                    this.active[j] = blockhitresult != null && (this.rightClickable.contains(blockstate.getBlock().getClass()) || this.rightClickable.contains(blockstate.getBlock().getClass().getSuperclass()));
                    this.bukkit[j] = false;

                    // TODO: liquid is deprecated
                    if (!this.active[j] && itemstack.getItem() == Items.BUCKET && blockstate.liquid()) {
                        this.active[j] = true;
                        this.bukkit[j] = true;
                    }
                }

                if (!this.wasactive[j] && this.active[j]) {
                    this.dh.vr.triggerHapticPulse(j, 250);
                }

                this.dh.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract).setEnabled(ControllerType.values()[j], this.active[j]);
                this.wasactive[j] = this.active[j];
            }
        }
    }

    public boolean isInteractActive(int controller) {
        return this.active[controller];
    }

    public boolean isInCamera() {
        return this.inCamera[0] || this.inCamera[1];
    }

    public boolean isInHandheldCamera() {
        return this.inHandheldCamera[0] || this.inHandheldCamera[1];
    }

    public void processBindings() {
        for (int i = 0; i < 2; ++i) {
            if (VivecraftVRMod.INSTANCE.keyVRInteract.consumeClick(ControllerType.values()[i]) && this.active[i]) {
                InteractionHand interactionhand = InteractionHand.values()[i];
                boolean flag = false;

                if (this.hotbar >= 0 && this.hotbar < 9 && this.mc.player.getInventory().selected != this.hotbar && interactionhand == InteractionHand.MAIN_HAND) {
                    this.mc.player.getInventory().selected = this.hotbar;
                    flag = true;
                } else if (this.hotbar == 9 && interactionhand == InteractionHand.MAIN_HAND) {
                    this.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                    flag = true;
                } else if (this.inCamera[i]) {
                    VRHotkeys.startMovingThirdPersonCam(i, VRHotkeys.Triggerer.INTERACTION);
                    flag = true;
                } else if (this.inHandheldCamera[i]) {
                    this.dh.cameraTracker.startMoving(i);
                    flag = true;
                } else if (this.inEntityHit[i] != null) {

                    flag = this.mc.gameMode.interactAt(this.mc.player, this.inEntity[i], this.inEntityHit[i], interactionhand).consumesAction() || this.mc.gameMode.interact(this.mc.player, this.inEntity[i], interactionhand).consumesAction();
                } else if (this.inBlockHit[i] != null) {
                    // force main hand, since 1.20.5 only checks no item interactions for the main hand
                    flag = this.mc.gameMode.useItemOn(this.mc.player, InteractionHand.MAIN_HAND, this.inBlockHit[i]).consumesAction();
                } else if (this.bukkit[i]) {
                    flag = this.mc.gameMode.useItem(this.mc.player, interactionhand).consumesAction();
                }

                if (flag) {
                    ((PlayerExtension) this.mc.player).vivecraft$swingArm(interactionhand, VRFirstPersonArmSwing.Interact);
                    this.dh.vr.triggerHapticPulse(i, 750);
                }
            }
        }
    }
}
