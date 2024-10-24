package org.vivecraft.client_vr;

import net.minecraft.Util;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.ListIterator;

public class Vec3History {
    private static final int _capacity = 450;
    private final LinkedList<Entry> _data = new LinkedList<>();

    /**
     * adds a new entry with the given Vec3
     * @param in Vec3 to add
     */
    public void add(Vec3 in) {
        this._data.add(new Entry(in));

        if (this._data.size() > _capacity) {
            this._data.removeFirst();
        }
    }

    /**
     * clears all data
     */
    public void clear() {
        this._data.clear();
    }

    /**
     * @return the newest Vec3
     */
    public Vec3 latest() {
        return (this._data.getLast()).data;
    }

    /**
     * Get the total integrated device translation for the specified time period.
     * @param seconds time period
     * @return distance in meters
     */
    public double totalMovement(double seconds) {
        long now = Util.getMillis();
        ListIterator<Entry> iterator = this._data.listIterator(this._data.size());
        Entry last = null;
        double distance = 0.0D;

        // TODO: this does the wrong thing I think
        while (iterator.hasPrevious()) {
            Entry current = iterator.previous();

            if (now - current.ts > seconds * 1000.0D) {
                break;
            } else {
                if (last == null) {
                    last = current;
                } else {
                    distance += last.data.distanceTo(current.data);
                }
            }
        }

        return distance;
    }

    /**
     * Get the vector representing the difference in position from now to {@code seconds} ago.
     * @param seconds time period
     * @return vector with the position difference
     */
    public Vec3 netMovement(double seconds) {
        long now = Util.getMillis();
        ListIterator<Entry> iterator = this._data.listIterator(this._data.size());
        Entry last = null;
        Entry first = null;

        while (iterator.hasPrevious()) {
            Entry current = iterator.previous();

            if (now - current.ts > seconds * 1000.0D) {
                break;
            }

            if (last == null) {
                last = current;
            } else {
                first = current;
            }
        }

        return last != null && first != null ? last.data.subtract(first.data) : new Vec3(0.0D, 0.0D, 0.0D);
    }

    /**
     * Get the average speed of the device over the specified time period.
     * @param seconds time period
     * @return speed in m/s.
     */
    public double averageSpeed(double seconds) {
        long now = Util.getMillis();
        ListIterator<Entry> iterator = this._data.listIterator(this._data.size());
        double speedTotal = 0.0D;
        Entry last = null;
        int count = 0;

        while (iterator.hasPrevious()) {
            Entry current = iterator.previous();

            if (now - current.ts > seconds * 1000.0D) {
                break;
            }

            if (last == null) {
                last = current;
            } else {
                count++;
                double timeDelta = 0.001D * (last.ts - current.ts);
                double positionDelta = last.data.subtract(current.data).length();
                speedTotal += positionDelta / timeDelta;
            }
        }

        return count == 0 ? speedTotal : speedTotal / (double) count;
    }

    /**
     * Get the average position for the last {@code seconds}.
     * @param seconds time period
     * @return average position
     */
    public Vec3 averagePosition(double seconds) {
        long now = Util.getMillis();
        ListIterator<Entry> iterator = this._data.listIterator(this._data.size());
        Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
        int count = 0;

        while (iterator.hasPrevious()) {
            Entry current = iterator.previous();

            if (now - current.ts > seconds * 1000.0D) {
                break;
            }

            vec3 = vec3.add(current.data);
            count++;
        }

        return count == 0 ? vec3 : vec3.scale(1.0D / (double) count);
    }

    /**
     * Entry holding a position and timestamp
     */
    private static class Entry {
        public long ts = Util.getMillis();
        public Vec3 data;

        public Entry(Vec3 in) {
            this.data = in;
        }
    }
}
