package org.vivecraft.client_vr.provider.openvr_lwjgl.control;

import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;
import org.vivecraft.common.utils.lwjgl.Vector2f;
import org.vivecraft.common.utils.math.Vector2;

public class TrackpadSwipeSampler {
    private static final int UP = 0;
    private static final int RIGHT = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    private final Vector2f[] buffer = new Vector2f[5];
    private int index;
    private long count;
    private final Vector2f accumulator = new Vector2f();
    private final int[] swiped = new int[4];
    public float threshold = 0.5F;

    public TrackpadSwipeSampler() {
        for (int i = 0; i < this.buffer.length; ++i) {
            this.buffer[i] = new Vector2f();
        }
    }

    public void update(ControllerType hand, Vector2 position) {
        MCOpenVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyTrackpadTouch).setCurrentHand(hand);

        if (MCOpenVR.get().getInputAction(VivecraftVRMod.INSTANCE.keyTrackpadTouch).isButtonPressed()) {
            this.buffer[this.index].set(position.getX(), position.getY());

            if (++this.index >= this.buffer.length) {
                this.index = 0;
            }

            ++this.count;
        } else {
            for (Vector2f vector2f : this.buffer) {
                vector2f.set(0.0F, 0.0F);
            }

            this.count = 0L;
        }

        if (this.count >= (long) this.buffer.length) {
            int i = (this.index + 1) % this.buffer.length;
            this.accumulator.x += this.buffer[i].x - this.buffer[this.index].x;
            this.accumulator.y += this.buffer[i].y - this.buffer[this.index].y;

            if (this.accumulator.x >= this.threshold) {
                this.accumulator.x -= this.threshold;
                int i1 = this.swiped[1]++;
            }

            if (this.accumulator.x <= -this.threshold) {
                this.accumulator.x += this.threshold;
                int j = this.swiped[3]++;
            }

            if (this.accumulator.y >= this.threshold) {
                this.accumulator.y -= this.threshold;
                int k = this.swiped[0]++;
            }

            if (this.accumulator.y <= -this.threshold) {
                this.accumulator.y += this.threshold;
                int l = this.swiped[2]++;
            }
        } else {
            this.accumulator.set(0.0F, 0.0F);
        }
    }

    public boolean isSwipedLeft() {
        return this.isSwiped(3);
    }

    public boolean isSwipedRight() {
        return this.isSwiped(1);
    }

    public boolean isSwipedUp() {
        return this.isSwiped(0);
    }

    public boolean isSwipedDown() {
        return this.isSwiped(2);
    }

    private boolean isSwiped(int direction) {
        if (this.swiped[direction] > 0) {
            int i = this.swiped[direction]--;
            return true;
        } else {
            return false;
        }
    }
}
