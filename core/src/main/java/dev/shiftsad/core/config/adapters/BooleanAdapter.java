package dev.shiftsad.core.config.adapters;

import com.typesafe.config.Config;

public final class BooleanAdapter implements ConfigAdapter<Boolean> {

    public static final BooleanAdapter INSTANCE = new BooleanAdapter();
    private BooleanAdapter() {}

    @Override
    public Boolean fromConfig(Config config, String path) {
        return config.getBoolean(path);
    }
}