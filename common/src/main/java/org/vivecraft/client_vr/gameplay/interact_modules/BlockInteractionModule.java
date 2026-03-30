package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.InteractModule;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;

import java.util.HashSet;

public class BlockInteractionModule implements InteractModule {

    private static final Identifier ID = Identifier.fromNamespaceAndPath("vivecraft", "block_interact");

    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    public final BlockHitResult[] inBlockHit = new BlockHitResult[2];

    // indicates when a hand has a bucket and is in a liquid
    public final boolean[] bukkit = new boolean[2];

    // a set of blocks that can be interacted with
    private HashSet<Class<?>> rightClickable = null;

    public BlockInteractionModule(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        // block interaction should be after entities
        return 1500;
    }

    @Override
    public void reset(LocalPlayer player, InteractionHand hand) {
        this.inBlockHit[hand.ordinal()] = null;
        this.bukkit[hand.ordinal()] = false;
    }

    @Override
    public boolean isActive(LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        if (this.rightClickable == null) {
            // compile a list of blocks that explicitly declare OnBlockActivated (right click)
            this.rightClickable = new HashSet<>();

            String name = "useWithoutItem";
            for (Object object : BuiltInRegistries.BLOCK) {
                Class<?> oclass = object.getClass();

                addIfClassHasMethod(name, oclass);
                addIfClassHasMethod(name, oclass.getSuperclass());
            }

            // remove base classes, since that would trigger on all blocks
            this.rightClickable.remove(Block.class);
            this.rightClickable.remove(BlockBehaviour.class);
            this.rightClickable.remove(BlockBehaviour.BlockStateBase.class);
        }

        if (this.dh.vrSettings.realisticBlockInteractEnabled) {
            Vec3 hmdPos = this.dh.vrPlayer.vrdata_world_pre.getHeadPivot();
            BlockPos blockpos = BlockPos.containing(handPosition);
            BlockState blockstate = player.level().getBlockState(blockpos);

            BlockHitResult hit = blockstate.getShape(player.level(), blockpos).clip(hmdPos, handPosition, blockpos);
            this.inBlockHit[hand.ordinal()] = hit;

            if (hit != null && (this.rightClickable.contains(blockstate.getBlock().getClass()) ||
                this.rightClickable.contains(blockstate.getBlock().getClass().getSuperclass())
            ))
            {
                return true;
            } else if (player.getItemInHand(hand).getItem() == Items.BUCKET && blockstate.liquid()) {
                // bucket liquid pickup
                this.bukkit[hand.ordinal()] = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPress(LocalPlayer player, InteractionHand hand) {
        boolean success = false;
        if (this.inBlockHit[hand.ordinal()] != null) {
            // force main hand, since 1.20.5+ only checks no item interactions for the main hand
            ClientNetworking.sendActiveHand(hand, true);
            success = this.mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, this.inBlockHit[hand.ordinal()])
                .consumesAction();
            ClientNetworking.resetActiveBodyPart();
        } else if (this.bukkit[hand.ordinal()]) {
            ClientNetworking.sendActiveHand(hand, true);
            success = this.mc.gameMode.useItem(player, hand).consumesAction();
            ClientNetworking.resetActiveBodyPart();
        }
        return success;
    }

    public boolean isActive(int controller) {
        return this.dh.interactTracker.isActiveModule(this, controller);
    }

    private void addIfClassHasMethod(String name, Class<?> oclass) {
        try {
            oclass.getDeclaredMethod(name,
                BlockState.class,
                net.minecraft.world.level.Level.class,
                BlockPos.class,
                net.minecraft.world.entity.player.Player.class,
                BlockHitResult.class);
            this.rightClickable.add(oclass);
        } catch (Throwable ignored) {
            // catching Throwable here, instead of just NoSuchMethodException,
            // because some mods implement interfaces for mod compat, that don't need to be present and
            // those throw a NoClassDefFoundError
        }
    }
}
