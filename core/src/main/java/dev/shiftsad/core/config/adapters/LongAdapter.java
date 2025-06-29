package dev.shiftsad.core.config.adapters;

import com.typesafe.config.Config;

public final class LongAdapter implements ConfigAdapter<Long> {

    public static final LongAdapter INSTANCE = new LongAdapter();
    private LongAdapter() {}

    @Override
    public Long fromConfig(Config config, String path) {
        return config.getLong(path);
    }
}