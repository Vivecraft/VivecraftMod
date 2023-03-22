package org.vivecraft.provider.openxr_jna.control;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import org.vivecraft.provider.openxr_jna.MCOpenXR;
import org.vivecraft.provider.ControllerType;

public class HapticMusicPlayer
{
    private static Map<String, Music> map = new HashMap<>();

    private HapticMusicPlayer()
    {
    }

    public static Music newMusic(String name)
    {
        Music hapticmusicplayer$music = new Music(name);
        map.put(name, hapticmusicplayer$music);
        return hapticmusicplayer$music;
    }

    public static boolean hasMusic(String name)
    {
        return map.containsKey(name);
    }

    public static Music getMusic(String name)
    {
        return map.get(name);
    }

    public static void removeMusic(String name)
    {
        map.remove(name);
    }

    public static class Music
    {
        final String name;
        private List<Object> data = new LinkedList<>();

        private Music(String name)
        {
            this.name = name;
        }

        public Music addNote(@Nullable ControllerType controller, float durationSeconds, float frequency, float amplitude)
        {
            this.data.add(new Note(controller, durationSeconds, frequency, amplitude));
            return this;
        }

        public Music addDelay(float durationSeconds)
        {
            this.data.add(new Delay(durationSeconds));
            return this;
        }

        public void clearData()
        {
            this.data.clear();
        }

        public void play()
        {
            float f = 0.0F;

            for (Object object : this.data)
            {
                if (object instanceof Note)
                {
                    Note hapticmusicplayer$music$note = (Note)object;

                    if (hapticmusicplayer$music$note.controller != null)
                    {
                        MCOpenXR.get().triggerHapticPulse(hapticmusicplayer$music$note.controller, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                    }
                    else
                    {
                        MCOpenXR.get().triggerHapticPulse(ControllerType.RIGHT, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                        MCOpenXR.get().triggerHapticPulse(ControllerType.LEFT, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                    }
                }
                else if (object instanceof Delay)
                {
                    Delay hapticmusicplayer$music$delay = (Delay)object;
                    f += hapticmusicplayer$music$delay.durationSeconds;
                }
            }
        }

        private class Delay
        {
            final float durationSeconds;

            private Delay(float durationSeconds)
            {
                this.durationSeconds = durationSeconds;
            }
        }

        private class Note
        {
            final ControllerType controller;
            final float durationSeconds;
            final float frequency;
            final float amplitude;

            private Note(ControllerType controller, float durationSeconds, float frequency, float amplitude)
            {
                this.controller = controller;
                this.durationSeconds = durationSeconds;
                this.frequency = frequency;
                this.amplitude = amplitude;
            }
        }
    }

    public class MusicBuilder
    {
        private Music music;
        private float tempo;
    }
}
