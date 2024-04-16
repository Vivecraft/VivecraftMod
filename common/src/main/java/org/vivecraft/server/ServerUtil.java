package org.vivecraft.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.*;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.vivecraft.client.utils.UpdateChecker;
import org.vivecraft.server.config.ConfigBuilder;
import org.vivecraft.server.config.ServerConfig;

import java.util.IllegalFormatException;
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
                if (!serverPlayer.hasDisconnected()) {
                    ServerVivePlayer vivePlayer = ServerVRPlayers.getVivePlayer(serverPlayer);
                    String message = "";

                    boolean isOpAndAllowed = ServerConfig.allow_op.get() && serverPlayer.server.getPlayerList().isOp(serverPlayer.getGameProfile());

                    // kick non VR players
                    if (!isOpAndAllowed && ServerConfig.vr_only.get()
                        && (vivePlayer == null || !vivePlayer.isVR())) {
                        String kickMessage = ServerConfig.messagesKickVROnly.get();
                        try {
                            kickMessage = kickMessage.formatted(serverPlayer.getName().getString());
                        } catch (IllegalFormatException e) {
                            // catch errors users might put into the messages, to not crash other stuff
                            ServerNetworking.LOGGER.error("KickVROnly message '{}' has errors: {}", kickMessage, e.toString());
                        }
                        serverPlayer.connection.disconnect(Component.literal(kickMessage));
                        return;
                    }

                    // kick non vivecraft players
                    if (!isOpAndAllowed && ServerConfig.vive_only.get()
                        && (vivePlayer == null)) {
                        String kickMessage = ServerConfig.messagesKickViveOnly.get();
                        try {
                            kickMessage = kickMessage.formatted(serverPlayer.getName().getString());
                        } catch (IllegalFormatException e) {
                            // catch errors users might put into the messages, to not crash other stuff
                            ServerNetworking.LOGGER.error("KickViveOnly message '{}' has errors: {}", kickMessage, e.toString());
                        }
                        serverPlayer.connection.disconnect(Component.literal(kickMessage));
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
                            try {
                                serverPlayer.server.getPlayerList().broadcastSystemMessage(Component.literal(message.formatted(serverPlayer.getName().getString())), false);
                            } catch (IllegalFormatException e) {
                                // catch errors users might put into the messages, to not crash other stuff
                                ServerNetworking.LOGGER.error("Welcome message '{}' has errors: {}", message, e.toString());
                            }
                        }
                    }
                }
            }, (long) (ServerConfig.messageKickDelay.get() * 1000), TimeUnit.MILLISECONDS);
        }
    }

    public static void sendUpdateNotificationIfOP(ServerPlayer serverPlayer) {
        if (ServerConfig.checkForUpdate.get()) {
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
                Commands.literal("reload").executes(context -> {
                    ServerConfig.init((action, path, incorrectValue, correctedValue) -> context.getSource()
                        .sendSystemMessage(Component.literal("Corrected §a[" + String.join("§r.§a", path) + "]§r: was '(" + incorrectValue.getClass().getSimpleName() + ")" + incorrectValue + "', is now '(" + correctedValue.getClass().getSimpleName() + ")" + correctedValue + "'")));
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

            if (setting instanceof ConfigBuilder.InListValue<?> inListValue) {
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                        Commands.literal(inListValue.getPath()).then(
                            Commands.literal("set").then(
                                Commands.argument(argumentName, argument)
                                    .suggests((context, builder) -> {
                                        for (var value : inListValue.getValidValues()) {
                                            builder.suggest(value.toString());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        try {
                                            Object newValue = context.getArgument(argumentName, clazz);
                                            setting.set(newValue);
                                            context.getSource().sendSystemMessage(Component.literal("set §a[" + setting.getPath() + "]§r to '" + newValue + "'"));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return 1;
                                    })
                            )
                        )
                    )
                );
            } else if (!(setting.get() instanceof List)) {
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                        Commands.literal(setting.getPath()).then(
                            Commands.literal("set").then(
                                Commands.argument(argumentName, argument)
                                    .executes(context -> {
                                        try {
                                            Object newValue = context.getArgument(argumentName, clazz);
                                            setting.set(newValue);
                                            context.getSource().sendSystemMessage(Component.literal("set §a[" + setting.getPath() + "]§r to '" + newValue + "'"));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        return 1;
                                    })
                            )
                        )
                    )
                );
            } else {
                ConfigBuilder.ConfigValue<List<? extends String>> listConfig = setting;
                dispatcher.register(Commands.literal("vivecraft-server-config")
                    .requires(source -> source.hasPermission(4)).then(
                        Commands.literal(setting.getPath()).then(
                            Commands.literal("add").then(
                                Commands.argument("block", StringArgumentType.greedyString())
                                    .suggests((context, builder) -> {
                                        for (var block : BuiltInRegistries.BLOCK.keySet()) {
                                            builder.suggest(block.toString());
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        String newValue = context.getArgument("block", String.class);
                                        List list = listConfig.get();
                                        list.add(newValue);
                                        listConfig.set(list);
                                        context.getSource().sendSystemMessage(Component.literal("added '" + newValue + "' to §a[" + setting.getPath() + "]§r"));
                                        context.getSource().sendSystemMessage(Component.literal("is now '" + setting.get()));
                                        return 1;
                                    })
                            )
                        )
                    )
                );
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
                                        context.getSource().sendSystemMessage(Component.literal("removed '" + newValue + "' from §a[" + setting.getPath() + "]§r"));
                                        context.getSource().sendSystemMessage(Component.literal("is now '" + setting.get()));
                                        return 1;
                                    })
                            )
                        )
                    )
                );
            }
            dispatcher.register(Commands.literal("vivecraft-server-config")
                .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting.getPath()).then(
                        Commands.literal("reset")
                            .executes(context -> {
                                Object newValue = setting.reset();
                                context.getSource().sendSystemMessage(Component.literal("reset §a[" + setting.getPath() + "]§r to '" + newValue + "'"));
                                return 1;
                            })
                    )
                )
            );
            dispatcher.register(Commands.literal("vivecraft-server-config")
                .requires(source -> source.hasPermission(4)).then(
                    Commands.literal(setting.getPath())
                        .executes(context -> {
                            context.getSource().sendSystemMessage(Component.literal("§a[" + setting.getPath() + "]§r is set to '" + setting.get() + "'"));
                            return 1;
                        })
                )
            );
        }
    }
}
