package dev.shiftsad.core.config.adapters;

import com.typesafe.config.Config;

public final class DoubleAdapter implements ConfigAdapter<Double> {

    public static final DoubleAdapter INSTANCE = new DoubleAdapter();
    private DoubleAdapter() {}

    @Override
    public Double fromConfig(Config config, String path) {
        return config.getDouble(path);
    }
}