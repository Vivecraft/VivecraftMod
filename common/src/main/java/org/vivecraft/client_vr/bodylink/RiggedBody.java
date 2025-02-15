package org.vivecraft.client_vr.bodylink;

import com.bhaptics.haptic.models.PositionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.settings.AutoCalibration;
import org.vivecraft.common.utils.MathUtils;

import javax.annotation.Nullable;
import java.util.*;

public class RiggedBody {

    private static final float USER_THICCNESS = 0.3F;

    private final HashMap<PositionType, List<HapticPoint>> hapticPoints = new HashMap<>();
    private final HashMap<AnchorType, BodyAnchor> anchors = new HashMap<>();

    /**
     * Root bone with coordinates relative to players feet
     */
    private final Bone rootBone;
    private final Bone headBone;

    public RiggedBody() {
        // Create Skeleton

        float hmdHeight = AutoCalibration.getPlayerHeight();

        this.rootBone = new Bone(null, new Vector3f());
        this.headBone = new Bone(this.rootBone, new Vector3f(0, hmdHeight, 0));
        // TODO: Make complete Rig

        // Add tracked devices
        // TODO need recalibration event
        BodyAnchor hmdAnchor = new BodyAnchor(new Vector3f(0, hmdHeight, 0), new Quaternionf(), AnchorType.HMD);
        this.rootBone.addPoint(hmdAnchor);
        this.anchors.put(AnchorType.HMD, hmdAnchor);
        // TODO Complete Anchor setup

        // Add untracked devices

        // FIXME detect Suit or other devices and register bodypoints
    }

    public void updatePose(VRData vrData) {
        // TODO: update full rig using IK
        this.rootBone.currentRotRel = new Quaternionf().rotationY(vrData.getBodyYawRad());
        // TODO: what space should that be in?
        // it was world space, made it player local for now
        this.headBone.currentPosRel = MathUtils.subtractToVector3f(vrData.hmd.getPosition(),
            Minecraft.getInstance().player.position()).sub(this.headBone.basePosition);
        this.headBone.currentRotRel = vrData.hmd.getMatrix().getNormalizedRotation(new Quaternionf());

        this.rootBone.updatePoints();
    }


    public ArrayList<HapticPoint> getHapticPoints(com.bhaptics.haptic.models.PositionType... type) {
        ArrayList<HapticPoint> matches = new ArrayList<>();

        for (PositionType t : type) {
            if (t == PositionType.All) {
                matches.clear();
                for (Map.Entry<PositionType, List<HapticPoint>> entry : this.hapticPoints.entrySet()) {
                    matches.addAll(entry.getValue());
                }
                break;
            }

            List<HapticPoint> points = this.hapticPoints.get(t);
            if (points != null) {
                matches.addAll(points);
            }
        }

        return matches;
    }

    public void clearHapticPoints() {
        this.hapticPoints.clear();
    }

