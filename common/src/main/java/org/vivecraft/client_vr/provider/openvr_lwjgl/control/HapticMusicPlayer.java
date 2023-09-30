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

    private HapticMusicPlayer() {
    }

    public static Music newMusic(String name) {
        Music hapticmusicplayer$music = new Music(name);
        map.put(name, hapticmusicplayer$music);
        return hapticmusicplayer$music;
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
            float f = 0.0F;

            for (Object object : this.data) {
                if (object instanceof Note hapticmusicplayer$music$note) {

                    if (hapticmusicplayer$music$note.controller != null) {
                        MCOpenVR.get().triggerHapticPulse(hapticmusicplayer$music$note.controller, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                    } else {
                        MCOpenVR.get().triggerHapticPulse(ControllerType.RIGHT, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                        MCOpenVR.get().triggerHapticPulse(ControllerType.LEFT, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                    }
                } else if (object instanceof Delay hapticmusicplayer$music$delay) {
                    f += hapticmusicplayer$music$delay.durationSeconds;
                }
            }
        }

        private class Delay {
            final float durationSeconds;

            private Delay(float durationSeconds) {
                this.durationSeconds = durationSeconds;
            }
        }

        private class Note {
            final ControllerType controller;
            final float durationSeconds;
            final float frequency;
            final float amplitude;

            private Note(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
                this.controller = controller;
                this.durationSeconds = durationSeconds;
                this.frequency = frequency;
                this.amplitude = amplitude;
            }
        }
    }

    public class MusicBuilder {
        private Music music;
        private float tempo;
    }
}
