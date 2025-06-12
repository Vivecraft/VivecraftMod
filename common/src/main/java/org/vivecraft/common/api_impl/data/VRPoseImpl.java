package org.vivecraft.common.api_impl.data;

import org.vivecraft.api.data.FBTMode;
import org.vivecraft.api.data.VRBodyPart;
import org.vivecraft.api.data.VRBodyPartData;
import org.vivecraft.api.data.VRPose;

import javax.annotation.Nullable;

public record VRPoseImpl(VRBodyPartData hmd, VRBodyPartData c0, VRBodyPartData c1,
                         VRBodyPartData rightFoot, VRBodyPartData leftFoot,
                         VRBodyPartData waist,
                         VRBodyPartData rightKnee, VRBodyPartData leftKnee,
                         VRBodyPartData rightElbow, VRBodyPartData leftElbow,
                         boolean isSeated, boolean isLeftHanded, FBTMode fbtMode) implements VRPose
{

    @Override
    @Nullable
    public VRBodyPartData getBodyPartData(VRBodyPart vrBodyPart) {
        if (vrBodyPart == null) {
            throw new IllegalArgumentException("Cannot get a null body part's data!");
        }
        return switch (vrBodyPart) {
            case HEAD -> this.hmd;
            case MAIN_HAND -> this.c0;
            case OFF_HAND -> this.c1;
            case RIGHT_FOOT -> this.rightFoot;
            case LEFT_FOOT -> this.leftFoot;
            case WAIST -> this.waist;
            case RIGHT_KNEE -> this.rightKnee;
            case LEFT_KNEE -> this.leftKnee;
            case RIGHT_ELBOW -> this.rightElbow;
            case LEFT_ELBOW -> this.leftElbow;
        };
    }

    @Override
    public FBTMode getFBTMode() {
        return this.fbtMode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VRPose:\nHMD: ").append(this.hmd)
            .append("\nmain hand: ").append(this.c0)
            .append("\noffhand: ").append(this.c1);
        if (this.fbtMode != FBTMode.ARMS_ONLY) {
            sb.append("\nright foot: ").append(this.rightFoot)
                .append("\nleft foot: ").append(this.leftFoot)
                .append("\nwaist: ").append(this.waist);
        }
        if (this.fbtMode == FBTMode.WITH_JOINTS) {
            sb.append("\nright knee: ").append(this.rightKnee)
                .append("\nleft knee: ").append(this.leftKnee)
                .append("\nright elbow: ").append(this.rightElbow)
                .append("\nleft elbow: ").append(this.leftElbow);
        }
        sb.append("\nseated: ").append(this.isSeated)
            .append(", leftHanded: ").append(this.isLeftHanded)
            .append(", fbtMode: ").append(this.fbtMode);
        return sb.toString();
    }
}
