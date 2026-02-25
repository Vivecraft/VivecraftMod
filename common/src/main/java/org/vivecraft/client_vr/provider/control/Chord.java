package org.vivecraft.client_vr.provider.control;


import java.util.Map;

public record Chord(Map<String, ActionType> inputs, String output) {}
