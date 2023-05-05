package org.vivecraft.common.serverconfig;

public class IntValue extends ConfigValue<Integer>{

    public IntValue(String key, Integer defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public Integer get() {
        Integer value = Integer.parseInt(ServerConfig.properties.getProperty(this.key, String.valueOf(this.defaultValue)));
        ServerConfig.properties.setProperty(this.key, String.valueOf(value));
        return value;
    }
}
