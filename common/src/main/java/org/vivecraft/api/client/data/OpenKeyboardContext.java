package org.vivecraft.api.client.data;

/**
 * Determines how strict the opening of the VR keyboard should be
 */
public enum OpenKeyboardContext {
    /**
     * Will open the keyboard no matter the situation.
     *
     * @since 1.3.0
     */
    FORCE,
    /**
     * Only opens the keyboard if the user has configured to open the keyboard when a text box becomes focused.
     *
     * @since 1.3.0
     */
    FOCUS,
    /**
     * Only opens the keyboard if the user has configured to open the keyboard when a text box or chat becomes focused.
     * Same as {@link #FOCUS}, but also works if the user only has chat configured to automatically open the keyboard.
     *
     * @since 1.3.0
     */
    FOCUS_CHAT
}
