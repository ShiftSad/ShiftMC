package dev.shiftsad.core.config;

import dev.shiftsad.core.config.adapters.*;

import java.util.concurrent.ConcurrentHashMap;

public final class AdapterRegistry {
    private AdapterRegistry() {}

    private static final ConcurrentHashMap<Class<?>, ConfigAdapter<?>> adapters = new ConcurrentHashMap<>();

    static {
        registerAdapter(String.class, StringAdapter.INSTANCE);
        registerAdapter(Boolean.class, BooleanAdapter.INSTANCE);
        registerAdapter(Long.class, LongAdapter.INSTANCE);
        registerAdapter(Double.class, DoubleAdapter.INSTANCE);
        registerAdapter(Integer.class, IntAdapter.INSTANCE);
    }

    public static <T> void registerAdapter(Class<T> type, ConfigAdapter<T> adapter) {
        if (type == null || adapter == null) {
            throw new IllegalArgumentException("Type and adapter must not be null");
        }
        adapters.put(type, adapter);
    }

    @SuppressWarnings("unchecked")
    public static <T> ConfigAdapter<T> getAdapter(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        ConfigAdapter<T> adapter = (ConfigAdapter<T>) adapters.get(type);
        if (adapter == null) {
            throw new IllegalStateException("No adapter registered for type: " + type.getName());
        }
        return adapter;
    }
}
