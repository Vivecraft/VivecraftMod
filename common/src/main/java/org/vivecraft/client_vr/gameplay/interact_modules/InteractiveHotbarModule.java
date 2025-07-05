package org.vivecraft.client_vr.gameplay.interact_modules;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.api.client.InteractModule;

public class InteractiveHotbarModule implements InteractModule {

    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("vivecraft", "interactive_hotbar");

    // indicates the pointed at hotbar slot
    public int hotbar = -1;

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public int getPriority() {
        // hotbar first
        return 0;
    }

    @Override
    public boolean isActive(LocalPlayer player, InteractionHand hand, Vec3 handPosition) {
        // interactive hotbar is priority 1
        return hand == InteractionHand.MAIN_HAND && this.hotbar >= 0;
    }

    @Override
    public boolean onPress(LocalPlayer player, InteractionHand hand) {
        if (this.hotbar >= 0 && this.hotbar < 9 && player.getInventory().selected != this.hotbar &&
            hand == InteractionHand.MAIN_HAND)
        {
            player.getInventory().selected = this.hotbar;
        } else if (this.hotbar == 9 && hand == InteractionHand.MAIN_HAND) {
            player.connection.send(
                new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND,
                    BlockPos.ZERO, Direction.DOWN));
        }
        return false;
    }
}
