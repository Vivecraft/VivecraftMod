package org.vivecraft.client_vr.provider.openvr_lwjgl.control;

import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HapticMusicPlayer {
    private static final Map<String, Music> map = new HashMap<>();

    private HapticMusicPlayer() {}

    public static Music newMusic(String name) {
        Music music = new Music(name);
        map.put(name, music);
        return music;
    }

    public static boolean hasMusic(String name) {
        return map.containsKey(name);
    }

    public static Music getMusic(String name) {
        return map.get(name);
    }

    public static void removeMusic(String name) {
        map.remove(name);
    }

    public static class Music {
        final String name;
        private final List<Object> data = new LinkedList<>();

        private Music(String name) {
            this.name = name;
        }

        public Music addNote(@Nullable ControllerType controller, float durationSeconds, float frequency, float amplitude) {
            this.data.add(new Note(controller, durationSeconds, frequency, amplitude));
            return this;
        }

        public Music addDelay(float durationSeconds) {
            this.data.add(new Delay(durationSeconds));
            return this;
        }

        public void clearData() {
            this.data.clear();
        }

        public void play() {
            float delayAccum = 0.0F;

            for (Object object : this.data) {
                if (object instanceof Note note) {
                    if (note.controller != null) {
                        MCOpenVR.get().triggerHapticPulse(note.controller, note.durationSeconds, note.frequency, note.amplitude, delayAccum);
                    } else {
                        MCOpenVR.get().triggerHapticPulse(ControllerType.RIGHT, note.durationSeconds, note.frequency, note.amplitude, delayAccum);
                        MCOpenVR.get().triggerHapticPulse(ControllerType.LEFT, note.durationSeconds, note.frequency, note.amplitude, delayAccum);
                    }
                } else if (object instanceof Delay delay) {
                    delayAccum += delay.durationSeconds;
                }
            }
        }

        private record Delay(float durationSeconds) {}

        private record Note(ControllerType controller, float durationSeconds, float frequency, float amplitude) {}
    }

    public class MusicBuilder {
        private Music music;
        private float tempo;
    }
}
