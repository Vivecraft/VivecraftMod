package org.vivecraft.client_vr.gameplay.trackers;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.Xplat;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
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

import java.util.HashSet;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import static org.joml.Math.*;
import static org.joml.RoundingMode.FLOOR;

public class InteractTracker extends Tracker
{
    public boolean[] bukkit = new boolean[2];
    public int hotbar = -1;
    public BlockHitResult[] inBlockHit = new BlockHitResult[2];
    BlockPos[] inBlockPos = new BlockPos[2];
    Entity[] inEntity = new Entity[2];
    private EntityHitResult[] inEntityHit = new EntityHitResult[2];
    private boolean[] inCamera = new boolean[2];
    private boolean[] inHandheldCamera = new boolean[2];
    boolean[] active = new boolean[2];
    boolean[] wasactive = new boolean[2];
    private HashSet<Class> rightClickable = null;

    public boolean isActive()
    {
        if (mc.gameMode == null)
        {
            return false;
        }
        else if (mc.player == null)
        {
            return false;
        }
        else if (!mc.player.isAlive())
        {
            return false;
        }
        else if (mc.player.isSleeping())
        {
            return false;
        }
        else
        {
            if (dh.vrSettings.seated)
            {
                return false;
            }
            else if (mc.player.isBlocking() && this.hotbar < 0)
            {
                return false;
            }
            else
            {
                return !dh.bowTracker.isNotched();
            }
        }
    }

    public void reset()
    {
        for (int i = 0; i < 2; ++i)
        {
            this.reset(i);
        }
    }

    private void reset(int c)
    {
        if (this.inCamera[c] && VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.INTERACTION && VRHotkeys.getMovingThirdPersonCamController() == c)
        {
            VRHotkeys.stopMovingThirdPersonCam();
        }

        if (this.inHandheldCamera[c] && dh.cameraTracker.isMoving() && dh.cameraTracker.getMovingController() == c && !dh.cameraTracker.isQuickMode())
        {
            dh.cameraTracker.stopMoving();
        }

        this.inBlockPos[c] = null;
        this.inBlockHit[c] = null;
        this.inEntity[c] = null;
        this.inEntityHit[c] = null;
        this.inCamera[c] = false;
        this.inHandheldCamera[c] = false;
        this.active[c] = false;
        dh.vr.getInputAction(VivecraftVRMod.keyVRInteract).setEnabled(ControllerType.values()[c], false);
    }

