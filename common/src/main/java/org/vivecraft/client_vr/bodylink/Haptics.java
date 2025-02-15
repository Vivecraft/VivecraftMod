package org.vivecraft.client_vr.bodylink;

import com.bhaptics.haptic.HapticPlayerImpl;
import com.bhaptics.haptic.models.DotPoint;
import com.bhaptics.haptic.models.PositionType;
import com.bhaptics.haptic.models.RotationOption;
import com.bhaptics.haptic.models.ScaleOption;
import org.joml.Vector3f;
import org.vivecraft.client.utils.FileUtils;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Haptics {
    private static final String APP_ID = "org.vivecraft";
    private static final String APP_NAME = "Vivecraft bHaptics Integration";

    private static final HashMap<String, HapticAnimation> REG_ANIMATIONS = new HashMap<>();

    private static HapticPlayerImpl B_HAPTICS_PLAYER = null;
    private static boolean CONNECTED;


    public enum Animations {

        explosion(1),
        fire(2, 2500),
        potion_positive(0),
        potion_negative(0),
        low_health(1, 1500),
        hunger(1, 2000),
        critical_health(1, 1000),
        generic_hit(3),
        zombie_hit(3),
        rain(1, 1500),
        consume(1),
        consume_effect(1);

        public int variants;
        public long durationMillis;

        Animations(int variants) {
            this.variants = variants;
        }

        Animations(int variants, long durationMillis) {
            this.variants = variants;
            this.durationMillis = durationMillis;
        }
    }

    public static class HapticMotor {
        private final PositionType positionType;
        private final int index;

        public HapticMotor(PositionType positionType, int index) {
            this.positionType = positionType;
            this.index = index;
        }

        public void dot(int intensity, int duration) {
            if (!CONNECTED) return;

            String key = "pos" + this.positionType.ordinal() + "_index" + this.index;
            //TODO Combine requests
            DotPoint d = new DotPoint(this.index, intensity);
            ArrayList<DotPoint> list = new ArrayList<>();
            list.add(d);
            B_HAPTICS_PLAYER.submitDot(key, this.positionType, list, duration);
        }

        public static void flushBuffered() {}
    }

    public static class HapticAnimation {
        private String baseId;
        private int variations;
        private long durationMillis = -1;
        private boolean looping;
        private boolean isLoop;
        private long startTimeStamp = -1;

        private Vector3f loopingVec;

        public String getRandomVariant() {
            return this.baseId + "_" + (int) (Math.random() * this.variations);
        }

        public void playSingle(boolean layered, Vector3f vec, double scale) {

            if (B_HAPTICS_PLAYER == null) return;
            if (!layered && isPlaying()) return;

            RotationOption rotationOption;
            if (vec != null) {
                rotationOption = new RotationOption(
                    Math.toDegrees(Math.atan2(vec.x, vec.z)) - 180,
                    Math.toDegrees(Math.asin(vec.y / vec.length())));
            } else {
                rotationOption = new RotationOption(0, 0);
            }

            ScaleOption scaleOption = new ScaleOption(scale, 1);

            String id = getRandomVariant();
            this.startTimeStamp = System.currentTimeMillis();

            B_HAPTICS_PLAYER.submitRegistered(id, this.baseId, rotationOption, scaleOption);
            //bHapticsPlayer.submitRegistered(id);
        }

        public void playSingle(boolean layered, Vector3f vec) {
            playSingle(layered, vec, 1.0);
        }

        public void setLooping(boolean looping) {
            this.isLoop = true;
            this.looping = looping;
        }

        public void setLoopingVec(Vector3f loopingVec) {
            this.loopingVec = loopingVec;
        }

        public boolean isPlaying() {
            if (this.startTimeStamp == -1 || this.durationMillis == -1) {return false;} else {
                return System.currentTimeMillis() < this.startTimeStamp + this.durationMillis;
            }
        }

        public void stopPlaying() {
            this.startTimeStamp = -1;
            B_HAPTICS_PLAYER.turnOff(this.baseId);
        }

        public void tick() {
            if (!this.isLoop) return;
            if (this.looping && !isPlaying()) {
                playSingle(false, this.loopingVec);
            } else if (!this.looping && isPlaying()) {
                stopPlaying();
            }
        }
    }

    public static void tick() {
        if (!isConnected()) {return;}
        for (HapticAnimation h : REG_ANIMATIONS.values()) {
            h.tick();
        }
    }

    static void loadAnimationFiles() {

        for (Animations animation : Animations.values()) {
            for (int i = 0; i < animation.variants; i++) {
                String fullId = animation.name() + "_" + i;
                String content = FileUtils.loadAssetToString("tact/" + fullId + ".tact", false);
                if (content == null) {
                    VRSettings.LOGGER.warn("Vivecraft: Missing .tact file {}.tact", fullId);
                } else {
                    B_HAPTICS_PLAYER.register(fullId, content);
                }
            }

            HapticAnimation hp = new HapticAnimation();
            hp.baseId = animation.name();
            hp.variations = animation.variants;
            hp.durationMillis = animation.durationMillis;
            REG_ANIMATIONS.put(animation.name(), hp);
        }
    }

    public static HapticAnimation getAnimation(Animations animation) {
        return REG_ANIMATIONS.get(animation.name());
    }

    public static boolean isConnected() {
        return B_HAPTICS_PLAYER != null && CONNECTED;
    }

    public static void test() {
        //getAnimation(Animations.explosion).playSingle(true,null);

        //bHapticsPlayer.submitRegistered("explosion_0");
        B_HAPTICS_PLAYER.submitDot("test", PositionType.VestBack, List.of(new DotPoint(3, 100)), 200);
    }

    public static void connect() {
        try {
            B_HAPTICS_PLAYER = new HapticPlayerImpl(APP_ID, APP_NAME, true, connected -> {
                Haptics.CONNECTED = connected;
                RiggedBody.getInstance().clearHapticPoints();
                if (connected) {
                    loadAnimationFiles();
                    // FIXME detect and add haptic devices

                    RiggedBody.getInstance().addHapticPoints(DeviceType.X40);
                }
            });

            VRSettings.LOGGER.info("Vivecraft: BHaptics library loaded");
        } catch (Throwable e) {
            VRSettings.LOGGER.error("Vivecraft: BHaptics library not found", e);
        }
    }

    public static boolean setLoopState(Animations animation, boolean loop) {
        HapticAnimation hapticAnimation = getAnimation(animation);
        if (hapticAnimation == null || hapticAnimation.looping == loop) return false;

        hapticAnimation.setLooping(loop);
        return true;
    }

    public static boolean isPlaying(String id) {
        if (!isConnected()) {
            return false;
        }
        return REG_ANIMATIONS.get(id).isPlaying();
    }

    public enum DeviceType {
        None, X40
    }
}
