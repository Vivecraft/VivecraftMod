package org.vivecraft.mod_compat_vr.sodium.extensions;

public interface ModelCuboidExtension {

    /**
     * @return vertex overrides for this cuboid
     */
    float[][] vivecraft$getOverrides();

    /**
     * sets a vertex override, copies the vertex data from {@code overrideFaceIndex} to {@code sourceFaceIndex}
     *
     * @param overrideFaceIndex face index to copy from
     * @param sourceFaceIndex   face index to copy to
     * @param source            source texture data, first array contains 6 U coords, second array the 3 V coords
     */
    void vivecraft$addOverrides(int overrideFaceIndex, int sourceFaceIndex, float[][] source);
}
