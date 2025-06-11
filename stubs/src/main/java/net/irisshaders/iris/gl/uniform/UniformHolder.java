package net.irisshaders.iris.gl.uniform;

import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public interface UniformHolder {

    UniformHolder uniform1i(UniformUpdateFrequency updateFrequency, String name, IntSupplier value);

    UniformHolder uniform1b(UniformUpdateFrequency updateFrequency, String name, BooleanSupplier value);

    UniformHolder uniform3f(UniformUpdateFrequency updateFrequency, String name, Supplier<Vector3f> value);

    UniformHolder uniformMatrix(UniformUpdateFrequency updateFrequency, String name, Supplier<Matrix4fc> value);
}
