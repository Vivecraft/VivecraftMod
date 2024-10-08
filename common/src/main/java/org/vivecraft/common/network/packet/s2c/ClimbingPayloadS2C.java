package org.vivecraft.common.network.packet.s2c;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.vivecraft.common.network.packet.PayloadIdentifier;
import org.vivecraft.server.config.ClimbeyBlockmode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * packet that holds if the server allows climbing, and optionally a list of blocks that are allowed or blocked
 * @param allowed if climbing is enabled
 * @param blockmode id of the block mode. 0: DISABLED, 1: WHITELIST, 2: BLACKLIST
 * @param blocks list of blocks, can be {@code null}
 */
public record ClimbingPayloadS2C(boolean allowed, ClimbeyBlockmode blockmode, @Nullable List<String> blocks) implements VivecraftPayloadS2C {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CLIMBING;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.allowed);
        buffer.writeByte(this.blockmode.ordinal());
        if (this.blocks != null) {
            for (String block : this.blocks) {
                try {
                    Block b = BuiltInRegistries.BLOCK.get(new ResourceLocation(block));
                    // only send valid blocks
                    if (b != Blocks.AIR) {
                        buffer.writeUtf(block);
                    }
                } catch (ResourceLocationException ignore) {}
            }
        }
    }

    public static ClimbingPayloadS2C read(FriendlyByteBuf buffer) {
        boolean allowed = buffer.readBoolean();

        // legacy support, very old server plugin versions didn't have blocklists
        if (buffer.readableBytes() > 0) {
            ClimbeyBlockmode blockmode = ClimbeyBlockmode.values()[buffer.readByte()];
            List<String> blocks  = new ArrayList<>();
            // there could be no blocks sent
            if (buffer.readableBytes() > 0) {
                while (buffer.readableBytes() > 0) {
                    blocks.add(buffer.readUtf());
                }
            }
            return new ClimbingPayloadS2C(allowed, blockmode, blocks);
        } else {
            return new ClimbingPayloadS2C(allowed, ClimbeyBlockmode.DISABLED, null);
        }
    }


}