    public void doProcess()
    {
        if (this.rightClickable == null)
        {
            this.rightClickable = new HashSet<>();

            String name = Xplat.getUseMethodName();
            for (Object object : BuiltInRegistries.BLOCK)
            {
                Class<?> oclass = object.getClass();

                try
                {
                    if (oclass.getMethod(name,
                            BlockState.class,
                            net.minecraft.world.level.Level.class,
                            BlockPos.class,
                            net.minecraft.world.entity.player.Player.class,
                            InteractionHand.class,
                            BlockHitResult.class).getDeclaringClass() == oclass) {
                        this.rightClickable.add(oclass);
                    }
                }
                catch (Throwable ignored)
                {
                }

                oclass = oclass.getSuperclass();

                try
                {
                    if (oclass.getMethod(name,
                            BlockState.class,
                            net.minecraft.world.level.Level.class,
                            BlockPos.class,
                            net.minecraft.world.entity.player.Player.class,
                            InteractionHand.class,
                            BlockHitResult.class).getDeclaringClass() == oclass) {
                        this.rightClickable.add(oclass);
                    }
                }
                catch (Throwable ignored)
                {
                }
            }

            this.rightClickable.remove(Block.class);
            this.rightClickable.remove(BlockBehaviour.class);
            this.rightClickable.remove(BlockBehaviour.BlockStateBase.class);
        }

        Vec3 vec34 = new Vec3(0.0D, 0.0D, -1.0D);

        for (int j = 0; j < 2; ++j)
        {
            if (!this.inCamera[j] && !this.inHandheldCamera[j] || !VivecraftVRMod.keyVRInteract.isDown(ControllerType.values()[j]))
            {
                this.reset(j);

                if (j == 0 && this.hotbar >= 0)
                {
                    this.active[j] = true;
                }

                Vec3 vec35 = dh.vrPlayer.vrdata_world_pre.getHeadPivot();
                Vec3 vec3 = dh.vrPlayer.vrdata_world_pre.getController(j).getPosition();
                Vec3 vec31 = dh.vrPlayer.vrdata_world_pre.getHand(j).getCustomVector(vec34);
                ItemStack itemstack = mc.player.getItemInHand(j == 0 ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
                Item item = null;

                if (!this.active[j] && (dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) && dh.vrSettings.mixedRealityRenderCameraModel)
                {
                    VRData.VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_world_pre.getEye(RenderPass.THIRD);
                    Vec3 vec32 = vrdata$vrdevicepose.getPosition();
                    vec32 = vec32.subtract(vrdata$vrdevicepose.getCustomVector(new Vec3(0.0D, 0.0D, -1.0D)).scale((double)0.15F * dh.vrPlayer.vrdata_world_pre.worldScale));
                    vec32 = vec32.subtract(vrdata$vrdevicepose.getCustomVector(new Vec3(0.0D, -1.0D, 0.0D)).scale((double)0.05F * dh.vrPlayer.vrdata_world_pre.worldScale));

                    if (vec3.distanceTo(vec32) < (double)0.15F * dh.vrPlayer.vrdata_world_pre.worldScale)
                    {
                        this.inCamera[j] = true;
                        this.active[j] = true;
                    }
                }

                if (!this.active[j] && dh.cameraTracker.isVisible() && !dh.cameraTracker.isQuickMode())
                {
                    VRData.VRDevicePose vrdata$vrdevicepose1 = dh.vrPlayer.vrdata_world_pre.getEye(RenderPass.CAMERA);
                    Vec3 vec36 = vrdata$vrdevicepose1.getPosition();
                    vec36 = vec36.subtract(vrdata$vrdevicepose1.getCustomVector(new Vec3(0.0D, 0.0D, -1.0D)).scale((double)0.08F * dh.vrPlayer.vrdata_world_pre.worldScale));

                    if (vec3.distanceTo(vec36) < (double)0.11F * dh.vrPlayer.vrdata_world_pre.worldScale)
                    {
                        this.inHandheldCamera[j] = true;
                        this.active[j] = true;
                    }
                }

                if (!this.active[j])
                {
                    int k = roundUsing(vec3.x, FLOOR);
                    int l = roundUsing(vec3.y, FLOOR);
                    int i = roundUsing(vec3.z, FLOOR);
                    Vec3 vec33 = new Vec3(vec3.x + vec31.x * -0.1D, vec3.y + vec31.y * -0.1D, vec3.z + vec31.z * -0.1D);
                    AABB aabb = new AABB(vec3, vec33);
                    this.inEntityHit[j] = ProjectileUtil.getEntityHitResult(mc.getCameraEntity(), vec35, vec3, aabb, (e) ->
                    {
                        return !e.isSpectator() && e.isPickable() && e != mc.getCameraEntity().getVehicle();
                    }, 0.0D);

                    if (this.inEntityHit[j] != null)
                    {
                        Entity entity = this.inEntityHit[j].getEntity();
                        this.inEntity[j] = entity;
                        this.active[j] = true;
                    }
                }

                if (!this.active[j])
                {
                    BlockPos blockpos = null;
                    blockpos = BlockPos.containing(vec3);
                    BlockState blockstate = mc.level.getBlockState(blockpos);
                    BlockHitResult blockhitresult = blockstate.getShape(mc.level, blockpos).clip(vec35, vec3, blockpos);
                    this.inBlockPos[j] = blockpos;
                    this.inBlockHit[j] = blockhitresult;
                    this.active[j] = blockhitresult != null && (this.rightClickable.contains(blockstate.getBlock().getClass()) || this.rightClickable.contains(blockstate.getBlock().getClass().getSuperclass()));
                    this.bukkit[j] = false;

                    // TODO: liquid is deprecated
                    if (!this.active[j] && itemstack.getItem() == Items.BUCKET && blockstate.liquid())
                    {
                        this.active[j] = true;
                        this.bukkit[j] = true;
                    }
                }

                if (!this.wasactive[j] && this.active[j])
                {
                    dh.vr.triggerHapticPulse(j, 250);
                }

                dh.vr.getInputAction(VivecraftVRMod.keyVRInteract).setEnabled(ControllerType.values()[j], this.active[j]);
                this.wasactive[j] = this.active[j];
            }
        }
    }

    public boolean isInteractActive(int controller)
    {
        return this.active[controller];
    }

    public boolean isInCamera()
    {
        return this.inCamera[0] || this.inCamera[1];
    }

    public boolean isInHandheldCamera()
    {
        return this.inHandheldCamera[0] || this.inHandheldCamera[1];
    }

    public void processBindings()
    {
        for (int i = 0; i < 2; ++i)
        {
            if (VivecraftVRMod.keyVRInteract.consumeClick(ControllerType.values()[i]) && this.active[i])
            {
                InteractionHand interactionhand = InteractionHand.values()[i];
                boolean flag = false;

                if (this.hotbar >= 0 && this.hotbar < 9 && mc.player.getInventory().selected != this.hotbar && interactionhand == InteractionHand.MAIN_HAND)
                {
                    mc.player.getInventory().selected = this.hotbar;
                    flag = true;
                }
                else if (this.hotbar == 9 && interactionhand == InteractionHand.MAIN_HAND)
                {
                    mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
                    flag = true;
                }
                else if (this.inCamera[i])
                {
                    VRHotkeys.startMovingThirdPersonCam(i, VRHotkeys.Triggerer.INTERACTION);
                    flag = true;
                }
                else if (this.inHandheldCamera[i])
                {
                    dh.cameraTracker.startMoving(i);
                    flag = true;
                }
                else if (this.inEntityHit[i] != null)
                {
                    flag = true;

                    if (!mc.gameMode.interactAt(mc.player, this.inEntity[i], this.inEntityHit[i], interactionhand).consumesAction() && !mc.gameMode.interact(mc.player, this.inEntity[i], interactionhand).consumesAction())
                    {
                        flag = false;
                    }
                }
                else if (this.inBlockHit[i] != null)
                {
                    flag = mc.gameMode.useItemOn(mc.player, interactionhand, this.inBlockHit[i]).consumesAction();
                }
                else if (this.bukkit[i])
                {
                    flag = mc.gameMode.useItem(mc.player, interactionhand).consumesAction();
                }

                if (flag)
                {
                    ((PlayerExtension) mc.player).swingArm(interactionhand, VRFirstPersonArmSwing.Interact);
                    dh.vr.triggerHapticPulse(i, 750);
                }
            }
        }
    }
}
