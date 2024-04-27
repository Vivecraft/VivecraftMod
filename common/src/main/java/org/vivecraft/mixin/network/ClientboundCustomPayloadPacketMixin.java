package org.vivecraft.mixin.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packets.VivecraftDataPacket;

@Mixin(value = {ClientboundCustomPayloadPacket.class})
public class ClientboundCustomPayloadPacketMixin {

    /**
     * catches the vivecraft client bound packets so that they don't get discarded.
     * Neoforge handles that with the network events in {@link org.vivecraft.neoforge.event.ClientEvents#handleVivePacket}
     */
    // TODO 1.20.5
    /*@Inject(at = @At("HEAD"), method = "readPayload(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;", cancellable = true)
    private static void vivecraft$catchVivecraftPackets(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf, CallbackInfoReturnable<CustomPacketPayload> cir) {
        if (CommonNetworkHelper.CHANNEL.equals(resourceLocation)) {
            cir.setReturnValue(new VivecraftDataPacket(friendlyByteBuf));
        }
    }*/
}
