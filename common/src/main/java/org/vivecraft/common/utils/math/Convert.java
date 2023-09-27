package org.vivecraft.common.utils.math;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class Convert {
    public static Matrix matrix(float[] floatArray) {
        return new Matrix(floatArray);
    }

    public static Matrix matrix(float[][] float2Array) {
        float[] afloat = new float[float2Array.length * float2Array[0].length];

        for (int i = 0; i < float2Array.length; ++i) {
            System.arraycopy(float2Array[i], 0, afloat, i * float2Array[0].length, float2Array[0].length);
        }

        return matrix(afloat);
    }

    public static Matrix matrix(FloatBuffer floatBuffer) {
        float[] afloat = new float[floatBuffer.capacity()];
        ((Buffer) floatBuffer).position(0);
        floatBuffer.get(afloat);
        ((Buffer) floatBuffer).position(0);
        return matrix(afloat);
    }

    public static Matrix matrix(org.vivecraft.common.utils.lwjgl.Matrix4f matrix4f) {
        FloatBuffer floatbuffer = FloatBuffer.allocate(16);
        matrix4f.store(floatbuffer);
        return matrix(floatbuffer);
    }

    public static Matrix matrix(Quaternion quaternion) {
        return matrix(quaternion.getMatrix());
    }

    public static class Matrix {
        int dimension;
        float[] floatArray;
        double[] doubleArray;
        int[] intArray;
        boolean floatFilled = false;
        boolean doubleFilled = false;
        boolean intFilled = false;

        public Matrix(float[] floatArray) {
            this.dimension = (int) Math.sqrt(floatArray.length);

            if (this.dimension * this.dimension != floatArray.length) {
                throw new IllegalArgumentException("Input array has invalid length");
            } else {
                this.floatArray = floatArray;
                this.floatFilled = true;
            }
        }

        private void needFloats() {
            if (!this.floatFilled) {
                for (int i = 0; i < this.floatArray.length; ++i) {
                    if (this.doubleFilled) {
                        this.floatArray[i] = (float) this.doubleArray[i];
                    } else if (this.intFilled) {
                        this.floatArray[i] = (float) this.intArray[i];
                    }
                }

                this.floatFilled = true;
            }
        }

        private void needDoubles() {
            if (!this.doubleFilled) {
                for (int i = 0; i < this.doubleArray.length; ++i) {
                    if (this.floatFilled) {
                        this.doubleArray[i] = this.floatArray[i];
                    } else if (this.intFilled) {
                        this.doubleArray[i] = this.intArray[i];
                    }
                }

                this.doubleFilled = true;
            }
        }

        private void needInts() {
            if (!this.intFilled) {
                for (int i = 0; i < this.intArray.length; ++i) {
                    if (this.doubleFilled) {
                        this.intArray[i] = (int) this.doubleArray[i];
                    } else if (this.floatFilled) {
                        this.intArray[i] = (int) this.floatArray[i];
                    }
                }

                this.intFilled = true;
            }
        }

        public Matrix4f toOVRMatrix4f() {
            this.needFloats();

            if (this.dimension == 3) {
                return new Matrix4f(this.floatArray[0], this.floatArray[1], this.floatArray[2], this.floatArray[3], this.floatArray[4], this.floatArray[5], this.floatArray[6], this.floatArray[7], this.floatArray[8]);
            } else if (this.dimension == 4) {
                return new Matrix4f(this.floatArray[0], this.floatArray[1], this.floatArray[2], this.floatArray[3], this.floatArray[4], this.floatArray[5], this.floatArray[6], this.floatArray[7], this.floatArray[8], this.floatArray[9], this.floatArray[10], this.floatArray[11], this.floatArray[12], this.floatArray[13], this.floatArray[14], this.floatArray[15]);
            } else {
                throw new IllegalArgumentException("Wrong dimension! Can't convert Matrix" + this.dimension + " to Matrix4f");
            }
        }

        public org.joml.Matrix4f toMCMatrix4f() {
            this.needFloats();

            if (this.dimension == 4) {
                org.joml.Matrix4f matrix4f = new org.joml.Matrix4f();
                matrix4f.get(this.toFloatBuffer());
                return matrix4f;
            } else {
                throw new IllegalArgumentException("Wrong dimension! Can't convert Matrix" + this.dimension + " to Matrix4f");
            }
        }

        public FloatBuffer toFloatBuffer() {
            return FloatBuffer.wrap(this.floatArray);
        }
    }
}
