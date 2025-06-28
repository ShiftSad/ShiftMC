package dev.shiftsad.core.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pkl.config.java.Config;
import org.pkl.config.java.ConfigEvaluator;
import org.pkl.config.java.NoSuchChildException;
import org.pkl.config.java.mapper.ConversionException;
import org.pkl.core.ModuleSource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigurationLoader {

    private final Config config;

    public ConfigurationLoader(@NotNull String file, @Nullable Path target) throws IOException {
        String content = getFileContent(file, target);
        this.config = evaluateConfiguration(content);
    }

    /**
     * Get the content of the configuration file.
     * If the file is not available in the filesystem, it will attempt to create it from the resources.
     *
     * @param fileName the name of the configuration file
     * @return the content of the configuration file as a String
     * @throws IOException if an I/O error occurs while reading or creating the file
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
     * Use the PKL ConfigEvaluator to evaluate the configuration text.
     *
     * @param text the configuration text to evaluate
     * @return the evaluated Config object
     */
    private Config evaluateConfiguration(@NotNull String text) {
        Config config;

        try (var evaluator = ConfigEvaluator.preconfigured()) {
            config = evaluator.evaluate(ModuleSource.text(text));
        }

        return config;
    }

    /**
     * Returns the child node with the given unqualified name.
     * Allows for nested keys using dot notation (e.g., "parent.child").
     * GitHub issue related to warnings: <a href="https://github.com/apple/pkl/issues/1109">https://github.com/apple/pkl/issues/1109</a>
     *
     * @throws NoSuchChildException if a child with the given name does not exist
     */
    public Config get(String key) {
        String[] parts = key.split("\\.");
        Config current = config;
        for (String part : parts) {
            current = current.get(part);
        }
        return current;
    }

    /**
     * Returns the child node with the given unqualified name and converts it to the specified type.
     *
     * @throws NoSuchChildException if a child with the given name does not exist
     * @throws ConversionException if the value cannot be converted to the given type
     */
    public <T> T get(String key, Class<T> type) {
        return get(key).as(type);
    }
}