package org.vivecraft.api.data;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents the pose history of the VR player. See {@link VRPose} for individual Pose data.
 * In other words, it allows getting movement information of the VR player.
 * <br>
 * Vivecraft will store data for players going up to 200 ticks into the past. Attempting to retrieve history before
 * this far back will throw an {@link IllegalArgumentException}.
 *
 * @since 1.3.0
 */
public interface VRPoseHistory {

    /**
     * @return The amount of ticks worth of history currently held. The number returned by this method will never be
     * higher than 200, the maximum amount of past ticks of data Vivecraft holds, however it can be lower than 200.
     * @since 1.3.0
     */
    int ticksOfHistory();

    /**
     * Gets a raw list of {@link VRPose} instances, with index 0 representing the current tick's pose, 1 representing
     * last tick's pose, etc.
     *
     * @return The aforementioned list of {@link VRPose} instances.
     * @since 1.3.0
     */
    List<VRPose> getAllHistoricalData();

    /**
     * Gets the pose history {@code ticksBack} ticks back, or {@code null} if such data isn't available.
     *
     * @param ticksBack Ticks back to retrieve data.
     * @return A {@link VRPose} instance from index ticks ago, or {@code null} if that data isn't available.
     * @throws IllegalArgumentException Thrown when maxTicksBack is outside the range [0,200].
     * @since 1.3.0
     */
    VRPose getHistoricalData(int ticksBack) throws IllegalArgumentException;

    /**
     * Gets the net movement between the most recent pose in this instance and the oldest position that can be
     * retrieved, going no farther back than {@code maxTicksBack}.
     *
     * @param bodyPart     The body part to get the net movement for.
     * @param maxTicksBack The maximum amount of ticks back to compare the most recent data with.
     * @return The aforementioned net movement. Note that this will return zero change on all axes if only zero ticks
     * can be looked back. Will be {@code null} if the body part requested isn't available.
     * @throws IllegalArgumentException Thrown when maxTicksBack is outside the range [0,200] or an invalid
     *                                  {@code bodyPart} is supplied.
     * @since 1.3.0
     */
    @Nullable
    Vec3 netMovement(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException;

    /**
     * Gets the average velocity in blocks/tick between the most recent pose in this instance and the oldest position
     * that can be retrieved, going no farther back than {@code maxTicksBack}.
     *
     * @param bodyPart     The body part to get the average velocity for.
     * @param maxTicksBack The maximum amount of ticks back to calculate velocity with.
     * @return The aforementioned average velocity on each axis. Note that this will return zero velocity on all axes
     * if only zero ticks can be looked back. Will be {@code null} if the body part requested isn't available.
     * @throws IllegalArgumentException Thrown when maxTicksBack is outside the range [0,200] or an invalid
     *                                  {@code bodyPart} is supplied.
     * @since 1.3.0
     */
    @Nullable
    Vec3 averageVelocity(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException;

    /**
     * Gets the average speed in blocks/tick between the most recent pose in this instance and the oldest position
     * that can be retrieved, going no farther back than {@code maxTicksBack}.
     *
     * @param bodyPart     The body part to get the average speed for.
     * @param maxTicksBack The maximum amount of ticks back to calculate speed with.
     * @return The aforementioned average speed on each axis. Note that this will return zero speed if only zero ticks
     * can be looked back. Will be 0 if the body part requested isn't available.
     * @throws IllegalArgumentException Thrown when maxTicksBack is outside the range [0,200] or an invalid
     *                                  {@code bodyPart} is supplied.
     * @since 1.3.0
     */
    double averageSpeed(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException;

    /**
     * Gets the average position between the most recent pose in this instance and the oldest position that can be
     * retrieved, going no farther back than maxTicksBack.
     *
     * @param bodyPart     The body part to get the average position for.
     * @param maxTicksBack The maximum amount of ticks back to calculate velocity with.
     * @return The aforementioned average position. Note that this will return the current position if only zero ticks
     * can be looked back. Will be {@code null} if the body part requested isn't available.
     * @throws IllegalArgumentException Thrown when maxTicksBack is outside the range [0,200] or an invalid
     *                                  {@code bodyPart} is supplied.
     * @since 1.3.0
     */
    @Nullable
    Vec3 averagePosition(VRBodyPart bodyPart, int maxTicksBack) throws IllegalArgumentException;
}
