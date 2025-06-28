package dev.shiftsad.core.config;

import dev.shiftsad.core.config.testclasses.TestConfigFinalField;
import dev.shiftsad.core.config.testclasses.TestConfigInstanceFields;
import dev.shiftsad.core.config.testclasses.TestConfigStaticFields;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfigurationInjector Tests")
public class ConfigurationInjectorTest {

    @TempDir
    Path tempDir;

    private ConfigurationLoader loader;
    private ConfigurationInjector injector;

    private static final String TEST_PACKAGE = "dev.shiftsad.core.config.testclasses";
    private static final String CONFIG_FILE_NAME = "test-injector-config.pkl";

    @BeforeEach
    void setUp() throws IOException {
        Path configFile = tempDir.resolve(CONFIG_FILE_NAME);
        String configContent = """
                app {
                  testString = "Injected Value"
                  testInt = 42
                  testBoolean = true
                }
                db {
                  username = "injected_user"
                }
                badType = "this is not an integer"
                """;
        Files.writeString(configFile, configContent);

        loader = new ConfigurationLoader(CONFIG_FILE_NAME, tempDir);
        injector = new ConfigurationInjector(loader);

        TestConfigStaticFields.reset();
    }

    @AfterEach
    void tearDown() {
        TestConfigStaticFields.reset();
    }

    @Test
    @DisplayName("Should inject values into static fields")
    void injectsValuesIntoStaticFields() {
        assertNull(TestConfigStaticFields.getMyString(), "Pre-condition: String should be null");
        assertEquals(0, TestConfigStaticFields.getMyInt(), "Pre-condition: int should be 0");
        assertFalse(TestConfigStaticFields.isMyBoolean(), "Pre-condition: boolean should be false");

        injector.configurate(TEST_PACKAGE);

        assertEquals("Injected Value", TestConfigStaticFields.getMyString());
        assertEquals(42, TestConfigStaticFields.getMyInt());
        assertTrue(TestConfigStaticFields.isMyBoolean());
    }

    @Test
    @DisplayName("Should inject values into private static fields")
    void injectsValuesIntoPrivateStaticFields() {
        assertEquals("", TestConfigStaticFields.getDbUsername(), "Pre-condition: private field should be empty");

        injector.configurate(TEST_PACKAGE);

        assertEquals("injected_user", TestConfigStaticFields.getDbUsername());
    }

    @Test
    @DisplayName("Should skip final fields and not change their value")
    void skipsFinalFields() {
        String initialValue = TestConfigFinalField.FINAL_STRING;
        assertEquals("initial", initialValue, "Pre-condition: Final field should have its initial value");

        injector.configurate(TEST_PACKAGE);

        assertEquals(initialValue, TestConfigFinalField.FINAL_STRING, "Final field value should not be changed");
    }

    @Test
    @DisplayName("Should not throw exception for non-existent keys and continue injection")
    void handlesMissingKeysGracefully() {
        assertDoesNotThrow(() -> injector.configurate(TEST_PACKAGE),
                "Injector should not throw an exception for missing keys.");

        assertEquals(42, TestConfigStaticFields.getMyInt(),
                "Other fields should still be injected even if some keys are missing.");
    }

    @Test
    @DisplayName("Should not throw exception for type conversion errors and continue injection")
    void handlesTypeMismatchGracefully() {
        assertDoesNotThrow(() -> injector.configurate(TEST_PACKAGE),
                "Injector should not throw an exception for type mismatches.");

        assertEquals("Injected Value", TestConfigStaticFields.getMyString(),
                "Other fields should still be injected even if a type mismatch occurs.");
    }

    @Test
    @DisplayName("Should not throw exception for classes without no-arg constructor")
    void handlesClassWithoutNoArgConstructorGracefully() {
        assertDoesNotThrow(() -> injector.configurate(TEST_PACKAGE),
                "Injector should not throw an exception when a class lacks a no-arg constructor.");

        assertTrue(TestConfigStaticFields.isMyBoolean(),
                "Other fields should still be injected even if an instance cannot be created.");
    }

    @Test
    @DisplayName("Should not inject into instance fields as new instances are created and discarded")
    void instanceFieldInjectionIsANoOp() {
        TestConfigInstanceFields instance = new TestConfigInstanceFields();
        assertNull(instance.instanceString, "Pre-condition: instance field should be null");

        injector.configurate(TEST_PACKAGE);

        assertNull(instance.instanceString,
                "Instance field should remain null because the injector creates and injects into a different, discarded instance.");
    }

    @Test
    @DisplayName("Should not crash when scanning a package with no annotated fields")
    void handlesPackageWithNoAnnotatedFields() {
        assertDoesNotThrow(() -> injector.configurate("java.lang"),
                "Injector should run without error on a package with no @Value annotations.");
    }
}