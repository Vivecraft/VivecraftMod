package org.vivecraft.api.data;

/**
 * Holds the Vivecraft version a player joined the server with.
 *
 * @since 1.3.13
 */
public interface ViveVersion {

    /**
     * Returns the major version component.
     *
     * @return The major version component of this version.
     */
    int getMajor();

    /**
     * Returns the minor version component.
     *
     * @return The minor version component of this version.
     */
    int getMinor();

    /**
     * Returns the patch version component.
     *
     * @return The patch version component of this version.
     */
    int getPatch();

    /**
     * Retruns the Release type of the version, either RELEASE, BETA or ALPHA.
     *
     * @return The ReleaseType of this version.
     */
    ReleaseType getReleaseType();

    enum ReleaseType {
        RELEASE,
        BETA,
        ALPHA
    }
}
