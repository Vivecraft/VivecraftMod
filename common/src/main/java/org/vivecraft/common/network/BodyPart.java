package org.vivecraft.common.network;

import net.minecraft.world.InteractionHand;

public enum BodyPart {
    MAIN_HAND,
    OFF_HAND,
    RIGHT_FOOT,
    LEFT_FOOT,
    WAIST,
    RIGHT_KNEE,
    LEFT_KNEE,
    RIGHT_ELBOW,
    LEFT_ELBOW,
    HEAD;

    /**
     * @return the opposite limb
     */
    public BodyPart opposite() {
        return switch (this) {
            case MAIN_HAND -> OFF_HAND;
            case OFF_HAND -> MAIN_HAND;
            case RIGHT_FOOT -> LEFT_FOOT;
            case LEFT_FOOT -> RIGHT_FOOT;
            case RIGHT_KNEE -> LEFT_KNEE;
            case LEFT_KNEE -> RIGHT_KNEE;
            case RIGHT_ELBOW -> LEFT_ELBOW;
            case LEFT_ELBOW -> RIGHT_ELBOW;
            default -> this;
        };
    }

    public static BodyPart fromInteractionHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? MAIN_HAND : OFF_HAND;
    }

    /**
     * @param fbtMode FBT mode to check for
     * @return if {@code this} limb is valid for the given FBT mode
     */
    public boolean isValid(FBTMode fbtMode) {
        return switch (this) {
            case MAIN_HAND, OFF_HAND, HEAD -> true;
            case RIGHT_FOOT, LEFT_FOOT, WAIST -> fbtMode != FBTMode.ARMS_ONLY;
            case RIGHT_KNEE, LEFT_KNEE, RIGHT_ELBOW, LEFT_ELBOW -> fbtMode == FBTMode.WITH_JOINTS;
        };
    }

    public boolean isFoot() {
        return this == RIGHT_FOOT || this == LEFT_FOOT;
    }

    public boolean isHand() {
        return this == MAIN_HAND || this == OFF_HAND;
    }
}
