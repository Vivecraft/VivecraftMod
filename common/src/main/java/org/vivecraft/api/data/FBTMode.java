package org.vivecraft.api.data;

/**
 * The mode used for full-body tracking, denoting which body parts are being tracked.
 *
 * @since 1.3.0
 */
public enum FBTMode {
    /**
     * Only head and hands are available.
     *
     * @since 1.3.0
     */
    ARMS_ONLY,
    /**
     * Head, hands, waist, and feet trackers are available.
     *
     * @since 1.3.0
     */
    ARMS_LEGS,
    /**
     * Head, hands, waist, feet, elbow, and knee trackers are available.
     *
     * @since 1.3.0
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
