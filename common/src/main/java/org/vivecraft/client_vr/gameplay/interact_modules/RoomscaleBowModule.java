package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class RoomscaleBowModule implements HeldInteractModule {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("vivecraft", "roomscale_bow");

    private final ClientDataHolderVR dh;

    public RoomscaleBowModule(ClientDataHolderVR dh) {
        this.dh = dh;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public boolean doProcess(LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        // roomscale Bow shooting, only activate for the hand with the arrow
        return this.dh.bowTracker.isNotched() &&
            hand == ((this.dh.vrSettings.reverseShootingEye && ClientNetworking.supportsReversedBow()) ?
                InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND
            );
    }

    @Override
    public boolean processBindingPress(LocalPlayer player, InteractionHand hand) {
        // we don't do anything, we just block other modules from doing stuff
        return false;
    }

    @Override
    public void processBindingRelease(@Nullable LocalPlayer player, InteractionHand hand) {
        // we don't do anything, we just block other modules from doing stuff
    }
}
