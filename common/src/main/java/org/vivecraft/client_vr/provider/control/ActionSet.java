package org.vivecraft.client_vr.provider.control;

import java.util.List;

public record ActionSet(
    List<Source> sources,
    List<Haptic> haptics,
    List<Pose> poses,
    List<Chord> chords
) {}
