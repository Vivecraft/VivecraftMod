package org.vivecraft.common.serverconfig;

public class BooleanValue extends ConfigValue<Boolean>{

    public BooleanValue(String key, Boolean defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public Boolean get() {
        boolean value = Boolean.parseBoolean(ServerConfig.properties.getProperty(this.key, String.valueOf(this.defaultValue)));
        ServerConfig.properties.setProperty(this.key, String.valueOf(value));
        return value;
    }

}
