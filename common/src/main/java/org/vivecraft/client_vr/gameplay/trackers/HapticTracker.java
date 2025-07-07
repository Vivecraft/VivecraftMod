package org.vivecraft.client_vr.gameplay.trackers;

import com.bhaptics.haptic.models.PositionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.client.Tracker;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.bodylink.Haptics;
import org.vivecraft.client_vr.bodylink.RiggedBody;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.packet.c2s.DamageDirectionPayloadC2S;
import org.vivecraft.common.utils.MathUtils;
import org.vivecraft.common.utils.Utils;

import java.util.ArrayList;
import java.util.Random;


public class HapticTracker implements Tracker {

    private static final int HUNGER_THRESHOLD = 15;
    private static final double MAX_EXPLOSION_DIST = 5;

    private final Random random = new Random();
    private final ArrayList<HapticsModule> modules = new ArrayList<>();

    private float lastHealth;

    private Vector3fc lastHitDirection = null;
    private float lastVanillaHurtYaw = 0f;

    private final Minecraft mc;
    private final ClientDataHolderVR dh;

    public HapticTracker(Minecraft mc, ClientDataHolderVR dh) {
        this.mc = mc;
        this.dh = dh;
        this.modules.add(new RainModule());
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        return player != null && Haptics.isConnected();
    }

    @Override
    public ProcessType processType() {
        return ProcessType.PER_TICK;
    }

    @Override
    public void activeProcess(LocalPlayer player) {
        //TODO Find better place for this
        RiggedBody.getInstance().updatePose(this.dh.vrPlayer.getVRDataWorld());

        if (ClientNetworking.USED_NETWORK_VERSION >= CommonNetworkHelper.NETWORK_VERSION_DAMAGE_DIRECTION &&
            !ClientNetworking.REQUESTED_DAMAGE_DIRECTION)
        {
            ClientNetworking.sendServerPacket(new DamageDirectionPayloadC2S());
            ClientNetworking.REQUESTED_DAMAGE_DIRECTION = true;
        }

        float thresholdLowHealth = 5;
        float thresholdCriticalHealth = 2;

        Haptics.setLoopState(Haptics.Animations.FIRE, player.isOnFire());
        Haptics.setLoopState(Haptics.Animations.POTION_POSITIVE, hasPotionPositive(player));
        Haptics.setLoopState(Haptics.Animations.POTION_NEGATIVE, hasPotionNegative(player));
        Haptics.setLoopState(Haptics.Animations.LOW_HEALTH,
            player.getHealth() < thresholdLowHealth && !(player.getHealth() < thresholdCriticalHealth));
        Haptics.setLoopState(Haptics.Animations.CRITICAL_HEALTH, player.getHealth() < thresholdCriticalHealth);
        Haptics.setLoopState(Haptics.Animations.RAIN, isInRain(player));

        for (HapticsModule module : this.modules) {
            if (module.enabled) {
                module.tick(player);
            }
        }

        if (player.getHealth() != this.lastHealth) {
            float damage = this.lastHealth - player.getHealth();
            if (damage > 0) {
                handleHit(player.getLastDamageSource(), damage);
            }
            this.lastHealth = player.getHealth();
        }

        doHunger(player);

        Haptics.tick();
    }


    private void doHunger(LocalPlayer player) {
        int food = player.getFoodData().getFoodLevel();
        if (food < HUNGER_THRESHOLD) {
            float foodPerc = (float) food / 20;
            if (this.random.nextInt(20 * 3 + (int) (foodPerc * 30 * 20)) == 0) {
                Haptics.getAnimation(Haptics.Animations.HUNGER).playSingle(false, null);
            }
        }
    }


