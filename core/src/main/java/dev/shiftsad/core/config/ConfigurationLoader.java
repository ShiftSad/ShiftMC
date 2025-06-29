package dev.shiftsad.core.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import dev.shiftsad.core.config.adapters.ConfigAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationLoader {

    private final Config config;

    public ConfigurationLoader(@NotNull String file, @Nullable Path target) throws IOException {
        String content = getFileContent(file, target);
        this.config = ConfigFactory.parseString(content).resolve();
    }

    /**
     * Get the content of the configuration file.
     * If the file is not available in the filesystem, it will attempt to create it from the resources.
     *
     * @param fileName the name of the configuration file
     * @throws IOException if an I/O error occurs while reading or creating the file
     * @return the content of the configuration file as a String
     */
    private String getFileContent(@NotNull String fileName, @Nullable Path target) throws IOException {
        Path path = target != null ? target.resolve(fileName) : Paths.get(fileName);

        if (!Files.exists(path)) {
            URL defaultConfig = ConfigurationLoader.class.getResource("/" + fileName);

            if (defaultConfig != null) {
                if (path.getParent() != null) {
                    Files.createDirectories(path.getParent());
                }
                try (var inputStream = defaultConfig.openStream()) {
                    Files.copy(inputStream, path);
                }
            } else {
                throw new IOException("Default configuration file not found in resources: /" + fileName);
            }
        }

        return Files.readString(path);
    }

    /**
     * Returns the value at the given path as a ConfigValue.
     * Allows for nested keys using dot notation (e.g., "parent.child").
     *
     * @throws com.typesafe.config.ConfigException.Missing if the path does not exist
     */
    public ConfigValue get(String key) {
        return config.getValue(key);
    }

    /**
     * Returns the value at the given path and converts it to the specified type.
     *
     * @throws com.typesafe.config.ConfigException.Missing if the path does not exist
     */
    public <T> T get(String key, ConfigAdapter<T> adapter) {
        return adapter.fromConfig(config, key);
    }

    /**
     * Returns the value at the given path and converts it to the specified type.
     *
     * @param key the configuration key to retrieve
     * @param type the class type to convert the value to
     * @return the value at the specified key converted to the specified type
     * @param <T> the type to convert the value to
     * @throws IllegalStateException if no adapter is registered for the specified type
     */
    public <T> T get(String key, Class<T> type) {
        ConfigAdapter<T> adapter = AdapterRegistry.getAdapter(type);
        return get(key, adapter);
    }
}