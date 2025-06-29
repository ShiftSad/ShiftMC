package dev.shiftsad.core.config.adapters;

import com.typesafe.config.Config;

public final class StringAdapter implements ConfigAdapter<String> {

    public static final StringAdapter INSTANCE = new StringAdapter();
    private StringAdapter() {}

    @Override
    public String fromConfig(Config config, String path) {
        return config.getString(path);
    }
}