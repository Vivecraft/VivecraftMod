package org.vivecraft.provider.openvr_jna.control;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.vivecraft.provider.ControllerType;
import org.vivecraft.provider.openvr_jna.MCOpenVR;

public class HapticMusicPlayer
{
    private static Map<String, HapticMusicPlayer.Music> map = new HashMap<>();

    private HapticMusicPlayer()
    {
    }

    public static HapticMusicPlayer.Music newMusic(String name)
    {
        HapticMusicPlayer.Music hapticmusicplayer$music = new HapticMusicPlayer.Music(name);
        map.put(name, hapticmusicplayer$music);
        return hapticmusicplayer$music;
    }

    public static boolean hasMusic(String name)
    {
        return map.containsKey(name);
    }

    public static HapticMusicPlayer.Music getMusic(String name)
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

        public HapticMusicPlayer.Music addNote(@Nullable ControllerType controller, float durationSeconds, float frequency, float amplitude)
        {
            this.data.add(new HapticMusicPlayer.Music.Note(controller, durationSeconds, frequency, amplitude));
            return this;
        }

        public HapticMusicPlayer.Music addDelay(float durationSeconds)
        {
            this.data.add(new HapticMusicPlayer.Music.Delay(durationSeconds));
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
                if (object instanceof HapticMusicPlayer.Music.Note)
                {
                    HapticMusicPlayer.Music.Note hapticmusicplayer$music$note = (HapticMusicPlayer.Music.Note)object;

                    if (hapticmusicplayer$music$note.controller != null)
                    {
                        MCOpenVR.get().triggerHapticPulse(hapticmusicplayer$music$note.controller, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                    }
                    else
                    {
                        MCOpenVR.get().triggerHapticPulse(ControllerType.RIGHT, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                        MCOpenVR.get().triggerHapticPulse(ControllerType.LEFT, hapticmusicplayer$music$note.durationSeconds, hapticmusicplayer$music$note.frequency, hapticmusicplayer$music$note.amplitude, f);
                    }
                }
                else if (object instanceof HapticMusicPlayer.Music.Delay)
                {
                    HapticMusicPlayer.Music.Delay hapticmusicplayer$music$delay = (HapticMusicPlayer.Music.Delay)object;
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
        private HapticMusicPlayer.Music music;
        private float tempo;
    }
}
