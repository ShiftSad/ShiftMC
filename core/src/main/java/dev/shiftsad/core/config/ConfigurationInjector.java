package dev.shiftsad.core.config;

import org.jetbrains.annotations.NotNull;
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
     * Scans the specified package for fields annotated with @Value and injects configuration values into them.
     * @param packageToScan the package to scan for annotated fields
     */
    public void configurate(String packageToScan) {
        var reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(packageToScan)
                .addScanners(Scanners.FieldsAnnotated));

        Set<Field> annotatedFields = reflections.getFieldsAnnotatedWith(Value.class);
        if (annotatedFields.isEmpty()) {
            logger.debug("No fields annotated with @Value found in package: {}", packageToScan);
            return;
        }

        logger.debug("Found {} fields annotated with @Value", annotatedFields.size());
        for (Field field : annotatedFields) {
            Class<?> declaringClass = field.getDeclaringClass();
            String fieldName = field.getName();

            Value annotation = field.getAnnotation(Value.class);
            String configKey = annotation.value();

            boolean isStaticField = Modifier.isStatic(field.getModifiers());
            if (Modifier.isFinal(field.getModifiers())) {
                logger.warn("→ Skipping final field: {}.{} (static: {})", declaringClass.getName(), fieldName, isStaticField);
                continue;
            }

            Object instance = null;
            if (!isStaticField) {
                try {
                    instance = declaringClass.getDeclaredConstructor().newInstance();
                    logger.debug("→ Created instance of {} for field: {}", declaringClass.getName(), fieldName);
                } catch (ReflectiveOperationException e) {
                    logger.error("Failed to create instance of {} for field: {}", declaringClass.getName(), fieldName, e);
                    continue;
                }
            }

            try {
                Object value = loader.get(configKey, field.getType());
                field.setAccessible(true);
                field.set(isStaticField ? null : instance, value);
                logger.debug("→ Set value for {}.{}: {}", declaringClass.getName(), fieldName, value);
            } catch (IllegalArgumentException e) {
                logger.error("Failed to set value for {}.{}: {}", declaringClass.getName(), fieldName, e.getMessage(), e);
            } catch (IllegalAccessException e) {
                logger.error("Illegal access while setting value for {}.{}: {}", declaringClass.getName(), fieldName, e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Unexpected error while setting value for {}.{}: {}", declaringClass.getName(), fieldName, e.getMessage(), e);
            }
        }
    }
}
