package org.vivecraft.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import net.minecraft.Util;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.server.config.ConfigBuilder;
import org.vivecraft.server.config.ServerConfig;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerUtil {

    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(scheduler::shutdownNow));
    }

    public static void scheduleWelcomeMessageOrKick(ServerPlayer serverPlayer) {
        if (ServerConfig.messagesEnabled.get() ||
            (ServerConfig.vive_only.get() || ServerConfig.vr_only.get())) {
            scheduler.schedule(() -> {
                // only do stuff, if the player is still on the server
                if(!serverPlayer.hasDisconnected()) {
                    ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
                    String message = "";

                    boolean isOpAndAllowed = ServerConfig.allow_op.get() && serverPlayer.server.getPlayerList().isOp(serverPlayer.getGameProfile());

                    // kick non VR players
                    if (!isOpAndAllowed && ServerConfig.vr_only.get()
                        && (vivePlayer == null || !vivePlayer.isVR())) {
                        serverPlayer.connection.disconnect(new TextComponent(ServerConfig.messagesKickVROnly.get()));
                        return;
                    }

                    // kick non vivecraft players
                    if (!isOpAndAllowed && ServerConfig.vive_only.get()
                        && (vivePlayer == null)) {
                        serverPlayer.connection.disconnect(new TextComponent(ServerConfig.messagesKickViveOnly.get()));
                        return;
                    }


                    // welcome message
                    if (ServerConfig.messagesEnabled.get()) {
                        // get the right message
                        if (vivePlayer == null) {
                            message = ServerConfig.messagesWelcomeVanilla.get();
                        } else if (!vivePlayer.isVR()) {
                            message = ServerConfig.messagesWelcomeNonVR.get();
                        } else if (vivePlayer.isSeated()) {
                            message = ServerConfig.messagesWelcomeSeated.get();
                        } else {
                            message = ServerConfig.messagesWelcomeVR.get();
                        }
                        // actually send the message, if there is one set
                        if (!message.isEmpty()) {
                            serverPlayer.server.getPlayerList().broadcastMessage(new TextComponent(message.formatted(serverPlayer.getName().getString())), ChatType.SYSTEM, Util.NIL_UUID);
                        }
                    }
                }
            }, (long)(ServerConfig.messageKickDelay.get() * 1000), TimeUnit.MILLISECONDS);
        }
    }

    public static void sendUpdateNotificationIfOP (ServerPlayer serverPlayer) {
        if (ServerConfig.checkForUpdate.get()) {
            // don't send update notifications on singleplayer
            if (serverPlayer.server.isDedicatedServer() && serverPlayer.server.getPlayerList().isOp(serverPlayer.getGameProfile())) {
                // check for update on not the main thread
                scheduler.schedule(() -> {
                    if (UpdateChecker.checkForUpdates()) {
                        serverPlayer.sendMessage(new TextComponent("Vivecraft update available: §a" + UpdateChecker.newestVersion), Util.NIL_UUID);
                    }
                }, 0, TimeUnit.MILLISECONDS);
            }
        }
    }
    public static void registerCommands(CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vivecraft-server-config")
            .requires(source -> source.hasPermission(4)).then(
                Commands.literal("reload").executes(context -> {
                    ServerConfig.init((action, path, incorrectValue, correctedValue) -> context.getSource()
                        .sendSuccess(new TextComponent("Corrected §a[" + String.join("§r.§a", path) + "]§r: was '(" + incorrectValue.getClass().getSimpleName() + ")" + incorrectValue + "', is now '(" + correctedValue.getClass().getSimpleName() + ")" + correctedValue + "'"), true));
                    return 1;
                })
            )
        );

        for (var setting : ServerConfig.getConfigValues()) {
            Class<?> clazz = setting.get().getClass();
            final ArgumentType<?> argument;
            String argumentName;
            if (clazz == Integer.class) {
                argumentName = "int";
                argument = IntegerArgumentType.integer();
            } else if (clazz == Double.class) {
                argumentName = "double";
                argument = DoubleArgumentType.doubleArg();
            } else if (clazz == Boolean.class) {
                argumentName = "bool";
                argument = BoolArgumentType.bool();
            } else {
                argumentName = "string";
                argument = StringArgumentType.string();
            }

            if (!(setting.get()instanceof List)) {
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting.getPath()).then(
                        Commands.literal("set").then(
                            Commands.argument(argumentName, argument)
                                .executes(context -> {
                                    try {
                                        Object newValue = context.getArgument(argumentName, clazz);
                                        setting.set(newValue);
                                        context.getSource().sendSuccess(new TextComponent("set §a[" + setting.getPath() + "]§r to '" + newValue + "'"), true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 1;
                                })
                        )
                    )
                ));
            } else {
                ConfigBuilder.ConfigValue<List<? extends String>> listConfig = setting;
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting.getPath()).then(
                        Commands.literal("add").then(
                            Commands.argument("block", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    for (var block : Registry.BLOCK.keySet()) {
                                        builder.suggest(block.toString());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String newValue = context.getArgument("block", String.class);
                                    List list = listConfig.get();
                                    list.add(newValue);
                                    listConfig.set(list);
                                    context.getSource().sendSuccess(new TextComponent("added '" + newValue + "' to §a[" + setting.getPath() + "]§r"), true);
                                    context.getSource().sendSuccess(new TextComponent("is now '" + setting.get()), true);
                                    return 1;
                                })
                        )
                    )
                ));
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting.getPath()).then(
                        Commands.literal("remove").then(
                            Commands.argument("block", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    for (String block : listConfig.get()) {
                                        builder.suggest(block);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String newValue = context.getArgument("block", String.class);
                                    List<? extends String> list = listConfig.get();
                                    list.remove(newValue);
                                    listConfig.set(list);
                                    context.getSource().sendSuccess(new TextComponent("removed '" + newValue + "' from §a[" + setting.getPath() + "]§r"), true);
                                    context.getSource().sendSuccess(new TextComponent("is now '" + setting.get()), true);
                                    return 1;
                                })
                        )
                    )
                ));
            }
            dispatcher.register(Commands.literal("vivecraft-server-config")
                .requires(source -> source.hasPermission(4)).then(
                Commands.literal(setting.getPath()).then(
                    Commands.literal("reset")
                        .executes(context -> {
                            Object newValue = setting.reset();
                            context.getSource().sendSuccess(new TextComponent("reset §a[" + setting.getPath() + "]§r to '" + newValue + "'"), true);
                            return 1;
                        })
                )
            ));
            dispatcher.register(Commands.literal("vivecraft-server-config")
                .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting.getPath())
                        .executes(context -> {
                            context.getSource().sendSuccess(new TextComponent("§a[" + setting.getPath() + "]§r is set to '" + setting.get() + "'"), true);
                            return 1;
                        })
                ));
        }
    }

}
