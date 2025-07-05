package org.vivecraft.api.client.data;

/**
 * Determines how strict the opening of the VR keyboard should be
 */
public enum OpenKeyboardContext {
    /**
     * Will open the keyboard no matter the situation.
     */
    FORCE,
    /**
     * Only opens the keyboard if the user has configured to open the keyboard when a text box becomes focused.
     */
    FOCUS,
    /**
     * Only opens the keyboard if the user has configured to open the keyboard when a text box or chat becomes focused.
     * Same as {@link #FOCUS}, but also works if the user only has chat configured to automatically open the keyboard.
     */
    FOCUS_CHAT
}
