package org.vivecraft.client_vr;

import net.minecraft.Util;
import org.joml.Quaternionf;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class QuaternionfHistory {
    private final int _capacity = 450;
    private final LinkedList<Entry> _data = new LinkedList<>();

    public void add(Quaternionf in) {
        this._data.add(new Entry(in));

        if (this._data.size() > this._capacity) {
            this._data.removeFirst();
        }
    }

    public void clear() {
        this._data.clear();
    }

    public Quaternionf latest() {
        return (this._data.getLast()).data;
    }

    public Quaternionf averageRotation(double seconds) {
        long i = Util.getMillis();
        ListIterator<Entry> listiterator = this._data.listIterator(this._data.size());
        List<Quaternionf> list = new LinkedList<>();

        for (int j = 0; listiterator.hasPrevious(); j++) {
            Entry entry = listiterator.previous();

            if ((double) (i - entry.ts) > seconds * 1000.0D) {
                break;
            } else {
                list.add(entry.data);
            }
        }

        if (list.size() > 0) {
            Quaternionf quaternionf = new Quaternionf();
            float[] weights = new float[list.size()];
            Arrays.fill(weights, 1.0F);
            Quaternionf.slerp(list.toArray(new Quaternionf[]{}), weights, quaternionf);

            return quaternionf;
        } else {
            return latest();
        }
    }

    private static class Entry {
        public long ts = Util.getMillis();
        public Quaternionf data;

        public Entry(Quaternionf in) {
            this.data = in;
        }
    }
}
