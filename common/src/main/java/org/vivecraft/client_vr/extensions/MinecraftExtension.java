package org.vivecraft.client_vr.extensions;

public interface MinecraftExtension {

    void vivecraft$notifyMirror(String text, boolean clear, int lengthMs);
    void vivecraft$drawProfiler();
}
