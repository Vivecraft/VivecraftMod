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
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.bodylink.Haptics;
import org.vivecraft.client_vr.bodylink.RiggedBody;
import org.vivecraft.common.utils.MathUtils;

import java.util.ArrayList;
import java.util.Random;


public class HapticTracker extends Tracker {

    private static final int HUNGER_THRESHOLD = 15;
    private static final double MAX_EXPLOSION_DIST = 5;

    private final Random random = new Random();
    private final ArrayList<HapticsModule> modules = new ArrayList<>();

    private float lastHealth;

    public HapticTracker(Minecraft mc, ClientDataHolderVR dh) {
        super(mc, dh);
        this.modules.add(new RainModule());
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        return Haptics.isConnected();
    }

    @Override
    public void doProcess(LocalPlayer player) {
        //TODO Find better place for this
        RiggedBody.getInstance().updatePose(ClientDataHolderVR.getInstance().vrPlayer.getVRDataWorld());

        float thresholdLowHealth = 5;
        float thresholdCriticalHealth = 2;

        Haptics.setLoopState(Haptics.Animations.fire, player.isOnFire());
        Haptics.setLoopState(Haptics.Animations.potion_positive, hasPotionPositive(player));
        Haptics.setLoopState(Haptics.Animations.potion_negative, hasPotionNegative(player));
        Haptics.setLoopState(Haptics.Animations.low_health,
            player.getHealth() < thresholdLowHealth && !(player.getHealth() < thresholdCriticalHealth));
        Haptics.setLoopState(Haptics.Animations.critical_health, player.getHealth() < thresholdCriticalHealth);
        Haptics.setLoopState(Haptics.Animations.rain, isInRain(player));

        for (HapticsModule module : this.modules) {
            if (module.enabled) {
                module.tick(player);
            }
        }

        if (player.getHealth() != this.lastHealth) {
            float damage = this.lastHealth - player.getHealth();
            if (damage > 0) {
                handleHit(null, damage);
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
                Haptics.getAnimation(Haptics.Animations.hunger).playSingle(false, null);
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

    public void handleExplode(Vec3 explosionPos) {
        double explosionDist = explosionPos.subtract(this.mc.player.position()).length();
        if (explosionDist < MAX_EXPLOSION_DIST) {
            double distFactor = 1.0 - (explosionDist / MAX_EXPLOSION_DIST);
            Haptics.getAnimation(Haptics.Animations.explosion).playSingle(true, null, distFactor);
        }
    }

    public void handleHit(DamageSource damageSrc, float damageAmount) {
        //TODO Always generic. Need custom server packet.
        // TODO this is meant to get the hit direction from the knockback, but knockback is usually sent after health updates so this doesn't really work
        Vector3f dmgVec = this.mc.player.getDeltaMovement().toVector3f()
            .mul(-1F)
            .mul(1, 0, 1) // only horizontal direction
            .normalize();
        if (Float.isNaN(dmgVec.x)) {
            // happens when standing still, just do forward damage then
            dmgVec.set(0, 0, 1);
        }
        dmgVec.rotateY(this.mc.player.yHeadRot * Mth.DEG_TO_RAD + Mth.PI);

        Haptics.getAnimation(Haptics.Animations.generic_hit).playSingle(true, dmgVec);
    }

    public void handleEat(ItemStack itemStack) {
        if (itemStack.get(DataComponents.FOOD) != null && itemStack.get(DataComponents.CONSUMABLE) != null) {
            if (itemStack.get(DataComponents.CONSUMABLE).onConsumeEffects().isEmpty()) {
                Haptics.getAnimation(Haptics.Animations.consume).playSingle(true, null);
            } else {
                Haptics.getAnimation(Haptics.Animations.consume_effect).playSingle(true, null);
            }
        }
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
