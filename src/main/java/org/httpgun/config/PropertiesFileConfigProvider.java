package org.httpgun.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class PropertiesFileConfigProvider implements ConfigProvider {
    public final Config config = ConfigFactory.load();

    @Override
    public <T> T get(String path, Class<T> clazz) {
        if (clazz == Long.class) {
            return clazz.cast(config.getLong(path));
        } else if (clazz == Integer.class) {
            return clazz.cast(config.getInt(path));
        } else if (clazz == Double.class) {
            return clazz.cast(config.getDouble(path));
        } else if (clazz == Boolean.class) {
            return clazz.cast(config.getBoolean(path));
        } else if (clazz == String.class) {
            return clazz.cast(config.getString(path));
        } else {
            return clazz.cast(config.getAnyRef(path));
        }
    }
}
