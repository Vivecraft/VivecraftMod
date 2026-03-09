package org.vivecraft.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRPose;
import org.vivecraft.common.api_impl.VRAPIImpl;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.NetworkVersion;
import org.vivecraft.common.network.VrPlayerState;
import org.vivecraft.common.utils.MathUtils;

import javax.annotation.Nullable;

public class ServerVivePlayer {
    // player movement state
    @Nullable
    private VrPlayerState vrPlayerState;
    private VRPose vrPlayerStateAsPose;
    // how much the player is drawing the roomscale bow
    public float draw;
    public float worldScale = 1.0F;
    public float heightScale = 1.0F;
    public VRBodyPart activeBodyPart = VRBodyPart.MAIN_HAND;
    // custom aim override, instead of using the active bodypart
    public Vector3fc aimDirOverride = null;
    public Vec3 aimPosOverride = null;
    // ticks when to reset the aim override
    public int aimReset;
    // we need to keep a copy of this, in case the item breaks during dualwielding
    public ItemStack activeItemOverride = ItemStack.EMPTY;
    // when a player mines a block too fast, the destroy is delayed, need to keep track of the bodypart that actually destroyed it
    public VRBodyPart delayedDestroyBodyPart = null;
    public boolean useBodyPartForAim = false;
    public boolean crawling;
    // if the player has VR active
    private boolean isVR = false;
    // offset set during aimFix to keep the original data positions
    public Vec3 offset = Vec3.ZERO;
    // player this data belongs to
    public ServerPlayer player;
    // network protocol this player is communicating with
    public NetworkVersion networkVersion = NetworkVersion.fromProtocolVersion(
        CommonNetworkHelper.MAX_SUPPORTED_NETWORK_PROTOCOL);
    // if the client requested damage direction data
    public boolean wantsDamageDirection = false;

    public ServerVivePlayer(ServerPlayer player) {
        this.player = player;
    }

    /**
     * Gets the active bodypart to use to get the the used item, accounts for roomscale bow
     *
     * @return the active bodypart
     */
    public VRBodyPart getActiveItemBodyPart() {
        // the bow sets the bodypart to the bodypart the arrow was drawn with, but the bow is still the active item
        return this.isDrawing() ? VRBodyPart.MAIN_HAND : this.activeBodyPart;
    }

    /**
     * @return if the player is using the roomscale bow
     */
    public boolean isDrawing() {
        return !this.isSeated() && this.draw > 0.0F;
    }

    /**
     * transforms the local {@code direction} vector on BodyPart {@code bodyPart} into world space
     *
     * @param bodyPart  BodyPart to get the custom direction on, if not available, will use the MAIN_HAND
     * @param direction local direction to transform
     * @return direction in world space
     */
    public Vec3 getBodyPartVectorCustom(VRBodyPart bodyPart, Vector3fc direction) {
        if (this.vrPlayerState != null) {
            if (this.isSeated() || !bodyPart.availableInMode(this.vrPlayerState.fbtMode())) {
                bodyPart = VRBodyPart.MAIN_HAND;
            }

            return new Vec3(
                this.vrPlayerState.getBodyPartPose(bodyPart).orientation().transform(direction, new Vector3f()));
        } else {
            return this.player.getLookAngle();
        }
    }

    /**
     * @param bodyPart BodyPart to get the direction from, if not available, will use the MAIN_HAND
     * @return forward direction of the given BodyPart
     */
    public Vec3 getBodyPartDir(VRBodyPart bodyPart) {
        return this.getBodyPartVectorCustom(bodyPart, MathUtils.BACK);
    }

    /**
     * @param ignoreUseForAim ignores the useBodyPartForAim state when set, and always uses the active BodyPart for the aim
     * @return the direction the player is aiming, accounts for the roomscale bow
     */
    public Vec3 getAimDir(boolean ignoreUseForAim) {
        if (this.aimDirOverride != null) {
            return new Vec3(this.aimDirOverride);
        } else if (this.isDrawing()) {
            return this.getBodyPartPos(this.activeBodyPart.opposite())
                .subtract(this.getBodyPartPos(this.activeBodyPart)).normalize();
        } else if (ignoreUseForAim || this.useBodyPartForAim) {
            return this.getBodyPartDir(this.activeBodyPart);
        } else {
            return this.getBodyPartDir(VRBodyPart.MAIN_HAND);
        }
    }

