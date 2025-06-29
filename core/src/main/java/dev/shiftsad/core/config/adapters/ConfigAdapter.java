package dev.shiftsad.core.config.adapters;

import com.typesafe.config.Config;

public interface ConfigAdapter<T> {
    T fromConfig(Config config, String path);
}