//package org.vivecraft.server;
//
//import net.minecraft.network.chat.Component;
//import net.minecraft.network.chat.TextComponent;
//import net.minecraft.server.MinecraftServer;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.damagesource.DamageSource;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.Mob;
//import net.minecraft.world.entity.ai.goal.Goal;
//import net.minecraft.world.entity.ai.goal.GoalSelector;
//import net.minecraft.world.entity.ai.goal.SwellGoal;
//import net.minecraft.world.entity.ai.goal.WrappedGoal;
//import net.minecraft.world.entity.monster.Creeper;
//import net.minecraft.world.entity.monster.EnderMan;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.entity.projectile.AbstractArrow;
//import net.minecraft.world.entity.projectile.Arrow;
//import net.minecraft.world.entity.projectile.Projectile;
//import net.minecraft.world.entity.projectile.ThrownTrident;
//import net.minecraft.world.phys.Vec3;
//import org.vivecraft.api.CommonNetworkHelper;
//import org.vivecraft.api.ServerVivePlayer;
//import org.vivecraft.config.ServerConfig;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Function;
//import java.util.function.Supplier;
//
//public class Events {
//
//    public static void onAttackEntity(ServerPlayer target, ServerPlayer player, Function<Boolean, Void> cancel) {
//        if (CommonNetworkHelper.isVive(player)) {
//            ServerVivePlayer data = CommonNetworkHelper.vivePlayers.get(player);
//            if (data.isSeated()) { // Seated VR vs...
//                if (CommonNetworkHelper.isVive(target)) {
//                    ServerVivePlayer targetData = CommonNetworkHelper.vivePlayers.get(target);
//                    if (targetData.isSeated()) { // ...seated VR
//                        if (!ServerConfig.seatedVrVsSeatedVR.get()) cancel.apply(true);
//                    } else { // ...VR
//                        if (!ServerConfig.vrVsSeatedVR.get()) cancel.apply(true);
//                    }
//                } else { // ...non-VR
//                    if (!ServerConfig.seatedVrVsNonVR.get()) cancel.apply(true);
//                }
//            } else { // VR vs...
//                if (CommonNetworkHelper.isVive(target)) {
//                    ServerVivePlayer targetData = CommonNetworkHelper.vivePlayers.get(target);
//                    if (targetData.isSeated()) { // ...seated VR
//                        if (!ServerConfig.vrVsSeatedVR.get()) cancel.apply(true);
//                    } else { // ...VR
//                        if (!ServerConfig.vrVsVR.get()) cancel.apply(true);
//                    }
//                } else { // ...non-VR
//                    if (!ServerConfig.vrVsNonVR.get()) cancel.apply(true);
//                }
//            }
//        } else { // Non-VR vs...
//            if (CommonNetworkHelper.isVive(target)) {
//                ServerVivePlayer targetData = CommonNetworkHelper.vivePlayers.get(target);
//                if (targetData.isSeated()) { // ...seated VR
//                    if (!ServerConfig.seatedVrVsNonVR.get()) cancel.apply(true);
//                } else { // ...VR
//                    if (!ServerConfig.vrVsNonVR.get()) cancel.apply(true);
//                }
//            }
//        }
//    }
//
//    public void onArrowLoose(ServerPlayer player, Function<Integer, Void> charge) {
//        ServerVivePlayer data = CommonNetworkHelper.vivePlayers.get(player);
//        if (data != null && !data.isSeated() && data.getDraw() > 0) {
//            charge.apply(Math.round(data.getDraw() * 20));
//        }
//    }
//
//    public void onLivingHurt(LivingEntity target, DamageSource source, Supplier<Float> getAmount, Function<Float, Void> setAmount) {
//        if (source.getDirectEntity() instanceof Arrow && source.getEntity() instanceof Player) {
//            Arrow arrow = (Arrow) source.getDirectEntity();
//            ServerPlayer attacker = (ServerPlayer)source.getEntity();
//            if (CommonNetworkHelper.isVive(attacker)) {
//                ServerVivePlayer data = CommonNetworkHelper.vivePlayers.get(attacker);
//                boolean headshot = isHeadshot(target, arrow);
//                if (data.isSeated()) {
//                    if (headshot) setAmount.apply(getAmount.get() * ServerConfig.bowSeatedHeadshotMul.get().floatValue());
//                    else setAmount.apply(getAmount.get() * ServerConfig.bowSeatedMul.get().floatValue());
//                } else {
//                    if (headshot) setAmount.apply(getAmount.get() * ServerConfig.bowStandingHeadshotMul.get().floatValue());
//                    else setAmount.apply(getAmount.get() * ServerConfig.bowStandingMul.get().floatValue());
//                }
//            }
//        }
//    }
//
//    public static boolean isHeadshot(LivingEntity target, Arrow arrow) {
//        if (target.isPassenger()) return false;
//        if (target instanceof Player) {
//            Player player = (Player) target;
//            if (player.isShiftKeyDown()) {
//                //totalHeight = 1.65;
//                //bodyHeight = 1.20;
//                //headHeight = 0.45;
//                if (arrow.getY() >= player.getY() + 1.20) return true;
//            } else {
//                //totalHeight = 1.80;
//                //bodyHeight = 1.35;
//                //headHeight = 0.45;
//                if (arrow.getY() >= player.getY() + 1.35) return true;
//            }
//        } else {
//            // TODO: mobs
//        }
//        return false;
//    }
//
//    public void onEntityJoinWorld(Entity entity, MinecraftServer server) {
//        if (entity instanceof ServerPlayer player) {
//            if (ServerConfig.vrOnly.get() && !player.hasPermissions(2)) {
//                scheduler.schedule(() -> {
//                    server.submit(() -> {
//                        if (player.connection.getConnection().isConnected() && !CommonNetworkHelper.isVive(player)) {
//                            server.sendMessage(new TextComponent(ServerConfig.vrOnlyKickMessage.get()), player.getUUID());
//                            server.sendMessage(new TextComponent("If this is not a VR client, you will be kicked in " + ServerConfig.vrOnlyKickDelay.get() + " seconds."), player.getUUID());
//                            scheduler.schedule(() -> {
//                                server.submit(() -> {
//                                    if (player.connection.getConnection().isConnected() && CommonNetworkHelper.isVive(player)) {
//                                        player.connection.disconnect(new TextComponent(ServerConfig.vrOnlyKickMessage.get()));
//                                    }
//                                });
//                            }, Math.round(ServerConfig.vrOnlyKickDelay.get() * 1000), TimeUnit.MILLISECONDS);
//                        }
//                    });
//                }, 1000, TimeUnit.MILLISECONDS);
//            }
//        } else if (entity instanceof Projectile projectile) {
//            if (!(projectile.getOwner() instanceof Player))
//                return;
//            ServerPlayer shooter = (ServerPlayer)projectile.getOwner();
//            if (!CommonNetworkHelper.isVive(shooter))
//                return;
//
//            boolean arrow = projectile instanceof AbstractArrow && !(projectile instanceof ThrownTrident);
//            ServerVivePlayer data = CommonNetworkHelper.vivePlayers.get(shooter);
//            Vec3 pos = data.getControllerPos(data.activeHand, shooter);
//            Vec3 aim = data.getControllerDir(data.activeHand);
//
//            if (arrow && !data.isSeated() && data.getDraw() > 0) {
//                pos = data.getControllerPos(0, shooter);
//                aim = data.getControllerDir(1);
//            }
//
//            pos = pos.add(aim.scale(0.6));
//            double vel = projectile.getDeltaMovement().length();
//            projectile.setPos(pos.x, pos.y, pos.z);
//            projectile.shoot(aim.x, aim.y, aim.z, (float)vel, 0.0f);
//
//            Vec3 shooterMotion = shooter.getDeltaMovement();
//            projectile.setDeltaMovement(projectile.getDeltaMovement().add(shooterMotion.x, shooter.isOnGround() ? 0.0 : shooterMotion.y, shooterMotion.z));
//
//        } else if (entity instanceof Creeper creeper) {
//            replaceAIGoal(creeper, creeper.goalSelector, SwellGoal.class, () -> new VRCreeperSwellGoal(creeper));
//        } else if (entity instanceof EnderMan enderman) {
//            replaceAIGoal(enderman, enderman.goalSelector, EnderMan.EndermanFreezeWhenLookedAt.class, () -> new VREndermanStareGoal(enderman));
//            replaceAIGoal(enderman, enderman.targetSelector, EnderMan.EndermanLookForPlayerGoal.class, () -> new VREndermanFindPlayerGoal(enderman, enderman::isAngryAt));
//        }
//    }
//
//    public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//    static {
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                scheduler.shutdownNow();
//            }
//        });
//    }
//
//    public static void replaceAIGoal(Mob entity, GoalSelector goalSelector, Class<? extends Goal> targetGoal, Supplier<Goal> newGoalSupplier) {
//        WrappedGoal goal = goalSelector.availableGoals.stream().filter((g) -> targetGoal.isInstance(g.getGoal())).findFirst().orElse(null);
//        if (goal != null) {
//            goalSelector.removeGoal(goal.getGoal());
//            goalSelector.addGoal(goal.getPriority(), newGoalSupplier.get());
//        } else {
//        }
//    }
//
//}
