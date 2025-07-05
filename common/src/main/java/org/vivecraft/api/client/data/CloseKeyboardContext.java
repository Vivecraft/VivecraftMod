package org.vivecraft.api.client.data;

/**
 * Determines how strict the closing of the VR keyboard should be
 */
public enum CloseKeyboardContext {
    /**
     * Will close the keyboard no matter the situation.
     */
    FORCE,
    /**
     * Will close the keyboard if the user has configured to close the keyboard when an action has been completed.
     */
    ACTION_COMPLETE
}
