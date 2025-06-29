package dev.shiftsad.core.modules;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModuleManager {

    private final Map<Class<? extends Module>, Module> modules = new HashMap<>();

    /**
     * Registers a module in the module manager.
     * A generic type parameter is used to ensure type safety.
     * @param module the module to register
     * @param <T> the type of the module, which must extend Module
     */
    public <T extends Module> void registerModule(@NotNull T module) {
        if (modules.containsKey(module.getClass())) {
            throw new IllegalArgumentException("Module already registered: " + module.getClass().getName());
        }
        modules.put(module.getClass(), module);
    }

    /**
     * Enables all registered modules in the module manager.
     * This method will enable each module and its dependencies recursively.
     */
    public void enableModules() {
        Set<Module> enabled = new HashSet<>();
        for (Module module : modules.values()) {
            Set<Module> visiting = new HashSet<>();
            enableModuleWithDependencies(module, enabled, visiting);
        }
    }

    /**
     * Enables a module and its dependencies.
     * If a module is already enabled, it will not be enabled again.
     * Dependencies are enabled recursively before the module itself.
     *
     * @param module the module to enable
     * @param enabled a set of already enabled modules to avoid re-enabling
     * @param visiting a set of modules currently being visited to detect cycles
     */
    private void enableModuleWithDependencies(Module module, @NotNull Set<Module> enabled, Set<Module> visiting) {
        if (enabled.contains(module)) return;

        if (visiting.contains(module)) {
            throw new RuntimeException("Circular dependency detected involving: " + module.getClass().getName());
        }

        visiting.add(module);

        for (Class<? extends Module> dep : module.getDependencies()) {
            Module depModule = modules.get(dep);
            if (depModule == null) throw new RuntimeException("Missing dependency: " + dep.getName());
            enableModuleWithDependencies(depModule, enabled, visiting);
        }

        visiting.remove(module);
        module.onEnable();
        enabled.add(module);
    }
}
