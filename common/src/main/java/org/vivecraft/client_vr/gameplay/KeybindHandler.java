package org.vivecraft.client_vr.gameplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.ClientUtils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.menuworlds.MenuWorldDownloader;
import org.vivecraft.client_vr.menuworlds.MenuWorldExporter;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class KeybindHandler {

    private static final Minecraft MC = Minecraft.getInstance();
    private static final ClientDataHolderVR DH = ClientDataHolderVR.getInstance();

    private static int PREVIOUS_ITEM_SLOT = 0;

    public static void processKeybindings() {
        // menuworld export
        if (VivecraftVRMod.INSTANCE.keyExportWorld.consumeClick() && MC.level != null && MC.player != null) {
            saveMenuworld(MC.level, MC.player);
        }

        // quick commands
        if (MC.player != null) {
            for (int i = 0; i < VivecraftVRMod.INSTANCE.keyQuickCommands.length; i++) {
                if (VivecraftVRMod.INSTANCE.keyQuickCommands[i].consumeClick()) {
                    String command = DH.vrSettings.vrQuickCommands[i];
                    if (command.startsWith("/")) {
                        MC.player.connection.sendCommand(command.substring(1));
                    } else {
                        MC.player.connection.sendChat(command);
                    }
                }
            }
        }

        // quck item swap
        if (MC.player != null && VivecraftVRMod.INSTANCE.keyQuickSwap.consumeClick()) {
            // don't swap while actively climbing
            if (!DH.climbTracker.isClimbingWith(InteractionHand.MAIN_HAND)) {
                if (MC.player.getInventory().getSelectedSlot() != 0) {
                    PREVIOUS_ITEM_SLOT = MC.player.getInventory().getSelectedSlot();
                    MC.player.getInventory().setSelectedSlot(0);
                } else {
                    MC.player.getInventory().setSelectedSlot(PREVIOUS_ITEM_SLOT);
                    PREVIOUS_ITEM_SLOT = 0;
                }
            }
        }
    }

    private static void saveMenuworld(ClientLevel clientLevel, Player player) {
        Throwable error = null;
        try {
            final BlockPos blockpos = player.blockPosition();
            int size = 320;
            int offset = size / 2;
            File dir = new File(MenuWorldDownloader.CUSTOM_WORLD_FOLDER);
            dir.mkdirs();

            File foundFile;
            for (int i = 0; ; i++) {
                foundFile = new File(dir, "world" + i + ".mmw");
                if (!foundFile.exists()) break;
            }

            VRSettings.LOGGER.info("Vivecraft: Exporting world... area size: {}", size);
            VRSettings.LOGGER.info("Vivecraft: Saving to {}", foundFile.getAbsolutePath());

            if (MC.isLocalServer()) {
                final Level serverLevel = MC.getSingleplayerServer().getLevel(player.level().dimension());
                File finalFoundFile = foundFile;
                CompletableFuture<Throwable> completablefuture = MC.getSingleplayerServer().submit(() -> {
                    try {
                        MenuWorldExporter.saveAreaToFile(serverLevel, blockpos.getX() - offset,
                            blockpos.getZ() - offset,
                            size, size, blockpos.getY(), finalFoundFile);
                    } catch (Throwable throwable) {
                        VRSettings.LOGGER.error("Vivecraft: error exporting menuworld:", throwable);
                        return throwable;
                    }
                    return null;
                });

                error = completablefuture.get();
            } else {
                MenuWorldExporter.saveAreaToFile(clientLevel, blockpos.getX() - offset, blockpos.getZ() - offset,
                    size, size, blockpos.getY(), foundFile);
                ClientUtils.addChatMessage(
                    Component.translatable("vivecraft.messages.menuworldexportclientwarning"));
            }

            if (error == null) {
                ClientUtils.addChatMessage(
                    Component.translatable("vivecraft.messages.menuworldexportcomplete.1", size));
                ClientUtils.addChatMessage(Component.translatable("vivecraft.messages.menuworldexportcomplete.2",
                    foundFile.getAbsolutePath()));
            }
        } catch (Throwable throwable) {
            VRSettings.LOGGER.error("Vivecraft: Error exporting Menuworld:", throwable);
            error = throwable;
        } finally {
            if (error != null) {
                ClientUtils.addChatMessage(
                    Component.translatable("vivecraft.messages.menuworldexporterror", error.getMessage()));
            }
        }
    }
}
