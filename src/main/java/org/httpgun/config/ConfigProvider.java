package org.httpgun.config;

public interface ConfigProvider {
    <T> T get(String path, Class<T> clazz);
}
