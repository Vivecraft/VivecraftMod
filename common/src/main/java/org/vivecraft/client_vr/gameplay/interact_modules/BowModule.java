package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.HeldInteractModule;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.common.network.packet.c2s.DrawPayloadC2S;

import javax.annotation.Nullable;

/**
 * the bow interact module handles the key presses and sending of use packets for the {@link BowTracker}
 */
public class BowModule implements HeldInteractModule {

    private static final Identifier ID = Identifier.fromNamespaceAndPath("vivecraft", "roomscale_bow");

    private final ClientDataHolderVR dh;
    private final boolean[] isPressed = new boolean[2];

    public BowModule(ClientDataHolderVR dh) {
        this.dh = dh;
    }

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        // bow after hotbar
        return 500;
    }

    @Override
    public boolean onHoldTick(LocalPlayer player, InteractionHand hand) {
        // stop when the bow isn't active anymore
        return this.dh.bowTracker.isActive(player);
    }

    @Override
    public boolean isActive(LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        // roomscale Bow shooting, only activate for the hand with the arrow
        return this.dh.bowTracker.isNotched() &&
            hand == ((this.dh.vrSettings.reverseShootingEye && ClientNetworking.supportsReversedBow()) ?
                InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND
            );
    }

    @Override
    public void reset(LocalPlayer player, InteractionHand hand) {
        this.isPressed[hand.ordinal()] = false;
    }

    public boolean isPressed() {
        return this.isPressed[0] || this.isPressed[1];
    }

    @Override
    public boolean onPress(LocalPlayer player, InteractionHand hand) {
        // start drawing
        this.isPressed[hand.ordinal()] = true;
        // call useItem with the hand that has the bow
        boolean bowInMain = BowTracker.isHoldingBow(player, InteractionHand.MAIN_HAND);
        Minecraft.getInstance().gameMode.useItem(player,
            bowInMain ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
        return false;
    }

    @Override
    public void onRelease(@Nullable LocalPlayer player, InteractionHand hand) {
        // we cannot abort the item using from the client, so we need to shoot
        // fire!
        int arrowHand = hand == InteractionHand.MAIN_HAND ? 0 : 1;
        int bowHand = 1 - arrowHand;
        this.dh.vr.triggerHapticPulse(arrowHand, 500);
        this.dh.vr.triggerHapticPulse(bowHand, 3000);
        ClientNetworking.sendServerPacket(new DrawPayloadC2S(this.dh.bowTracker.getDrawPercent()));
        ClientNetworking.sendActiveBodyPart(arrowHand == 0 ? VRBodyPart.MAIN_HAND : VRBodyPart.OFF_HAND, true);

        Minecraft.getInstance().gameMode.releaseUsingItem(player);

        // reset to 0, in case user switches modes.
        ClientNetworking.sendServerPacket(new DrawPayloadC2S(0.0F));
        ClientNetworking.resetActiveBodyPart();
    }
}
