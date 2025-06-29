package dev.shiftsad.core.config.adapters;

import com.typesafe.config.Config;

public final class IntAdapter implements ConfigAdapter<Integer> {

    public static final IntAdapter INSTANCE = new IntAdapter();
    private IntAdapter() {}

    @Override
    public Integer fromConfig(Config config, String path) {
        return config.getInt(path);
    }
}