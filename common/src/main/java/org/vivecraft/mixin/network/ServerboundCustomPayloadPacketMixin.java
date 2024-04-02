package org.vivecraft.mixin.network;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packets.VivecraftDataPacket;

import java.util.ArrayList;

@Mixin(value = {ServerboundCustomPayloadPacket.class})
public class ServerboundCustomPayloadPacketMixin {

    /**
     * catches the vivecraft server bound packets and processes them.
     * Neoforge handles that in {@link org.vivecraft.neoforge.event.ServerEvents#handleVivePacket}
     */
    /*@Inject(at = @At("HEAD"), method = "readPayload(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;", cancellable = true)
    private static void vivecraft$catchVivecraftPackets(ResourceLocation resourceLocation, FriendlyByteBuf friendlyByteBuf, CallbackInfoReturnable<CustomPacketPayload> cir) {
        if (CommonNetworkHelper.CHANNEL.equals(resourceLocation)) {
            cir.setReturnValue(new VivecraftDataPacket(friendlyByteBuf));
        }
    }*/
    @WrapOperation(at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;", ordinal = 0), method = "<clinit>")
    private static <E> ArrayList<E> bypassExpensiveCalculationIfNecessary(E[] elements, Operation<ArrayList<E>> original) {
        /*return original.call(ArrayUtils.add(elements,
            new CustomPacketPayload.TypeAndCodec<>(VivecraftDataPacket.TYPE, VivecraftDataPacket.STREAM_CODEC)));*/
        return (ArrayList<E>) Lists.newArrayList(ArrayUtils.add(elements,
            new CustomPacketPayload.TypeAndCodec<>(VivecraftDataPacket.TYPE, VivecraftDataPacket.STREAM_CODEC)));
    }
}
