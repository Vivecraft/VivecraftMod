package org.vivecraft.client_vr.extensions;

public interface MinecraftExtension {

    /**
     * shows notification text on the desktop window
     * @param text text to show
     * @param clear if the screen should be cleared to black
     * @param lengthMs how many milliseconds the text should be shown
     */
    void vivecraft$notifyMirror(String text, boolean clear, int lengthMs);

    /**
     * draws the profiler pie on the screen
     */
    void vivecraft$drawProfiler();
}
