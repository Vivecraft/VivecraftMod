package org.vivecraft.common.network.packet.c2s;

import net.minecraft.network.FriendlyByteBuf;
import org.vivecraft.common.network.packet.PayloadIdentifier;

/**
 * holds if the client started or stopped crawling
 *
 * @param crawling if the player started or stopped crawling
 */
public record CrawlPayloadC2S(boolean crawling) implements VivecraftPayloadC2S {

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CRAWL;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.crawling);
    }

    public static CrawlPayloadC2S read(FriendlyByteBuf buffer) {
        return new CrawlPayloadC2S(buffer.readBoolean());
    }
}
