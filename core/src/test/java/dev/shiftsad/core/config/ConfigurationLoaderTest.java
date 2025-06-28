package dev.shiftsad.core.config;

import dev.shiftsad.core.config.ConfigurationLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.pkl.config.java.NoSuchChildException;
import org.pkl.config.java.mapper.ConversionException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfigurationLoader Tests")
public class ConfigurationLoaderTest {

    @TempDir
    Path tempDir;

    private static final String EXISTING_CONFIG_FILENAME = "my-app-test.pkl";
    private static final String RESOURCE_CONFIG_FILENAME = "test-config.pkl";
    private static final String BAD_TYPE_CONFIG_FILENAME = "bad-type.pkl";
    private static final String NON_EXISTENT_FILENAME = "definitely-not-here.pkl";

    @BeforeEach
    void setUp() throws IOException {
        Path tempConfigFile = tempDir.resolve(EXISTING_CONFIG_FILENAME);
        Files.writeString(tempConfigFile,
                """
                app {
                  name = "TestApp"
                  version = 1.0
                  enabled = true
                }
                server {
                  port = 8080
                  timeout = 5000
                }""");
    }

    @Test
    @DisplayName("Should load an existing configuration file successfully")
    void testLoadExistingFile_success() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);
        assertNotNull(loader);
        assertEquals("TestApp", loader.get("app.name", String.class));
        assertEquals(1.0, loader.get("app.version", Double.class));
        assertTrue(loader.get("app.enabled", Boolean.class));
    }

    @Test
    @DisplayName("Should retrieve Config object for a key")
    void testGet_keyExists_returnsConfig() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);
        assertNotNull(loader.get("app"));
        assertNotNull(loader.get("server"));
    }

    @Test
    @DisplayName("Should retrieve typed value for a key")
    void testGet_keyExists_returnsTypedValue() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);
        assertEquals("TestApp", loader.get("app.name", String.class));
        assertEquals(1.0, loader.get("app.version", Double.class));
        assertEquals(8080, loader.get("server.port", int.class));
        assertEquals(5000L, loader.get("server.timeout", long.class));
    }

    @Test
    @DisplayName("Should throw NoSuchChildException for a non-existent key")
    void testGet_keyDoesNotExist_throwsNoSuchChildException() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);
        assertThrows(NoSuchChildException.class, () -> loader.get("nonexistent.key"));
        assertThrows(NoSuchChildException.class, () -> loader.get("app.nonexistent", String.class));
    }

    @Test
    @DisplayName("Should throw ConversionException for type mismatch")
    void testGet_typeMismatch_throwsConversionException() throws IOException {
        Path badTypeConfig = tempDir.resolve(BAD_TYPE_CONFIG_FILENAME);
        Files.copy(Objects.requireNonNull(getClass().getResourceAsStream("/" + RESOURCE_CONFIG_FILENAME)), badTypeConfig);

        ConfigurationLoader loader = new ConfigurationLoader(BAD_TYPE_CONFIG_FILENAME, tempDir);
        assertThrows(ConversionException.class, () -> loader.get("badType", Integer.class));
    }

    @Test
    @DisplayName("Should create file from resources if not existing")
    void testCreateFileFromResources_success() throws IOException {
        Path newFile = tempDir.resolve(RESOURCE_CONFIG_FILENAME);

        assertFalse(Files.exists(newFile), "Pre-condition: File should not exist before test.");

        ConfigurationLoader loader = new ConfigurationLoader(RESOURCE_CONFIG_FILENAME, tempDir);

        assertTrue(Files.exists(newFile), "File should have been created.");
        String content = Files.readString(newFile);
        assertTrue(content.contains("Hello Pkl"));
        assertEquals("Hello Pkl", loader.get("app.testString", String.class));
    }

    @Test
    @DisplayName("Should throw IOException if no file and no default resource")
    void testCreateFileFromResources_noDefaultResource_throwsIOException() {
        assertThrows(IOException.class, () -> new ConfigurationLoader(NON_EXISTENT_FILENAME, tempDir),
                "Should throw IOException if file doesn't exist and no default resource is found.");
    }
}
