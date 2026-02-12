package org.vivecraft.common.network;

public enum NetworkVersion {
    // clients that used the old installer
    LEGACY,
    // allows the client to send the data in a single packet and switch back to NONVR
    NEW_NETWORKING,
    // adds full body tracker data
    FBT,
    // adds dual wielding packet and server logic
    DUAL_WIELDING,
    // adds the head as a valid active BodyPart, and adds a useForAim flag
    HEAD_AIM,
    // allows sending haptic events to the client
    HAPTIC_PACKET,
    // adds a packet, to inform the client what vr changes are on non default values
    SERVER_VR_CHANGES,
    // adds packets to send/receive damage directions
    DAMAGE_DIRECTION,
    // adds possibility to toggle settings after initial connection
    OPTION_TOGGLE,
    // adds packet to override the aim direction/position
    AIM_OVERRIDE,
    // adds a packet to indicate to the server that the client did a climbey jump
    CLIMBEY_JUMP;

    public static NetworkVersion fromProtocolVersion(int protocolVersion) {
        return values()[protocolVersion + 1];
    }

    /**
     * @return The protocol version that is sent between server/client. This is different to the ordinal,
     * because legacy is -1
     */
    public int protocolVersion() {
        return this.ordinal() - 1;
    }

    /**
     * checks if {@code other} supports the features of {@code this} NetworkVersion
     *
     * @param other other NetworkVersion to test
     * @return if the other Network version supports the features of this version
     */
    public boolean accepts(NetworkVersion other) {
        return this.ordinal() <= other.ordinal();
    }

    @Override
    public String toString() {
        return this.name() + ": " + this.protocolVersion();
    }
}