    public void addHapticPoints(Haptics.DeviceType deviceType) {
        if (deviceType == Haptics.DeviceType.X40) {
            float dimX = 0.23F;
            float dimY = 0.30F;
            float dimZ = USER_THICCNESS;

            float chestCenterOffset = 1.2F;

            for (int side = 0; side < 2; side++) {
                HapticPoint[] points = new HapticPoint[20];
                boolean front = side == 0;
                PositionType type = front ? PositionType.VestFront : PositionType.VestBack;

                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 5; y++) {
                        float posX = (x / 3F) * dimX - dimX * 0.5F;
                        float posY = (x / 4F) * dimY - dimY * 0.5F + chestCenterOffset;
                        float posZ = (front ? -1 : 1) * dimZ * 0.5F;

                        int index = x + y * 4;
                        Vector3f posVec = new Vector3f(posX, posY, posZ);
                        Quaternionf q = new Quaternionf();

                        int rotationalIndex = front ? x : (7 - x);
                        q = q.rotateY(Mth.DEG_TO_RAD * (-90F + (rotationalIndex + 0.5F) / 8F * 360F));

                        q = q.rotateX(Mth.DEG_TO_RAD * (90F - ((y + 0.5F) / 5F) * 180F));

                        HapticPoint point = new HapticPoint(posVec, q, new Haptics.HapticMotor(type, index));
                        point.setAnchor(this.rootBone, 1.0);

                        points[index] = point;
                    }
                }

                this.hapticPoints.put(type, new ArrayList<>(Arrays.asList(points)));
            }
        }
    }

    public class Bone {
        private final Bone parent;
        private final ArrayList<Bone> children = new ArrayList<>();
        private final ArrayList<BodyPoint> attachedPoints = new ArrayList<>();

        /**
         * hinge origin relative to parent bone position
         * in the default Pose
         */
        private final Vector3f basePosition;
        /**
         * Current Position relative to parent
         * Coordinate System: OpenGL
         */
        private Vector3f currentPosRel;
        private Quaternionf currentRotRel;

        public Bone(@Nullable Bone parent, Vector3f origin) {
            this.parent = parent;
            this.basePosition = origin;
            this.currentPosRel = origin;
            this.currentRotRel = new Quaternionf();

            if (parent != null) {
                parent.children.add(this);
            }
        }

        public void addPoint(BodyPoint point) {
            this.attachedPoints.add(point);
        }

        public Quaternionf getAbsRot() {
            Quaternionf totalRot = new Quaternionf();
            if (this.parent != null) {
                totalRot.set(this.parent.getAbsRot());
            }

            return totalRot.mul(this.currentRotRel);
        }

        public Vector3f getAbsPos() {
            Vector3f parentPos = new Vector3f();
            Quaternionf parentRot = new Quaternionf();
            if (this.parent != null) {
                parentPos.set(this.parent.getAbsPos());
                parentRot.set(this.parent.getAbsRot());
            }
            return parentPos.add(parentRot.transform(this.currentPosRel, new Vector3f()));
        }


        public void updatePoints() {
            for (BodyPoint b : this.attachedPoints) {
                getAbsRot().transform(b.basePos, b.currentPos).add(getAbsPos());
                getAbsRot().mul(b.baseRot, b.currentRot);
            }

            for (Bone child : this.children) {
                child.updatePoints();
            }
        }
    }


    public abstract class BodyPoint {

        /**
         * Absolute position in player space
         * Coordinate System: OpenGL
         */
        public final Vector3f currentPos;

        /**
         * Current absolute rotation
         */
        public final Quaternionf currentRot;


        /*** Position relative to parent bone position and rotation in Calibration Pose
         *  Coordinate System: OpenGL
         * */
        private final Vector3fc basePos;

        /*** Position relative to parent  rotation in Calibration Pose
         *  Coordinate System: OpenGL
         * */
        private final Quaternionfc baseRot;

        public BodyPoint(Vector3fc basePos, Quaternionfc baseRot) {
            this.basePos = basePos;
            this.baseRot = baseRot;
            this.currentPos = new Vector3f(basePos);
            this.currentRot = new Quaternionf(baseRot);
        }

        abstract boolean isTracked();

        Bone parent;

        /**
         * Returns this points forward vector
         * Coordinate System: OpenGL if mc == false
         * Minecraft if mc == true
         */
        public Vector3f getNormal(boolean mc) {
            Vector3f n = this.currentRot.transform(0, 0, -1, new Vector3f());
            if (mc) {
                return n.rotateY(Mth.PI);
            } else {
                return n;
            }
        }

        /**
         * Get absolute position in Minecraft Worldspace
         * Coordinate System: Minecraft
         */
        public Vec3 getPosWorld(LocalPlayer player) {
            Vec3 playerPos = player.position();
            Vector3f currentPosMC = this.currentPos.rotateY(Mth.PI, new Vector3f());
            return playerPos.add(currentPosMC.x, currentPosMC.y, currentPosMC.z);
        }

        public void setAnchor(Bone parent, double attachPos) {
            this.parent = parent;
            // FIXME: Attachment at endpoint of bone
            // Need variable length bones
        }
    }


    public class HapticPoint extends BodyPoint {
        public Haptics.HapticMotor motor;

        public HapticPoint(Vector3f relPos, Quaternionf relRot, Haptics.HapticMotor motor) {
            super(relPos, relRot);
            this.motor = motor;
        }

        @Override
        boolean isTracked() {
            return false;
        }
    }

    public class BodyAnchor extends BodyPoint {
        AnchorType type;

        public BodyAnchor(Vector3f relPos, Quaternionf relRot, AnchorType type) {
            super(relPos, relRot);
            this.type = type;
        }

        @Override
        boolean isTracked() {
            return true;
        }
    }

    public enum AnchorType {
        HMD, HAND_L, HAND_R, FOOT_L, FOOT_R, BELT, GENERATED, OTHER_SENSOR
    }


    public static RiggedBody instance;

    public static RiggedBody getInstance() {
        return instance;
    }

    static {
        instance = new RiggedBody();
    }
}
