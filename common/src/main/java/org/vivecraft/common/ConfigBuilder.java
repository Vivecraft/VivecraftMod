package org.vivecraft.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.vivecraft.client.gui.settings.GuiListValueEditScreen;
import org.vivecraft.client.gui.widgets.QuadWidget;
import org.vivecraft.client.gui.widgets.SettingsList;
import org.vivecraft.client.gui.widgets.VectorWidget;

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

    private <T extends Enum<T>> void addDefaultValueComment(List<String> path, T defaultValue) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
                + new Formatter(Locale.US).format("default: %s", defaultValue.name()));
    }

    private void addDefaultValueComment(List<String> path, Quaternionf defaultValue) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
                +"x: %.2f, y %.2f, z: %.2f, w: %.2f".formatted(defaultValue.x, defaultValue.y, defaultValue.z, defaultValue.w));
    }

    private void addDefaultValueComment(List<String> path, Vector3d defaultValue) {
        String oldComment = config.getComment(path);
        config.setComment(path, (oldComment == null ? "" : oldComment + "\n ")
                +"x: %.2f, y: %.2f, z: %.2f".formatted(defaultValue.x, defaultValue.y, defaultValue.z));
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
    public <T> ListValue<T> defineList(List<T> defaultValue, Predicate<Object> validator) {
        List<String> path = stack.stream().toList();
        spec.defineList(path, defaultValue, validator);
        stack.removeLast();

        ListValue<T> value = new ListValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    /**
     * defines a setting with the current path, and pops the last path segment
     * @param defaultValue default value this setting should have
     * @param validValues Collection of values that are accepted
     * @return ConfigValue that accesses the setting at the path when calling this method
     */
    public <T> InListValue<T> defineInList(T defaultValue, Collection<? extends T> validValues) {
        List<String> path = stack.stream().toList();
        spec.defineInList(path, defaultValue, validValues);
        stack.removeLast();

        InListValue<T> value = new InListValue<>(config, path, defaultValue, validValues);
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

    public <T extends Enum<T>> EnumValue<T> define(T defaultValue) {
        List<String> path = stack.stream().toList();
        spec.defineEnum(path, defaultValue, EnumGetMethod.NAME);
        stack.removeLast();
        addDefaultValueComment(path, defaultValue);

        EnumValue<T> value = new EnumValue<>(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    public QuatValue define(Quaternionf defaultValue) {
        List<String> path = stack.stream().toList();
        stack.add("x");
        spec.define(stack.stream().toList(), defaultValue.x);
        stack.removeLast();
        stack.add("y");
        spec.define(stack.stream().toList(), defaultValue.y);
        stack.removeLast();
        stack.add("z");
        spec.define(stack.stream().toList(), defaultValue.z);
        stack.removeLast();
        stack.add("w");
        spec.define(stack.stream().toList(), defaultValue.w);
        stack.removeLast();
        stack.removeLast();

        addDefaultValueComment(path, defaultValue);
        QuatValue value = new QuatValue(config, path, defaultValue);
        configValues.add(value);
        return value;
    }

    public VectorValue define(Vector3d defaultValue) {
        List<String> path = stack.stream().toList();
        stack.add("x");
        spec.define(stack.stream().toList(), defaultValue.x);
        stack.removeLast();
        stack.add("y");
        spec.define(stack.stream().toList(), defaultValue.y);
        stack.removeLast();
        stack.add("z");
        spec.define(stack.stream().toList(), defaultValue.z);
        stack.removeLast();
        stack.removeLast();

        addDefaultValueComment(path, defaultValue);
        VectorValue value = new VectorValue(config, path, defaultValue);
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


    public static class ConfigValue<T> {

        // the config, this setting is part of
        protected final CommentedConfig config;
        protected final List<String> path;
        protected final T defaultValue;
        // cache te value to minimize config lookups
        protected T cachedValue = null;

        public ConfigValue(CommentedConfig config, List<String> path, T defaultValue) {
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

        public boolean isDefault() {
            return Objects.equals(get(),  defaultValue);
        }
        public String getComment() {
            String comment = config.getComment(path);
            return comment != null ? comment : "";
        }

        public String getPath() {
            return String.join(".", path);
        }

        public AbstractWidget getWidget(int width, int height) {
            return Button
                .builder(Component.literal("" + get()), button -> {})
                .bounds(0, 0, width, height)
                .tooltip(Tooltip.create(Component.literal(getComment())))
                .build();
        }
    }

    public static class BooleanValue extends ConfigValue<Boolean>{
        public BooleanValue(CommentedConfig config, List<String> path, boolean defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            return CycleButton
                .onOffBuilder(get())
                .displayOnlyValue()
                .withTooltip((bool) -> getComment() != null ? Tooltip.create(Component.literal(getComment())) : null)
                .create(0, 0,  width, height, Component.empty(), (button, bool) -> set(bool));
        }
    }

    public static class StringValue extends ConfigValue<String>{
        public StringValue(CommentedConfig config, List<String> path, String defaultValue) {
            super(config, path, defaultValue);
        }
        @Override
        public AbstractWidget getWidget(int width, int height) {
            EditBox box = new EditBox(Minecraft.getInstance().font, 0, 0, width - 1, height, Component.literal(get())) {
                @Override
                public boolean charTyped(char c, int i) {
                    boolean ret = super.charTyped(c, i);
                    set(this.getValue());
                    return ret;
                }

                @Override
                public boolean keyPressed(int i, int j, int k) {
                    boolean ret = super.keyPressed(i, j, k);
                    set(this.getValue());
                    return ret;
                }
            };
            box.setMaxLength(1000);
            box.setValue(get());
            box.setTooltip(Tooltip.create(Component.literal(getComment())));
            return box;
        }
    }

    public static class ListValue<T> extends ConfigValue<List<T>>{
        public ListValue(CommentedConfig config, List<String> path, List<T> defaultValue) {
            super(config, path, defaultValue);
        }
        @Override
        public AbstractWidget getWidget(int width, int height) {
            // TODO handle other types than String
            return Button
                .builder(
                    Component.translatable("vivecraft.options.editlist"),
                    button -> Minecraft.getInstance()
                        .setScreen(
                            new GuiListValueEditScreen(Component.literal(getPath().substring(getPath().lastIndexOf("."))), Minecraft.getInstance().screen, (ListValue<String>) this)
                        ))
                .size(width, height)
                .tooltip(Tooltip.create(Component.literal(getComment())))
                .build();
        }
    }

    public static class InListValue<T> extends ConfigValue<T>{
        private final Collection<? extends T> validValues;
        public InListValue(CommentedConfig config, List<String> path, T defaultValue, Collection<? extends T> validValues) {
            super(config, path, defaultValue);
            this.validValues = validValues;
        }

        public Collection<? extends T> getValidValues(){
            return validValues;
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            return CycleButton
                .builder((newValue) -> Component.literal("" + newValue))
                .withInitialValue(get())
                // toArray is needed here, because the button uses Objects, and the collection is of other types
                .withValues(getValidValues().toArray())
                .displayOnlyValue()
                .withTooltip((bool) -> getComment() != null ? Tooltip.create(Component.literal(getComment())) : null)
                .create(0, 0, width, height, Component.empty(), (button, newValue) -> set((T) newValue));
        }
    }

    public static abstract class NumberValue<E extends Number> extends ConfigValue<E>{

        private final E min;
        private final E max;

        public NumberValue(CommentedConfig config, List<String> path, E defaultValue, E min, E max) {
            super(config, path, defaultValue);
            this.min = min;
            this.max = max;
        }

        public E getMin(){
            return min;
        }
        public E getMax(){
            return max;
        }
        public double normalize() {
            return Mth.clamp((this.get().doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue()), 0.0D, 1.0D);
        }

        abstract public void fromNormalized(double value);

        @Override
        public AbstractWidget getWidget(int width, int height) {
             AbstractSliderButton widget = new AbstractSliderButton(0, 0,SettingsList.ResettableEntry.valueButtonWidth, 20, Component.literal("" + get()), normalize()) {
                @Override
                protected void updateMessage() {
                    setMessage(Component.literal("" + get()));
                }
                @Override
                protected void applyValue() {
                    fromNormalized(value);
                }
            };
            widget.setTooltip(Tooltip.create(Component.literal(getComment())));
            return widget;
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

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {

        public EnumValue(CommentedConfig config, List<String> path, T defaultValue) {
            super(config, path, defaultValue);
        }

        public void cycle() {
            T[] enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
            int newIndex = this.get().ordinal() + 1;
            if (enumConstants.length == newIndex) {
                newIndex = 0;
            }
            this.set(enumConstants[newIndex]);
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            return CycleButton
                    .builder((newValue) -> Component.literal("" + newValue))
                    .withInitialValue(get())
                    // toArray is needed here, because the button uses Objects, and the collection is of other types
                    .withValues(defaultValue.getDeclaringClass().getEnumConstants())
                    .displayOnlyValue()
                    .withTooltip((bool) -> getComment() != null ? Tooltip.create(Component.literal(getComment())) : null)
                    .create(0, 0, width, height, Component.empty(), (button, newValue) -> set((T) newValue));
        }
    }

    public static class QuatValue extends ConfigValue<Quaternionf> {

        public QuatValue(CommentedConfig config, List<String> path, Quaternionf defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public Quaternionf get() {
            if (cachedValue == null) {
                List<String> path2 = new ArrayList<>(path);
                path2.add("x");
                double x = config.get(path2);
                path2.set(path.size(), "y");
                double y = config.get(path2);
                path2.set(path.size(), "z");
                double z = config.get(path2);
                path2.set(path.size(), "w");
                double w = config.get(path2);
                cachedValue = new Quaternionf(x, y, z, w);
            }
            return new Quaternionf(cachedValue);
        }

        @Override
        public void set(Quaternionf newValue) {
            cachedValue = newValue;
            List<String> path2 = new ArrayList<>(path);
            path2.add("x");
            config.set(path2, newValue.x);
            path2.set(path.size(), "y");
            config.set(path2, newValue.y);
            path2.set(path.size(), "z");
            config.set(path2, newValue.z);
            path2.set(path.size(), "w");
            config.set(path2, newValue.w);
        }

        @Override
        public Quaternionf reset() {
            List<String> path2 = new ArrayList<>(path);
            path2.add("x");
            config.set(path2, defaultValue.x);
            path2.set(path.size(), "y");
            config.set(path2, defaultValue.y);
            path2.set(path.size(), "z");
            config.set(path2, defaultValue.z);
            path2.set(path.size(), "w");
            config.set(path2, defaultValue.w);
            cachedValue = defaultValue;
            return defaultValue;
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            return new QuadWidget(0,0, width, height, Component.literal(get().toString()), this);
        }
    }

    public class VectorValue extends ConfigValue<Vector3d> {

        public VectorValue(CommentedConfig config, List<String> path, Vector3d defaultValue) {
            super(config, path, defaultValue);
        }

        @Override
        public Vector3d get() {
            if (cachedValue == null) {
                List<String> path2 = new ArrayList<>(path);
                path2.add("x");
                double x = config.get(path2);
                path2.set(path.size(), "y");
                double y = config.get(path2);
                path2.set(path.size(), "z");
                double z = config.get(path2);
                cachedValue = new Vector3d(x, y, z);
            }
            return new Vector3d(cachedValue);
        }

        @Override
        public void set(Vector3d newValue) {
            cachedValue = newValue;
            List<String> path2 = new ArrayList<>(path);
            path2.add("x");
            config.set(path2, newValue.x);
            path2.set(path.size(), "y");
            config.set(path2, newValue.y);
            path2.set(path.size(), "z");
            config.set(path2, newValue.z);
        }

        @Override
        public Vector3d reset() {
            List<String> path2 = new ArrayList<>(path);
            path2.add("x");
            config.set(path2, defaultValue.x);
            path2.set(path.size(), "y");
            config.set(path2, defaultValue.y);
            path2.set(path.size(), "z");
            config.set(path2, defaultValue.z);
            cachedValue = defaultValue;
            return defaultValue;
        }

        @Override
        public AbstractWidget getWidget(int width, int height) {
            return new VectorWidget(0,0, width, height, Component.literal(get().toString()), this);
        }
    }
}
