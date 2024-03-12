package org.vivecraft.client.network;

import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.IOUtils;
import org.vivecraft.client.VRPlayersClient;

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
                        } catch (Exception exception1) {
                            System.out.println("error with donors txt " + exception1.getMessage());
                        }
                    }

                    cache = userMap;
                } catch (Exception exception1) {
                    System.out.println("Error parsing data: " + url + ", " + exception1.getClass().getName() + ": " + exception1.getMessage());
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
                        String url1 = "https://www.vivecraft.org/patreon/current.txt";
                        String url2 = "https://raw.githubusercontent.com/Vivecraft/VivecraftSupporters/supporters/supporters.txt";
                        new Thread(() -> {
                            try {
                                String value1 = IOUtils.toString(new URL(url1), StandardCharsets.UTF_8);
                                String value2 = IOUtils.toString(new URL(url2), StandardCharsets.UTF_8);
                                fileDownloadFinished(url1, value1, false);
                                fileDownloadFinished(url2, value2, true);
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
