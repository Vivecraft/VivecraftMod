package org.vivecraft.mixin.client.multiplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.ClientTelemetryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.api.NetworkHelper;
import org.vivecraft.render.PlayerModelController;

import java.util.UUID;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(Minecraft minecraft, Screen screen, Connection connection, GameProfile gameProfile, ClientTelemetryManager clientTelemetryManager, CallbackInfo ci) {
        NetworkHelper.resetServerSettings();
        NetworkHelper.displayedChatMessage = false;
    }

    @Inject(at = @At("TAIL"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void login(ClientboundLoginPacket p_105030_, CallbackInfo callback) {
        NetworkHelper.vivePlayers.clear();
        NetworkHelper.sendVersionInfo();
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0, shift = Shift.AFTER), method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V")
    public void respawn(ClientboundRespawnPacket packet, CallbackInfo callback) {
        NetworkHelper.resetServerSettings();
        NetworkHelper.sendVersionInfo();
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getData()Lnet/minecraft/network/FriendlyByteBuf;"),
            method = "handleCustomPayload(Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;)V",
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void handlepacket(ClientboundCustomPayloadPacket p_105004_, CallbackInfo info, ResourceLocation resourcelocation, FriendlyByteBuf friendlybytebuf) {
        if (resourcelocation.getNamespace().equalsIgnoreCase("vivecraft")) {
            if (resourcelocation.getPath().equalsIgnoreCase("data")) {
                byte b0 = friendlybytebuf.readByte();
                NetworkHelper.PacketDiscriminators networkhelper$packetdiscriminators = NetworkHelper.PacketDiscriminators.values()[b0];

                switch (networkhelper$packetdiscriminators) {
                    case VERSION:
                        String s11 = friendlybytebuf.readUtf(1024);

                        if (!NetworkHelper.displayedChatMessage) {
                            NetworkHelper.displayedChatMessage = true;
                            this.minecraft.gui.getChat().addMessage(new TranslatableComponent("vivecraft.messages.serverplugin", s11));
                        }

                        break;

                    case REQUESTDATA:
                        NetworkHelper.serverWantsData = true;
                        break;

                    case CLIMBING:

                        break;

                    case TELEPORT:
                        NetworkHelper.serverSupportsDirectTeleport = true;
                        break;

                    case UBERPACKET:
                        long olong = friendlybytebuf.readLong();
                        long olong1 = friendlybytebuf.readLong();
                        byte[] abyte = new byte[29];
                        byte[] abyte1 = new byte[29];
                        byte[] abyte2 = new byte[29];
                        friendlybytebuf.readBytes(29).getBytes(0, abyte);
                        friendlybytebuf.readBytes(29).getBytes(0, abyte1);
                        friendlybytebuf.readBytes(29).getBytes(0, abyte2);
                        UUID uuid2 = new UUID(olong, olong1);
                        float f3 = 1.0F;
                        float f4 = 1.0F;

                        if (friendlybytebuf.isReadable()) {
                            f3 = friendlybytebuf.readFloat();
                        }

                        if (friendlybytebuf.isReadable()) {
                            f4 = friendlybytebuf.readFloat();
                        }

                        PlayerModelController.getInstance().Update(uuid2, abyte, abyte1, abyte2, f3, f4);
                        break;

                    case SETTING_OVERRIDE:
                        break;

                    case CRAWL:
                        NetworkHelper.serverAllowsCrawling = true;
                }

				info.cancel();
            }
        }
    }
}
