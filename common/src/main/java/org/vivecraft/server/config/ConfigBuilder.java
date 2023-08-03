package org.vivecraft.server.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;

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

        DoubleValue value = new DoubleValue(config, path, defaultValue);
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

        IntValue value = new IntValue(config, path, defaultValue);
        configValues.add(value);
        return value;
    }


    public static class ConfigValue<T> {

        // the config, this setting is part of
        private final Config config;
        private final List<String> path;
        private final T defaultValue;
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
        public IntValue(Config config, List<String> path, int defaultValue) {
            super(config, path, defaultValue);
        }
    }

    public static class DoubleValue extends ConfigValue<Double>{
        public DoubleValue(Config config, List<String> path, double defaultValue) {
            super(config, path, defaultValue);
        }
    }
}
