package org.vivecraft.client.network;

import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.IOUtils;
import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client_vr.settings.VRSettings;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SupporterReceiver {
    private static final Object lock = new Object();
    private static final List<Player> queuedPlayers = new LinkedList<>();
    private static Map<String, Integer> cache;
    private static boolean downloadStarted;
    private static boolean downloadFailed;

    private static void fileDownloadFinished(String url, String data, boolean addData) {
        synchronized (lock) {
            if (data != null) {
                try {
                    Map<String, Integer> userMap = new HashMap<>();
                    if (addData) {
                        userMap = cache;
                    }

                    String[] lines = data.split("\\r?\\n");

                    for (String user : lines) {
                        if (user.isEmpty()) {
                            continue;
                        }
                        try {
                            String[] bits = user.split(":");
                            int i = Integer.parseInt(bits[1]);
                            userMap.put(bits[0].toLowerCase(), i);

                            for (Player player : queuedPlayers) {
                                if (bits[0].equalsIgnoreCase(player.getGameProfile().getName())) {
                                    VRPlayersClient.getInstance().setHMD(player.getUUID(), i);
                                }
                            }
                        } catch (Exception e) {
                            VRSettings.logger.error("Vivecraft: error with supporters txt: {}", user,  e);
                        }
                    }

                    cache = userMap;
                } catch (Exception e) {
                    VRSettings.logger.error("Vivecraft: error parsing supporter data: {}", url, e);
                    downloadFailed = true;
                }
            } else {
                downloadFailed = true;
            }
        }
    }

    public static void addPlayerInfo(Player p) {
        if (!downloadFailed) {
            synchronized (lock) {
                if (cache == null) {
                    queuedPlayers.add(p);
                    VRPlayersClient.getInstance().setHMD(p.getUUID(), 0);

                    if (!downloadStarted) {
                        downloadStarted = true;
                        String ogSupportersUrl = "https://www.vivecraft.org/patreon/current.txt";
                        String viveModSupportersUrl = "https://raw.githubusercontent.com/Vivecraft/VivecraftSupporters/supporters/supporters.txt";
                        new Thread(() -> {
                            try {
                                String ogSupporters = IOUtils.toString(new URL(ogSupportersUrl), StandardCharsets.UTF_8);
                                String viveModSupporters = IOUtils.toString(new URL(viveModSupportersUrl), StandardCharsets.UTF_8);
                                fileDownloadFinished(ogSupportersUrl, ogSupporters, false);
                                fileDownloadFinished(viveModSupportersUrl, viveModSupporters, true);
                                synchronized (lock) {
                                    queuedPlayers.clear();
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();
                    }
                } else {
                    VRPlayersClient.getInstance().setHMD(p.getUUID(), cache.getOrDefault(p.getGameProfile().getName().toLowerCase(), 0));
                }
            }
        }
    }
}
