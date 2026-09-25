package org.vivecraft.client_vr.provider.control;

public enum ActionType {
    BOOLEAN("boolean"),
    DOUBLE_PRESS("boolean"),
    LONG_PRESS("boolean"),
    HOLD("boolean"),
    TOGGLE("boolean"),
    VEC1("vector1"),
    VEC2("vector2"),
    POSE("pose"),
    HAPTIC("vibration");

    final String steamName;

    ActionType(String steamName) {
        this.steamName = steamName;
    }

    public String getSteamName() {
        return steamName;
    }
}
