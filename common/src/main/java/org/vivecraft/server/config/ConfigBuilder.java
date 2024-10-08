package org.vivecraft.server.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.util.Mth;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigBuilder {

    private final CommentedConfig config;
    private final ConfigSpec spec;
    private final Deque<String> stack = new ArrayDeque<>();
    private final List<ConfigValue> configValues = new ArrayList<>();

    public ConfigBuilder(CommentedConfig config, ConfigSpec spec) {
        this.config = config;
        this.spec = spec;
    }

    /**
     * pushes the given subPath to the path
     *
     * @param subPath new sub path
     * @return this builder, for chaining commands
     */
    public ConfigBuilder push(String subPath) {
        this.stack.add(subPath);
        return this;
    }

    /**
     * pops the last sub path
     *
     * @return this builder, for chaining commands
     */
    public ConfigBuilder pop() {
        this.stack.removeLast();
        return this;
    }

    /**
     * add a comment to the config
     *
     * @param comment Text for the comment
     * @return this builder, for chaining commands
     */
    public ConfigBuilder comment(String comment) {
        this.config.setComment(this.stack.stream().toList(), comment);
        return this;
    }

    private void addDefaultValueComment(List<String> path, int defaultValue, int min, int max) {
        String oldComment = this.config.getComment(path);
        this.config.setComment(path, (oldComment == null ? "" : oldComment + "\n ") +
            "default: %d, min: %d, max: %d".formatted(defaultValue, min, max));
    }

    private void addDefaultValueComment(List<String> path, double defaultValue, double min, double max) {
        String oldComment = this.config.getComment(path);
        this.config.setComment(path, (oldComment == null ? "" : oldComment + "\n ") +
            new Formatter(Locale.US).format("default: %.2f, min: %.2f, max: %.2f", defaultValue, min, max));
    }

    /**
     * corrects the attached config, with the built spec
     *
     * @param listener listener to send correction to
     */
    public void correct(ConfigSpec.CorrectionListener listener) {
        this.spec.correct(this.config, listener);
    }

    public List<ConfigValue> getConfigValues() {
        return this.configValues;
    }

    // general Settings

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ConfigValue<T> define(T defaultValue) {
        List<String> path = this.stack.stream().toList();
        this.spec.define(path, defaultValue);
        this.stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(this.config, path, defaultValue);
        this.configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param min          the minimum value, that  is valid for this setting
     * @param max          the maximum value, that  is valid for this setting
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T extends Comparable<? super T>> ConfigValue<T> defineInRange(T defaultValue, T min, T max) {
        List<String> path = this.stack.stream().toList();
        this.spec.defineInRange(path, defaultValue, min, max);
        this.stack.removeLast();

        ConfigValue<T> value = new ConfigValue<>(this.config, path, defaultValue);
        this.configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param validator    Predicate, that signals, what values are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> ListValue<T> defineList(List<T> defaultValue, Predicate<Object> validator) {
        List<String> path = this.stack.stream().toList();
        this.spec.defineList(path, defaultValue, validator);
        this.stack.removeLast();

        ListValue<T> value = new ListValue<>(this.config, path, defaultValue);
        this.configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @param validValues  Collection of values that are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> InListValue<T> defineInList(T defaultValue, Collection<? extends T> validValues) {
        List<String> path = this.stack.stream().toList();
        this.spec.defineInList(path, defaultValue, validValues);
        this.stack.removeLast();

        InListValue<T> value = new InListValue<>(this.config, path, defaultValue, validValues);
        this.configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     *
     * @param defaultValue default value this setting should have
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T extends Enum<T>> EnumValue<T> defineEnum(T defaultValue, Class<T> enumClass) {
        List<String> path = this.stack.stream().toList();

        EnumValue<T> value = new EnumValue<>(this.config, path, defaultValue, enumClass);
        this.spec.defineInList(path, defaultValue, value.getValidValues());
        this.stack.removeLast();

        this.configValues.add(value);
        return value;
    }

    /**
     * same as {@link #define define(T defaultValue)} but returns a {@link BooleanValue}
     */
    public BooleanValue define(boolean defaultValue) {
        List<String> path = this.stack.stream().toList();
        this.spec.define(path, defaultValue);
        this.stack.removeLast();

        BooleanValue value = new BooleanValue(this.config, path, defaultValue);
        this.configValues.add(value);
        return value;
    }

    /**
     * same as {@link #define define(T defaultValue)} but returns a {@link StringValue}
     */
    public StringValue define(String defaultValue) {
        List<String> path = this.stack.stream().toList();
        this.spec.define(path, defaultValue);
        this.stack.removeLast();

        StringValue value = new StringValue(this.config, path, defaultValue);
        this.configValues.add(value);
        return value;
    }

    /**
     * same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public DoubleValue defineInRange(double defaultValue, double min, double max) {
        List<String> path = this.stack.stream().toList();
        this.spec.defineInRange(path, defaultValue, min, max);
        this.stack.removeLast();
        addDefaultValueComment(path, defaultValue, min, max);

        DoubleValue value = new DoubleValue(this.config, path, defaultValue, min, max);
        this.configValues.add(value);
        return value;
    }

    /**
     * same as {@link #defineInRange defineInRange(T defaultValue, T min, T max)} but returns a {@link DoubleValue}
     */
    public IntValue defineInRange(int defaultValue, int min, int max) {
        List<String> path = this.stack.stream().toList();
        this.spec.defineInRange(path, defaultValue, min, max);
        this.stack.removeLast();
        addDefaultValueComment(path, defaultValue, min, max);

        IntValue value = new IntValue(this.config, path, defaultValue, min, max);
        this.configValues.add(value);
        return value;
    }


    public static class ConfigValue<T> {

        // the config, this setting is part of
        private final CommentedConfig config;
        private final List<String> path;
        private final T defaultValue;
        // cache te value to minimize config lookups
        private T cachedValue = null;

        public ConfigValue(CommentedConfig config, List<String> path, T defaultValue) {
            this.config = config;
            this.path = path;
            this.defaultValue = defaultValue;
        }

        public T get() {
            if (this.cachedValue == null) {
                this.cachedValue = this.config.get(this.path);
            }
            return this.cachedValue;
        }

        public void set(T newValue) {
            this.cachedValue = newValue;
            this.config.set(this.path, newValue);
        }

        public T reset() {
            this.config.set(this.path, this.defaultValue);
            this.cachedValue = this.defaultValue;
            return this.defaultValue;
        }

        protected T getDefaultValue() {
            return this.defaultValue;
        }

        public boolean isDefault() {
            return Objects.equals(get(), getDefaultValue());
        }

        public String getComment() {
            String comment = this.config.getComment(this.path);
            return comment != null ? comment : "";
        }

        public String getPath() {
            return String.join(".", this.path);
        }

        public Supplier<AbstractWidget> getWidget(int width, int height) {
            return WidgetBuilder.getBaseWidget(this, width, height);
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean> {
        public BooleanValue(CommentedConfig config, List<String> path, boolean defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public Supplier<AbstractWidget> getWidget(int width, int height) {
            return WidgetBuilder.getOnOffWidget(this, width, height);
        }
    }

    public static class StringValue extends ConfigValue<String> {
        public StringValue(CommentedConfig config, List<String> path, String defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public Supplier<AbstractWidget> getWidget(int width, int height) {
            return WidgetBuilder.getEditBoxWidget(this, width, height);
        }
    }

    public static class ListValue<T> extends ConfigValue<List<T>> {
        public ListValue(CommentedConfig config, List<String> path, List<T> defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public Supplier<AbstractWidget> getWidget(int width, int height) {
            return WidgetBuilder.getEditListWidget(this, width, height);
        }
    }

    public static class InListValue<T> extends ConfigValue<T> {
        private final Collection<? extends T> validValues;

        public InListValue(CommentedConfig config, List<String> path, T defaultValue, Collection<? extends T> validValues) {
            super(config, path, defaultValue);
            this.validValues = validValues;
        }

        public Collection<? extends T> getValidValues() {
            return this.validValues;
        }

        @Override
        public Supplier<AbstractWidget> getWidget(int width, int height) {
            return WidgetBuilder.getCycleWidget(this, getValidValues(), width, height);
        }
    }

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
        private final Class<T> enumClass;
        public EnumValue(CommentedConfig config, List<String> path, T defaultValue, Class<T> enumClass) {
            super(config, path, defaultValue);
            this.enumClass = enumClass;
        }

        public T getEnumValue(Object value) {
            try {
                return EnumGetMethod.NAME_IGNORECASE.get(value, this.enumClass);
            } catch (Exception e) {
                return null;
            }
        }

        public Collection<? extends T> getValidValues() {
            return EnumSet.allOf(this.enumClass);
        }

        @Override
        public Supplier<AbstractWidget> getWidget(int width, int height) {
            return WidgetBuilder.getCycleWidget(this, getValidValues(), width, height);
        }
    }

    public static abstract class NumberValue<E extends Number> extends ConfigValue<E> {

        private final E min;
        private final E max;

        public NumberValue(CommentedConfig config, List<String> path, E defaultValue, E min, E max) {
            super(config, path, defaultValue);
            this.min = min;
            this.max = max;
        }

        public E getMin() {
            return this.min;
        }

        public E getMax() {
            return this.max;
        }

        public double normalize() {
            return Mth.clamp((this.get().doubleValue() - this.min.doubleValue()) / (this.max.doubleValue() - this.min.doubleValue()), 0.0D, 1.0D);
        }

        abstract public void fromNormalized(double value);

        @Override
        public Supplier<AbstractWidget> getWidget(int width, int height) {
            return WidgetBuilder.getSliderWidget(this, width, height);
        }
    }

    public static class IntValue extends NumberValue<Integer> {

        public IntValue(CommentedConfig config, List<String> path, int defaultValue, int min, int max) {
            super(config, path, defaultValue, min, max);
        }

        @Override
        public void fromNormalized(double value) {
            double newValue = this.getMin() + (this.getMax() - this.getMin()) * value;
            this.set(Mth.floor(newValue + 0.5));
        }
    }

    public static class DoubleValue extends NumberValue<Double> {

        public DoubleValue(CommentedConfig config, List<String> path, double defaultValue, double min, double max) {
            super(config, path, defaultValue, min, max);
        }

        @Override
        public void fromNormalized(double value) {
            double newValue = this.getMin() + (this.getMax() - this.getMin()) * value;
            this.set(Math.round(newValue * 100.0) / 100.0);
        }
    }
}
