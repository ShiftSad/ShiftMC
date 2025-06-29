package dev.shiftsad.core.config;

import com.typesafe.config.ConfigException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfigurationLoader Tests")
public class ConfigurationLoaderTest {

    @TempDir
    Path tempDir;

    private static final String EXISTING_CONFIG_FILENAME = "my-app-test.conf";
    private static final String RESOURCE_CONFIG_FILENAME = "test-config.conf";
    private static final String BAD_TYPE_CONFIG_FILENAME = "bad-type.conf";
    private static final String NON_EXISTENT_FILENAME = "definitely-not-here.conf";

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
        assertEquals("TestApp", loader.get("app.name", AdapterRegistry.getAdapter(String.class)));
        assertEquals(1.0, loader.get("app.version", AdapterRegistry.getAdapter(Double.class)));
        assertTrue(loader.get("app.enabled", AdapterRegistry.getAdapter(Boolean.class)));
    }

    @Test
    @DisplayName("Should retrieve ConfigValue object for a key")
    void testGet_keyExists_returnsConfigValue() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);
        assertNotNull(loader.get("app"));
        assertNotNull(loader.get("server"));
    }

    @Test
    @DisplayName("Should retrieve typed value for a key")
    void testGet_keyExists_returnsTypedValue() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);
        assertEquals("TestApp", loader.get("app.name", AdapterRegistry.getAdapter(String.class)));
        assertEquals(1.0, loader.get("app.version", AdapterRegistry.getAdapter(Double.class)));
        assertEquals(8080, loader.get("server.port", AdapterRegistry.getAdapter(Integer.class)));
        assertEquals(5000L, loader.get("server.timeout", AdapterRegistry.getAdapter(Long.class)));
    }

    @Test
    @DisplayName("Should throw ConfigException.Missing for a non-existent key")
    void testGet_keyDoesNotExist_throwsConfigExceptionMissing() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);
        assertThrows(ConfigException.Missing.class, () -> loader.get("nonexistent.key"));
        assertThrows(ConfigException.Missing.class, () -> loader.get("app.nonexistent", AdapterRegistry.getAdapter(String.class)));
    }

    @Test
    @DisplayName("Should throw ConfigException.WrongType for type mismatch")
    void testGet_typeMismatch_throwsConfigExceptionWrongType() throws IOException {
        // Prepare a config with a string value for "badType"
        Path badTypeConfig = tempDir.resolve(BAD_TYPE_CONFIG_FILENAME);
        Files.writeString(badTypeConfig, "badType = \"notAnInt\"");

        ConfigurationLoader loader = new ConfigurationLoader(BAD_TYPE_CONFIG_FILENAME, tempDir);
        assertThrows(ConfigException.WrongType.class, () -> loader.get("badType", AdapterRegistry.getAdapter(Integer.class)));
    }

    @Test
    @DisplayName("Should create file from resources if not existing")
    void testCreateFileFromResources_success() throws IOException {
        Path newFile = tempDir.resolve(RESOURCE_CONFIG_FILENAME);

        assertFalse(Files.exists(newFile), "Pre-condition: File should not exist before test.");

        ConfigurationLoader loader = new ConfigurationLoader(RESOURCE_CONFIG_FILENAME, tempDir);

        assertTrue(Files.exists(newFile), "File should have been created.");
        String content = Files.readString(newFile);
        assertTrue(content.contains("Hello Hocon"));
        assertEquals("Hello Hocon", loader.get("app.testString", AdapterRegistry.getAdapter(String.class)));
    }

    @Test
    @DisplayName("Should throw IOException if no file and no default resource")
    void testCreateFileFromResources_noDefaultResource_throwsIOException() {
        assertThrows(IOException.class, () -> new ConfigurationLoader(NON_EXISTENT_FILENAME, tempDir),
                "Should throw IOException if file doesn't exist and no default resource is found.");
    }

    // --- NOVOS TESTES PARA get(key, Class<T>) ---

    @Test
    @DisplayName("Should retrieve typed value using get(key, Class<T>)")
    void testGetByClass_success() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);

        assertEquals("TestApp", loader.get("app.name", String.class));
        assertEquals(1.0, loader.get("app.version", Double.class));
        assertTrue(loader.get("app.enabled", Boolean.class));
        assertEquals(8080, loader.get("server.port", Integer.class));
        assertEquals(5000L, loader.get("server.timeout", Long.class));
    }

    @Test
    @DisplayName("Should throw ConfigException.Missing for non-existent key using get(key, Class<T>)")
    void testGetByClass_missingKey() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);

        assertThrows(ConfigException.Missing.class, () -> loader.get("app.nonexistent", String.class));
    }

    @Test
    @DisplayName("Should throw ConfigException.WrongType for type mismatch using get(key, Class<T>)")
    void testGetByClass_wrongType() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);

        assertThrows(ConfigException.WrongType.class, () -> loader.get("app.name", Integer.class));
    }

    @Test
    @DisplayName("Should throw IllegalStateException if no adapter is registered using get(key, Class<T>)")
    void testGetByClass_noAdapter() throws IOException {
        ConfigurationLoader loader = new ConfigurationLoader(EXISTING_CONFIG_FILENAME, tempDir);

        class UnregisteredType {}

        assertThrows(IllegalStateException.class, () -> loader.get("app.name", UnregisteredType.class));
    }
}