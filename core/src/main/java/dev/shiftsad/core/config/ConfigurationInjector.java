package dev.shiftsad.core.config;

import dev.shiftsad.core.config.adapters.ConfigAdapter;
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
     * Scans the specified package for static fields annotated with @Value and injects configuration values into them.
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

                @SuppressWarnings("unchecked")
                ConfigAdapter<Object> adapter = (ConfigAdapter<Object>) AdapterRegistry.getAdapter(field.getType());

                Object value = loader.get(configKey, adapter);
                field.setAccessible(true);
                field.set(null, value);

                logger.debug("Injected value for {}.{}: {}", declaringClass.getName(), fieldName, value);
            } catch (IllegalStateException e) {
                logger.warn("No adapter registered for type {} (field {}.{}). Skipping.", field.getType(), declaringClass.getName(), fieldName);
            } catch (Exception e) {
                String configKey = field.getAnnotation(Value.class).value();
                logger.error("Failed to inject value into field {}.{} (config key '{}'): {}", declaringClass.getName(), fieldName, configKey, e.getMessage(), e);
            }
        }
    }
}