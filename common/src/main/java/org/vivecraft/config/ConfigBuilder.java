package org.vivecraft.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import org.joml.Vector3d;

import java.util.*;
import java.util.function.Predicate;

public class ConfigBuilder {

    private final CommentedConfig config;
    private final ConfigSpec spec;
    private final Deque<String> stack = new ArrayDeque<>();
    private final List<ConfigValue> configValues = new ArrayList<>();

    public ConfigBuilder(CommentedConfig config, ConfigSpec spec){
        this.config = config;
        this.spec = spec;
    }

    /**
     * pushes the given subPath to the path
     * @param subPath new sub path
     * @return this builder, for chaining commands
     */
    public ConfigBuilder push(String subPath) {
        stack.add(subPath);
        return this;
    }

    /**
     * pops the last sub path
     * @return this builder, for chaining commands
     */
    public ConfigBuilder pop() {
        stack.removeLast();
        return this;
    }

    /**
     * add a comment to the config
     * @param comment Text for the comment
     * @return this builder, for chaining commands
     */
    public ConfigBuilder comment(String comment) {
        config.setComment(stack.stream().toList(), comment);
        return this;
    }

    private void addDefaultValueComment(List<String> path, String defaultValue) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
                + new Formatter(Locale.US).format("default: %.2s", defaultValue));
    }

    private void addDefaultValueComment(List<String> path, int defaultValue, int min, int max) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
            +"default: %d, min: %d, max: %d".formatted(defaultValue, min, max));
    }

    private void addDefaultValueComment(List<String> path, double defaultValue, double min, double max) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
            + new Formatter(Locale.US).format("default: %.2f, min: %.2f, max: %.2f", defaultValue, min, max));
    }

    private <T extends Enum<T>> void addDefaultValueComment(List<String> path, T defaultValue) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
                + new Formatter(Locale.US).format("default: %.2s", defaultValue.name()));
    }

    /**
     * corrects the attached config, with the built spec
     * @param listener listener to send correction to
     */
    public void correct(ConfigSpec.CorrectionListener listener) {
        spec.correct(config, listener);
    }

    public List<ConfigValue> getConfigValues() {
        return configValues;
    }

    // general Settings
    /**
     * defines a setting with the current path, and pops the last path segment
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ConfigValue<T> define(T defaultValue) {
        List<String> path = stack.stream().toList();
        spec.define(path, defaultValue);
        stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     * @param defaultValue default value this setting should have
     * @param min the minimum value, that  is valid for this setting
     * @param max the maximum value, that  is valid for this setting
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T extends Comparable<? super T>> ConfigValue<T> defineInRange(T defaultValue, T min, T max) {
        List<String> path = stack.stream().toList();
        spec.defineInRange(path, defaultValue, min, max);
        stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     * @param defaultValue default value this setting should have
     * @param validator Predicate, that signals, what values are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ConfigValue<List<? extends T>> defineList(List<? extends T> defaultValue, Predicate<Object> validator) {
        List<String> path = stack.stream().toList();
        spec.defineList(path, defaultValue, validator);
        stack.removeLast();

        ConfigValue<List<? extends T>> value = new ConfigValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     * @param defaultValue default value this setting should have
     * @param validValues Collection of values that are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ConfigValue<T> defineInList(T defaultValue, Collection<? extends T> validValues) {
        List<String> path = stack.stream().toList();
        spec.defineInList(path, defaultValue, validValues);
        stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     *  same as {@link #define define(T defaultValue)} but returns a {@link BooleanValue}
     */
    public BooleanValue define(boolean defaultValue) {
        List<String> path = stack.stream().toList();
        spec.define(path, defaultValue);
        stack.removeLast();

        BooleanValue value = new BooleanValue(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     *  same as {@link #define define(T defaultValue)} but returns a {@link StringValue}
     */
    public StringValue define(String defaultValue) {
        List<String> path = stack.stream().toList();
        spec.define(path, defaultValue);
        stack.removeLast();

        StringValue value = new StringValue(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     *  same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public DoubleValue defineInRange(double defaultValue, double min, double max) {
        List<String> path = stack.stream().toList();
        spec.defineInRange(path, defaultValue, min, max);
        stack.removeLast();
        addDefaultValueComment(path, defaultValue, min, max);

        DoubleValue value = new DoubleValue(config, path, defaultValue, min, max);
        configValues.add(value);
        return value;
    }

    /**
     *  same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public IntValue defineInRange(int defaultValue, int min, int max) {
        List<String> path = stack.stream().toList();
        spec.defineInRange(path, defaultValue, min, max);
        stack.removeLast();
        addDefaultValueComment(path, defaultValue, min, max);

        IntValue value = new IntValue(config, path, defaultValue, min, max);
        configValues.add(value);
        return value;
    }

    public <T extends Enum<T>> EnumValue<T> define(T defaultValue) {
        List<String> path = stack.stream().toList();
        spec.defineEnum(path, defaultValue, EnumGetMethod.NAME);
        stack.removeLast();
        addDefaultValueComment(path, defaultValue);

        EnumValue<T> value = new EnumValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    public VectorValue define(Vector3d defaultValue) {
        List<String> path = stack.stream().toList();
        spec.define(path, defaultValue.toString());
        stack.removeLast();
        addDefaultValueComment(path, defaultValue.toString());

        VectorValue value = new VectorValue(config, path, defaultValue);
        configValues.add(value);
        return value;
    }


    public static class ConfigValue<T> {

        // the config, this setting is part of
        protected final Config config;
        protected final List<String> path;
        protected final T defaultValue;
        // cache te value to minimize config lookups
        private T cachedValue = null;

        public ConfigValue(Config config, List<String> path, T defaultValue) {
            this.config = config;
            this.path = path;
            this.defaultValue = defaultValue;
        }

        public T get() {
            if (cachedValue == null) {
                cachedValue = config.get(path);
            }
            return cachedValue;
        }

        public void set(T newValue) {
            cachedValue = newValue;
            config.set(path, newValue);
        }

        public T reset() {
            config.set(path, defaultValue);
            cachedValue = defaultValue;
            return defaultValue;
        }

        public String getPath() {
            return String.join(".", path);
        }

        public MutableComponent getName() {
            return Component.translatable("vivecraft.options." + getPath());
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean>{
        public BooleanValue(Config config, List<String> path, boolean defaultValue) {
            super(config, path, defaultValue);
        }
    }

    public static class StringValue extends ConfigValue<String>{
        public StringValue(Config config, List<String> path, String defaultValue) {
            super(config, path, defaultValue);
        }
    }

    public static class IntValue extends ConfigValue<Integer>{
        public IntValue(Config config, List<String> path, int defaultValue, int min, int max) {
            super(config, path, defaultValue);
            this.min = min;
            this.max = max;
        }

        private final int min;
        private final int max;
        public double normalize() {
            return Mth.clamp((this.get() - this.min) / (float)(this.max - this.min), 0.0D, 1.0D);
        }

        public void fromNormalised(double value) {
            this.set(Mth.ceil(this.min + (this.max - this.min) * value));

        }
    }

    public static class DoubleValue extends ConfigValue<Double>{
        private final double min;
        private final double max;

        public DoubleValue(Config config, List<String> path, double defaultValue, double min, double max) {
            super(config, path, defaultValue);
            this.min = min;
            this.max = max;
        }

        public double normalize() {
            return Mth.clamp((this.get() - this.min) / (this.max - this.min), 0.0d, 1.0d);
        }

        public void fromNormalised(double value) {
            this.set(this.min + (this.max - this.min) * value);

        }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {

        public EnumValue(Config config, List<String> path, T defaultValue) {
            super(config, path, defaultValue);
        }

        public void cycle() {
            T[] enumConstants = (T[]) defaultValue.getClass().getEnumConstants();
            int newIndex = this.get().ordinal() + 1;
            if (enumConstants.length == newIndex) {
                newIndex = 0;
            }
            this.set(enumConstants[newIndex]);
        }
    }

    public static class VectorValue extends ConfigValue<Vector3d> {

        private String cachedValue;

        public VectorValue(Config config, List<String> path, Vector3d defaultValue) {
            super(config, path, defaultValue);
        }

        public Vector3d get() {
            if (cachedValue == null) {
                cachedValue = this.config.get(path);
            }
            return new Vector3d(Arrays.stream(cachedValue.split(";-")).mapToDouble(Double::valueOf).toArray());
        }

        public void set(Vector3d newValue) {
            cachedValue = newValue.toString();
            config.set(path, newValue);
        }

        public Vector3d reset() {
            config.set(path, defaultValue);
            cachedValue = defaultValue.toString();
            return defaultValue;
        }
    }
}
