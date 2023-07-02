package org.vivecraft.common.serverconfig;

public class DoubleValue extends ConfigValue<Double>{

    public DoubleValue(String key, Double defaultValue) {
        super(key, defaultValue);
    }

    @Override
    public Double get() {
        Double value = Double.parseDouble(ServerConfig.properties.getProperty(this.key, String.valueOf(this.defaultValue)));
        ServerConfig.properties.setProperty(this.key, String.valueOf(value));
        return value;
    }

}
