package org.vivecraft.client_vr;

import com.mojang.math.Quaternion;
import net.minecraft.Util;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class QuaternionfHistory {
    private final int _capacity = 450;
    private final LinkedList<Entry> _data = new LinkedList<>();

    public void add(Quaternion in) {
        this._data.add(new Entry(in));

        if (this._data.size() > this._capacity) {
            this._data.removeFirst();
        }
    }

    public void clear() {
        this._data.clear();
    }

    public Quaternion latest() {
        return (this._data.getLast()).data;
    }

    public Quaternion averageRotation(double seconds) {
        long i = Util.getMillis();
        ListIterator<Entry> listiterator = this._data.listIterator(this._data.size());
        List<Quaternion> list = new LinkedList<>();

        for (int j = 0; listiterator.hasPrevious(); j++) {
            Entry entry = listiterator.previous();

            if ((double) (i - entry.ts) > seconds * 1000.0D) {
                break;
            } else {
                list.add(entry.data);
            }
        }

        if (list.size() > 0) {
            Quaternion quaternion = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
            return slerp(list.toArray(new Quaternion[]{}), quaternion);
        } else {
            return latest();
        }
    }

    private static class Entry {
        public long ts = Util.getMillis();
        public Quaternion data;

        public Entry(Quaternion in) {
            this.data = in;
        }
    }

    private static Quaternion slerp(Quaternion[] qs, Quaternion dest) {
        // mojang why are these not mapped
        dest.set(qs[0].i(), qs[0].j(), qs[0].k(), qs[0].r());
        float w = 1.0F;
        for (int i = 1; i < qs.length; i++) {
            float w0 = w;
            float w1 = 1.0F;
            float rw1 = w1 / (w0 + w1);
            w += w1;
            slerp(qs[i], rw1, dest);
        }
        return dest;
    }

    private static Quaternion slerp(Quaternion target, float alpha, Quaternion dest) {
        float cosom = dest.i() * target.i() + dest.j() * target.j() + dest.k() * target.k() + dest.r() * target.r();
        float absCosom = Math.abs(cosom);
        float scale0, scale1;
        if (1.0F - absCosom > 1E-6F) {
            float sinSqr = 1.0F - absCosom * absCosom;
            float sinom = 1.0F / (float) Math.sqrt(sinSqr);
            float omega = (float) Math.atan2(sinSqr * sinom, absCosom);
            scale0 = (float) Math.sin((1.0F - alpha) * omega) * sinom;
            scale1 = (float) Math.sin(alpha * omega) * sinom;
        } else {
            scale0 = 1.0F - alpha;
            scale1 = alpha;
        }
        scale1 = cosom >= 0.0F ? scale1 : -scale1;
        dest.set(
            scale0 * dest.i() + scale1 * target.i(),
            scale0 * dest.j() + scale1 * target.j(),
            scale0 * dest.k() + scale1 * target.k(),
            scale0 * dest.r() + scale1 * target.r()
        );
        return dest;
    }
}
