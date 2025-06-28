package dev.shiftsad.core.config;

import org.jetbrains.annotations.NotNull;
import org.pkl.config.java.NoSuchChildException;
import org.pkl.config.java.mapper.ConversionException;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

public class ConfigurationInjector {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationInjector.class);

    private final @NotNull ConfigurationLoader loader;

    public ConfigurationInjector(@NotNull ConfigurationLoader loader) {
        this.loader = loader;
    }

    /**
     * Scans the specified package for static fields annotated with @Value and injects configuration values into them.
     * <p>
     * This injector only supports <strong>static</strong> fields. Non-static fields are ignored because
     * this class does not manage instance lifecycles, and injecting into transient, newly-created
     * instances is not useful.
     *
     * @param packageToScan the package to scan for annotated fields
     */
    public void configurate(String packageToScan) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(packageToScan)
                .addScanners(Scanners.FieldsAnnotated));

        Set<Field> annotatedFields = reflections.getFieldsAnnotatedWith(Value.class);
        if (annotatedFields.isEmpty()) {
            logger.debug("No fields annotated with @Value found in package: {}", packageToScan);
            return;
        }

        logger.debug("Found {} fields annotated with @Value", annotatedFields.size());
        for (Field field : annotatedFields) {
            int modifiers = field.getModifiers();
            Class<?> declaringClass = field.getDeclaringClass();
            String fieldName = field.getName();

            if (!Modifier.isStatic(modifiers)) {
                logger.trace("Skipping non-static field {}.{}. This injector only supports static fields.", declaringClass.getName(), fieldName);
                continue;
            }

            if (Modifier.isFinal(modifiers)) {
                logger.warn("Cannot inject value into final static field: {}.{}. Skipping.", declaringClass.getName(), fieldName);
                continue;
            }

            try {
                Value annotation = field.getAnnotation(Value.class);
                String configKey = annotation.value();

                Object value = loader.get(configKey, field.getType());
                field.setAccessible(true);
                field.set(null, value);

                logger.debug("Injected value for {}.{}: {}", declaringClass.getName(), fieldName, value);
            } catch (NoSuchChildException | ConversionException e) {
                String configKey = field.getAnnotation(Value.class).value();
                logger.warn("Could not find or convert config key '{}' for field {}.{}. Skipping. Error: {}",
                        configKey, declaringClass.getName(), fieldName, e.getMessage());
            } catch (Exception e) {
                logger.error("Failed to inject value into field {}.{}: {}", declaringClass.getName(), fieldName, e.getMessage(), e);
            }
        }
    }
}
