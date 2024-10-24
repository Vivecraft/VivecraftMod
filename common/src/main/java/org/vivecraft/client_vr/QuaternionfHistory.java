package org.vivecraft.client_vr;

import net.minecraft.Util;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class QuaternionfHistory {
    private static final int _capacity = 450;
    private final LinkedList<Entry> _data = new LinkedList<>();

    /**
     * adds a new entry with the given quaternion
     * @param in quaternion to add
     */
    public void add(Quaternionf in) {
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
     * @return the newest Quaternion
     */
    public Quaternionf latest() {
        return this._data.getLast().data;
    }

    /**
     * get the average rotation of the last {@code seconds}
     * @param seconds time to take the average over
     * @return average rotation
     */
    public Quaternionfc averageRotation(double seconds) {
        long now = Util.getMillis();
        ListIterator<Entry> iterator = this._data.listIterator(this._data.size());
        List<Quaternionf> list = new LinkedList<>();

        while (iterator.hasPrevious()) {
            Entry entry = iterator.previous();

            if (now - entry.ts > seconds * 1000.0D) {
                break;
            } else {
                list.add(entry.data);
            }
        }

        if (!list.isEmpty()) {
            float[] weights = new float[list.size()];
            Arrays.fill(weights, 1.0F);
            return Quaternionf.slerp(list.toArray(new Quaternionf[]{}), weights, new Quaternionf());
        } else {
            return latest();
        }
    }

    /**
     * Entry holding a quaternion and timestamp
     */
    private static class Entry {
        public long ts = Util.getMillis();
        public Quaternionf data;

        public Entry(Quaternionf in) {
            this.data = in;
        }
    }
}
