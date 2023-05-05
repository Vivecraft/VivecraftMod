package org.vivecraft.common.serverconfig;

public class StringValue extends ConfigValue<String>{

    public StringValue(String key, String defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public String get() {
        String value = ServerConfig.properties.getProperty(this.key, String.valueOf(this.defaultValue));
        ServerConfig.properties.setProperty(this.key, String.valueOf(value));
        return value;
    }
}