    private boolean hasPotionPositive(LocalPlayer player) {
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (effect.getEffect().value().isBeneficial()
                && !effect.isAmbient())
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasPotionNegative(LocalPlayer player) {
        return player.getActiveEffects().stream().anyMatch(
            effect -> effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL && !effect.isAmbient());
    }

    private boolean isInRain(LocalPlayer player) {
        BlockPos blockpos = player.blockPosition();
        return player.clientLevel.isRainingAt(blockpos) || player.clientLevel.isRainingAt(
            new BlockPos(blockpos.getX(), (int) player.getBoundingBox().maxY, blockpos.getZ()));
    }

    private void handleHit(DamageSource damageSrc, float damageAmount) {
        Vector3fc dmgVec = null;
        if (damageSrc != null) {
            // use the damage source if available
            dmgVec = Utils.getDirFromDamageSource(damageSrc, this.mc.player);
        }
        if (dmgVec == null || MathUtils.isZero(dmgVec)) {
            if (this.lastHitDirection != null) {
                // got a direction from the server plugin
                dmgVec = this.lastHitDirection;
            } else if (this.lastVanillaHurtYaw != this.mc.player.getHurtDir()) {
                // use the vanilla hurt yaw
                // hurt dir is player local, and doesn't clear for non-directional damage
                this.lastVanillaHurtYaw = this.mc.player.getHurtDir();
                dmgVec = new Vector3f(1, 0, 0).rotateY(
                    (-this.lastVanillaHurtYaw - this.mc.player.getYRot()) * Mth.DEG_TO_RAD);
            }
            // else, no direction
        }
        // reset server hit
        this.lastHitDirection = null;
        if (dmgVec != null && !MathUtils.isZero(dmgVec)) {
            if (dmgVec.y() == 1F) {
                Haptics.getAnimation(Haptics.Animations.TOP_HIT).playSingle(true, null);
            } else if (dmgVec.y() == -1F) {
                Haptics.getAnimation(Haptics.Animations.BOTTOM_HIT).playSingle(true, null);
            } else {
                dmgVec = dmgVec.rotateY(this.dh.vrPlayer.getVRDataWorld().getBodyYawRad() + Mth.PI, new Vector3f());
                Haptics.getAnimation(Haptics.Animations.GENERIC_HIT).playSingle(true, dmgVec);
            }
        } else {
            Haptics.getAnimation(Haptics.Animations.ALL_AROUND_HIT).playSingle(true, null);
        }
    }

    // handlers called from outside
    public void handleExplode(Vec3 explosionPos) {
        if (this.isActive(this.mc.player)) {
            double explosionDist = explosionPos.subtract(this.mc.player.position()).length();
            if (explosionDist < MAX_EXPLOSION_DIST) {
                double distFactor = 1.0 - (explosionDist / MAX_EXPLOSION_DIST);
                Haptics.getAnimation(Haptics.Animations.EXPLOSION).playSingle(true, null, distFactor);
            }
        }
    }

    public void handleEat(ItemStack itemStack) {
        if (this.isActive(this.mc.player)) {
            if (itemStack.get(DataComponents.FOOD) != null && itemStack.get(DataComponents.CONSUMABLE) != null) {
                if (itemStack.get(DataComponents.CONSUMABLE).onConsumeEffects().isEmpty()) {
                    Haptics.getAnimation(Haptics.Animations.CONSUME).playSingle(true, null);
                } else {
                    Haptics.getAnimation(Haptics.Animations.CONSUME_EFFECT).playSingle(true, null);
                }
            }
        }
    }

    public void setLastHitDirection(Vector3fc lastHitDirection) {
        this.lastHitDirection = lastHitDirection;
    }

    private abstract static class HapticsModule {
        boolean enabled = false;

        abstract void tick(LocalPlayer player);
    }

    private static class RainModule extends HapticsModule {
        // Range: 0 to 1
        private static final double MIN_ANGLE = 0;
        private static final double DROP_CHANCE_THRESHOLD = 0.2;


        Random random = new Random();

        public RainModule() {
            super();
            this.enabled = false;
        }

        @Override
        void tick(LocalPlayer player) {
            if (!player.clientLevel.isRaining()) return;

            boolean isSnow = player.clientLevel.getBiome(player.blockPosition()).value()
                .coldEnoughToSnow(player.blockPosition(), player.level().getSeaLevel());

            // Terminal Velocity of rain in m/s
            Vec3 rainFall = new Vec3(0, -9, 0);

            // Add inverse player motion for relative motion
            Vector3f rainDir = MathUtils.subtractToVector3f(rainFall, player.getDeltaMovement()).normalize().mul(-1);

            ArrayList<RiggedBody.HapticPoint> points = RiggedBody.getInstance().getHapticPoints(PositionType.All);

            //Debug d = Debug .get("hapticsrain");

            for (RiggedBody.HapticPoint p : points) {
                // Check Occlusion
                if (!player.clientLevel.isRainingAt(player.blockPosition())) {
                    continue;
                }

                Vector3f normal = p.getNormal(true);

                //d.drawVector("vec:"+p.hashCode(),p.getPosWorld(player), normal, Color.red);

                double exposure = normal.dot(rainDir);

                if (exposure < MIN_ANGLE) {
                    // cull backface
                    exposure = 0;
                }

                double snowFactor = isSnow ? 2.0 : 1.0;

                if (Math.abs(this.random.nextGaussian()) * exposure > DROP_CHANCE_THRESHOLD * snowFactor) {
                    int intensity = 10; // TODO Randomize
                    int duration = 10;
                    p.motor.dot(intensity, duration);
                }
            }
        }
    }
}
