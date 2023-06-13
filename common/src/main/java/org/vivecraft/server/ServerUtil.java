package org.vivecraft.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.client.utils.UpdateChecker;

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
        if (ServerConfig.getBoolean(ServerConfig.messagesEnabled) ||
            (!serverPlayer.server.isDedicatedServer() && (ServerConfig.getBoolean(ServerConfig.vive_only) || ServerConfig.getBoolean(ServerConfig.vr_only)))) {
            scheduler.schedule(() -> {
                // only do stuff, if the player is still on the server
                if(!serverPlayer.hasDisconnected()) {
                    ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
                    String message = "";

                    boolean isOpAndAllowed = ServerConfig.getBoolean(ServerConfig.allow_op) && serverPlayer.server.getPlayerList().isOp(serverPlayer.getGameProfile());

                    // kick non VR players
                    if (!isOpAndAllowed && ServerConfig.getBoolean(ServerConfig.vr_only)
                        && (vivePlayer == null || !vivePlayer.isVR())) {
                        serverPlayer.connection.disconnect(Component.literal(ServerConfig.getString(ServerConfig.messagesKickVROnly)));
                        return;
                    }

                    // kick non vivecraft players
                    if (!isOpAndAllowed && ServerConfig.getBoolean(ServerConfig.vive_only)
                        && (vivePlayer == null)) {
                        serverPlayer.connection.disconnect(Component.literal(ServerConfig.getString(ServerConfig.messagesKickViveOnly)));
                        return;
                    }


                    // welcome message
                    if (ServerConfig.getBoolean(ServerConfig.messagesEnabled)) {
                        // get the right message
                        if (vivePlayer == null) {
                            message = ServerConfig.getString(ServerConfig.messagesWelcomeVanilla);
                        } else if (!vivePlayer.isVR()) {
                            message = ServerConfig.getString(ServerConfig.messagesWelcomeNonVR);
                        } else if (vivePlayer.isSeated()) {
                            message = ServerConfig.getString(ServerConfig.messagesWelcomeSeated);
                        } else {
                            message = ServerConfig.getString(ServerConfig.messagesWelcomeVR);
                        }
                        // actually send the message, if there is one set
                        if (!message.isEmpty()) {
                            serverPlayer.server.getPlayerList().broadcastSystemMessage(Component.literal(message.formatted(serverPlayer.getName().getString())), false);
                        }
                    }
                }
            }, (long)(ServerConfig.getDouble(ServerConfig.messageKickDelay) * 1000), TimeUnit.MILLISECONDS);
        }
    }

    public static void sendUpdateNotificationIfOP (ServerPlayer serverPlayer) {
        if (ServerConfig.getBoolean(ServerConfig.checkForUpdate)) {
            // don't send update notifications on singleplayer
            if (serverPlayer.server.isDedicatedServer() && serverPlayer.server.getPlayerList().isOp(serverPlayer.getGameProfile())) {
                // check for update on not the main thread
                scheduler.schedule(() -> {
                    if (UpdateChecker.checkForUpdates()) {
                        serverPlayer.sendSystemMessage(Component.literal("Vivecraft update available: §a" + UpdateChecker.newestVersion));
                    }
                }, 0, TimeUnit.MILLISECONDS);
            }
        }
    }
    public static void registerCommands(CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vivecraft-server-config")
            .requires(source -> source.hasPermission(4)).then(
            Commands.literal("reload")
                        .executes(context -> {
            ServerConfig.init((action, path, incorrectValue, correctedValue) -> context.getSource().sendSystemMessage(Component.literal("Corrected §a[" + String.join("§r.§a", path) + "]§r: was '(" + incorrectValue.getClass().getSimpleName() + ")" + incorrectValue + "', is now '(" + correctedValue.getClass().getSimpleName() + ")" + correctedValue + "'")));
            return 1;
        })
            ));

                for (String setting : ServerConfig.getSettings()) {
            Class<?> clazz = ServerConfig.getObject(setting).getClass();
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

            if (!setting.equals(ServerConfig.climbeyBlocklist)) {
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting).then(
                        Commands.literal("set").then(
                            Commands.argument(argumentName, argument)
                                .executes(context -> {
                                    try {
                                        Object newValue = context.getArgument(argumentName, clazz);
                                        ServerConfig.setSetting(setting, newValue);
                                        context.getSource().sendSystemMessage(Component.literal("set §a[" + setting + "]§r to '" + newValue + "'"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 1;
                                })
                        )
                    )
                ));
            } else {
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting).then(
                        Commands.literal("add").then(
                            Commands.argument("block", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (var block : BuiltInRegistries.BLOCK.keySet()) {
                                        builder.suggest(block.toString());
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String newValue = context.getArgument("block", String.class);
                                    context.getSource().sendSystemMessage(Component.literal("added '" + newValue + "' to §a[" + setting + "]§r"));
                                    return 1;
                                })
                        )
                    )
                ));
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting).then(
                        Commands.literal("remove").then(
                            Commands.argument("block", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (var block : ServerConfig.getList(setting)) {
                                        builder.suggest(block);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String newValue = context.getArgument("block", String.class);
                                    List<?> list = ServerConfig.getList(setting);
                                    list.remove(newValue);
                                    ServerConfig.setSetting(setting, list);
                                    context.getSource().sendSystemMessage(Component.literal("removed '" + newValue + "' from §a[" + setting + "]§r"));
                                    context.getSource().sendSystemMessage(Component.literal("is now '" + ServerConfig.getList(setting)));
                                    return 1;
                                })
                        )
                    )
                ));
            }
            dispatcher.register(Commands.literal("vivecraft-server-config")
                .requires(source -> source.hasPermission(4)).then(
                Commands.literal(setting).then(
                    Commands.literal("reset")
                        .executes(context -> {
                            Object newValue = ServerConfig.resetOption(setting);
                            context.getSource().sendSystemMessage(Component.literal("reset §a[" + setting + "]§r to '" + newValue + "'"));
                            return 1;
                        })
                )
            ));
        }
    }

}
