package org.vivecraft.client_vr;

import net.minecraft.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.Deque;

import static java.util.stream.Stream.generate;

@ParametersAreNonnullByDefault
public class Vec3History {
    private final int _capacity = 450;
    private final Deque<entry> _data = new ArrayDeque<>(_capacity);

    {
        _data.addAll(generate(entry::new).limit(_capacity).toList());
    }

    public Vector3dc add(double x, double y, double z) {
        this._data.addFirst(this._data.removeLast().set(x, y, z));
        return this._data.getFirst().data;
    }

    public Vector3dc add(Vec3 in) {
        return this.add(in.x(), in.y(), in.z());
    }

    public Vector3dc add(Vector3dc in) {
        this.add(in.x(), in.y(), in.z());
        return in;
    }

    public Vector3fc add(Vector3fc in) {
        this.add(in.x(), in.y(), in.z());
        return in;
    }

    public Vector3d latest(Vector3d dest) {
        return dest.set((this._data.getFirst()).data);
    }

    public Vector3f latest(Vector3f dest) {
        return dest.set((this._data.getFirst()).data);
    }

    public double totalMovement(double seconds) {
        long i = Util.getMillis();
        entry vec3history$entry = null;
        double d0 = 0.0D;

        for (entry vec3history$entry1 : this._data) {
            if ((double) (i - vec3history$entry1.ts) > seconds * 1000.0D) {
                break;
            }

            if (vec3history$entry == null) {
                vec3history$entry = vec3history$entry1;
            } else {
                d0 += vec3history$entry.data.distance(vec3history$entry1.data);
            }
        }

        return d0;
    }

    public Vector3d netMovement(double seconds, Vector3d dest) {
        long i = Util.getMillis();
        entry vec3history$entry = null;
        entry vec3history$entry1 = null;

        for (entry vec3history$entry2 : this._data) {
            if ((double) (i - vec3history$entry2.ts) > seconds * 1000.0D) {
                break;
            }

            if (vec3history$entry == null) {
                vec3history$entry = vec3history$entry2;
            } else {
                vec3history$entry1 = vec3history$entry2;
            }
        }

        return vec3history$entry != null && vec3history$entry1 != null ? vec3history$entry.data.sub(vec3history$entry1.data, dest) : dest.set(0);
    }

    public Vector3f netMovement(double seconds, Vector3f dest) {
        long i = Util.getMillis();
        entry vec3history$entry = null;
        entry vec3history$entry1 = null;

        for (entry vec3history$entry2 : this._data) {
            if ((double) (i - vec3history$entry2.ts) > seconds * 1000.0D) {
                break;
            }

            if (vec3history$entry == null) {
                vec3history$entry = vec3history$entry2;
            } else {
                vec3history$entry1 = vec3history$entry2;
            }
        }

        return vec3history$entry != null && vec3history$entry1 != null ? dest.set(vec3history$entry.data).sub((float) vec3history$entry1.data.x, (float) vec3history$entry1.data.y, (float) vec3history$entry1.data.z) : dest.set(0);
    }

    public double averageSpeed(double seconds) {
        long i = Util.getMillis();
        double d0 = 0.0D;
        entry vec3history$entry = null;
        int j = 0;

        for (entry vec3history$entry1 : this._data) {
            if ((double) (i - vec3history$entry1.ts) > seconds * 1000.0D) {
                break;
            }

            if (vec3history$entry == null) {
                vec3history$entry = vec3history$entry1;
            } else {
                ++j;
                double d1 = 0.001D * (vec3history$entry.ts - vec3history$entry1.ts);
                double d2 = vec3history$entry.data.distance(vec3history$entry1.data);
                d0 += d2 / d1;
            }
        }

        return j == 0 ? d0 : d0 / j;
    }

    public Vector3d averagePosition(double seconds, Vector3d dest) {
        long i = Util.getMillis();
        int j = 0;

        for (entry vec3history$entry : this._data) {
            if ((double) (i - vec3history$entry.ts) > seconds * 1000.0D) {
                break;
            }

            dest.add(vec3history$entry.data);
            ++j;
        }

        return j == 0 ? dest : dest.mul(1.0D / j);
    }

    public Vector3f averagePosition(double seconds, Vector3f dest) {
        long i = Util.getMillis();
        int j = 0;

        for (entry vec3history$entry : this._data) {
            if ((double) (i - vec3history$entry.ts) > seconds * 1000.0D) {
                break;
            }

            dest.add((float) vec3history$entry.data.x, (float) vec3history$entry.data.y, (float) vec3history$entry.data.z);
            ++j;
        }

        return j == 0 ? dest : dest.mul(1.0F / j);
    }

    private static class entry {
        public long ts = Util.getMillis();
        public Vector3d data = new Vector3d();

        public entry set(double x, double y, double z) {
            this.ts = Util.getMillis();
            this.data.set(x, y, z);
            return this;
        }
    }
}
