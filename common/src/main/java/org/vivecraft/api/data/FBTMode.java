package org.vivecraft.api.data;

/**
 * The mode used for full-body tracking, denoting which body parts are being tracked.
 *
 * @since 1.3.0
 */
public enum FBTMode {
    /**
     * Only head and hands are available.
     */
    ARMS_ONLY,
    /**
     * head, hands, waist, and feet trackers are available.
     */
    ARMS_LEGS,
    /**
     * head, hands, waist, feet, elbow, and knee trackers are available.
     */
    WITH_JOINTS;

    /**
     * Whether the provided body part is available in this full-body tracking mode.
     *
     * @param bodyPart The body part to see if data is available for in this mode.
     * @return Whether the provided body part is available in this mode.
     * @since 1.3.0
     */
    public boolean bodyPartAvailable(VRBodyPart bodyPart) {
        return bodyPart.availableInMode(this);
    }
}
