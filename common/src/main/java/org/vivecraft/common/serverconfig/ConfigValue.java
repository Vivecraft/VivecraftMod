package org.vivecraft.common.serverconfig;

public abstract class ConfigValue<T> {

    protected final String key;
    protected final T defaultValue;

    public ConfigValue(String key, T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    abstract public T get();

    public void set(T value) {
        ServerConfig.properties.setProperty(this.key, String.valueOf(value));
    }
}
