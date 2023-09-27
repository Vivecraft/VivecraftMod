package org.vivecraft.client_vr;

import net.minecraft.Util;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.ListIterator;

public class Vec3History {
    private final int _capacity = 450;
    private final LinkedList<entry> _data = new LinkedList<>();

    public void add(Vec3 in) {
        this._data.add(new entry(in));

        if (this._data.size() > this._capacity) {
            this._data.removeFirst();
        }
    }

    public void clear() {
        this._data.clear();
    }

    public Vec3 latest() {
        return (this._data.getLast()).data;
    }

    public double totalMovement(double seconds) {
        long i = Util.getMillis();
        ListIterator<entry> listiterator = this._data.listIterator(this._data.size());
        entry vec3history$entry = null;
        double d0 = 0.0D;
        int j = 0;

        while (listiterator.hasPrevious()) {
            entry vec3history$entry1 = listiterator.previous();
            ++j;

            if ((double) (i - vec3history$entry1.ts) > seconds * 1000.0D) {
                break;
            }

            if (vec3history$entry == null) {
                vec3history$entry = vec3history$entry1;
            } else {
                d0 += vec3history$entry.data.distanceTo(vec3history$entry1.data);
            }
        }

        return d0;
    }

    public Vec3 netMovement(double seconds) {
        long i = Util.getMillis();
        ListIterator<entry> listiterator = this._data.listIterator(this._data.size());
        entry vec3history$entry = null;
        entry vec3history$entry1 = null;
        double d0 = 0.0D;

        while (listiterator.hasPrevious()) {
            entry vec3history$entry2 = listiterator.previous();

            if ((double) (i - vec3history$entry2.ts) > seconds * 1000.0D) {
                break;
            }

            if (vec3history$entry == null) {
                vec3history$entry = vec3history$entry2;
            } else {
                vec3history$entry1 = vec3history$entry2;
            }
        }

        return vec3history$entry != null && vec3history$entry1 != null ? vec3history$entry.data.subtract(vec3history$entry1.data) : new Vec3(0.0D, 0.0D, 0.0D);
    }

    public double averageSpeed(double seconds) {
        long i = Util.getMillis();
        ListIterator<entry> listiterator = this._data.listIterator(this._data.size());
        double d0 = 0.0D;
        entry vec3history$entry = null;
        int j = 0;

        while (listiterator.hasPrevious()) {
            entry vec3history$entry1 = listiterator.previous();

            if ((double) (i - vec3history$entry1.ts) > seconds * 1000.0D) {
                break;
            }

            if (vec3history$entry == null) {
                vec3history$entry = vec3history$entry1;
            } else {
                ++j;
                double d1 = 0.001D * (double) (vec3history$entry.ts - vec3history$entry1.ts);
                double d2 = vec3history$entry.data.subtract(vec3history$entry1.data).length();
                d0 += d2 / d1;
            }
        }

        return j == 0 ? d0 : d0 / (double) j;
    }

    public Vec3 averagePosition(double seconds) {
        long i = Util.getMillis();
        ListIterator<entry> listiterator = this._data.listIterator(this._data.size());
        Vec3 vec3 = new Vec3(0.0D, 0.0D, 0.0D);
        int j;
        entry vec3history$entry;

        for (j = 0; listiterator.hasPrevious(); vec3 = vec3.add(vec3history$entry.data)) {
            vec3history$entry = listiterator.previous();

            if ((double) (i - vec3history$entry.ts) > seconds * 1000.0D) {
                break;
            }

            ++j;
        }

        return j == 0 ? vec3 : vec3.scale(1.0D / (double) j);
    }

    private class entry {
        public long ts = Util.getMillis();
        public Vec3 data;

        public entry(Vec3 in) {
            this.data = in;
        }
    }
}