    /**
     * @param ignoreUseForAim ignores the useBodyPartForAim state when set, and always uses the active BodyPart for the aim
     * @return the position from which the player is aiming
     */
    public Vec3 getAimPos(boolean ignoreUseForAim) {
        if (this.aimPosOverride != null) {
            return this.aimPosOverride;
        } else if (ignoreUseForAim || this.useBodyPartForAim) {
            return this.getBodyPartPos(this.activeBodyPart);
        } else {
            return this.getBodyPartPos(VRBodyPart.MAIN_HAND);
        }
    }

    /**
     * @return looking direction of the head
     */
    public Vec3 getHMDDir() {
        if (this.vrPlayerState != null) {
            return new Vec3(this.vrPlayerState.hmd().orientation().transform(MathUtils.BACK, new Vector3f()));
        } else {
            return this.player.getLookAngle();
        }
    }

    /**
     * @return position of the head, in world space
     */
    public Vec3 getHMDPos() {
        if (this.vrPlayerState != null) {
            Vector3fc hmdPos = this.vrPlayerState.hmd().position();
            return this.player.position().add(
                this.offset.x + hmdPos.x(),
                this.offset.y + hmdPos.y(),
                this.offset.z + hmdPos.z());
        } else {
            return this.player.position().add(0.0D, 1.62D, 0.0D);
        }
    }

    /**
     * @param bodyPart     BodyPart to get the position for, if not available, will use the MAIN_HAND
     * @param realPosition if true disables the seated override
     * @return BodyPart position in world space
     */
    public Vec3 getBodyPartPos(VRBodyPart bodyPart, boolean realPosition) {
        if (this.vrPlayerState != null) {
            if (!bodyPart.availableInMode(this.vrPlayerState.fbtMode())) {
                bodyPart = VRBodyPart.MAIN_HAND;
            }

            // in seated the realPosition is at the head,
            // so reconstruct the seated position when wanting the visual position
            if (this.isSeated() && bodyPart.isHand() && !realPosition) {
                Vec3 dir = this.getHMDDir();
                dir = dir.yRot(Mth.DEG_TO_RAD * (bodyPart == VRBodyPart.MAIN_HAND ? -35.0F : 35.0F));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                return this.getHMDPos().add(
                    dir.x * 0.3F * this.worldScale,
                    -0.4F * this.worldScale,
                    dir.z * 0.3F * this.worldScale);
            }

            Vector3fc conPos = this.vrPlayerState.getBodyPartPose(bodyPart).position();

            return this.player.position().add(
                this.offset.x + conPos.x(),
                this.offset.y + conPos.y(),
                this.offset.z + conPos.z());
        } else {
            return this.player.position().add(0.0D, 1.62D, 0.0D);
        }
    }

    /**
     * @param bodyPart BodyPart to get the position for, if not available, will use the MAIN_HAND
     * @return BodyPart position in world space
     */
    public Vec3 getBodyPartPos(VRBodyPart bodyPart) {
        return getBodyPartPos(bodyPart, false);
    }

    /**
     * @return if the player has VR active
     */
    public boolean isVR() {
        return this.isVR;
    }

    /**
     * set VR state of the player
     */
    public void setVR(boolean vr) {
        this.isVR = vr;
    }

    /**
     * @return if the player is using seated VR
     */
    public boolean isSeated() {
        return this.vrPlayerState != null && this.vrPlayerState.seated();
    }

    /**
     * @return if the player is using left-handed mode
     */
    public boolean isLeftHanded() {
        return this.vrPlayerState != null && this.vrPlayerState.leftHanded();
    }

    @Nullable
    public VrPlayerState vrPlayerState() {
        return this.vrPlayerState;
    }

    public void setVrPlayerState(VrPlayerState vrPlayerState) {
        this.vrPlayerState = vrPlayerState;
        this.vrPlayerStateAsPose = null;
        VRAPIImpl.INSTANCE.addPoseToHistory(this.player.getUUID(), vrPlayerState.asVRPose(this.player.position()),
            this.player.position(), false);
    }

    public VRPose asVRPose() {
        if (this.vrPlayerState == null) {
            return null;
        }
        if (this.vrPlayerStateAsPose == null) {
            this.vrPlayerStateAsPose = this.vrPlayerState.asVRPose(this.player.position());
        }
        return this.vrPlayerStateAsPose;
    }

    public void tick() {
        if (this.aimReset > 0 && --this.aimReset == 0) {
            this.aimDirOverride = null;
            this.aimPosOverride = null;
        }
    }
}
