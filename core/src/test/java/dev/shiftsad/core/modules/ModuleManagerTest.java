package dev.shiftsad.core.modules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ModuleManagerTest {

    private ModuleManager moduleManager;

    @BeforeEach
    void setUp() {
        moduleManager = new ModuleManager();
    }

    @Test
    void shouldRegisterModule() {
        Module module = createMockModule(Collections.emptyList());

        assertDoesNotThrow(() -> moduleManager.registerModule(module));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringDuplicateModule() {
        Module module = createMockModule(Collections.emptyList());

        moduleManager.registerModule(module);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> moduleManager.registerModule(module)
        );

        assertTrue(exception.getMessage().contains("Module already registered"));
    }

    @Test
    void shouldEnableModuleWithoutDependencies() {
        Module module = createMockModule(Collections.emptyList());

        moduleManager.registerModule(module);
        moduleManager.enableModules();

        verify(module, times(1)).onEnable();
    }

    @Test
    void shouldEnableModulesInCorrectOrder() {
        Module moduleA = createMockModule(Collections.emptyList());
        Module moduleB = createMockModule(Arrays.asList(moduleA.getClass()));

        moduleManager.registerModule(moduleA);
        moduleManager.registerModule(moduleB);
        moduleManager.enableModules();

        verify(moduleA, times(1)).onEnable();
        verify(moduleB, times(1)).onEnable();
    }

    @Test
    void shouldNotEnableModuleTwice() {
        Module module = createMockModule(Collections.emptyList());

        moduleManager.registerModule(module);
        moduleManager.enableModules();
        moduleManager.enableModules();

        verify(module, times(1)).onEnable();
    }

    @Test
    void shouldThrowExceptionForMissingDependency() {
        Module moduleB = createMockModule(Arrays.asList(TestModuleA.class));

        moduleManager.registerModule(moduleB);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> moduleManager.enableModules()
        );

        assertTrue(exception.getMessage().contains("Missing dependency"));
    }

    @Test
    void shouldDetectCircularDependency() {
        Module moduleA = spy(new TestModuleA());
        Module moduleB = spy(new TestModuleB());

        when(moduleA.getDependencies()).thenReturn(Arrays.asList(TestModuleB.class));
        when(moduleB.getDependencies()).thenReturn(Arrays.asList(TestModuleA.class));

        moduleManager.registerModule(moduleA);
        moduleManager.registerModule(moduleB);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> moduleManager.enableModules()
        );

        assertTrue(exception.getMessage().contains("Circular dependency detected"));
    }

    @Test
    void shouldHandleComplexDependencyChain() {
        Module moduleA = createMockModule(Collections.emptyList());
        Module moduleB = createMockModule(Arrays.asList(moduleA.getClass()));
        Module moduleC = createMockModule(Arrays.asList(moduleB.getClass()));

        moduleManager.registerModule(moduleA);
        moduleManager.registerModule(moduleB);
        moduleManager.registerModule(moduleC);
        moduleManager.enableModules();

        verify(moduleA, times(1)).onEnable();
        verify(moduleB, times(1)).onEnable();
        verify(moduleC, times(1)).onEnable();
    }

    private Module createMockModule(List<Class<? extends Module>> dependencies) {
        Module module = mock(Module.class);
        when(module.getDependencies()).thenReturn(dependencies);
        return module;
    }

    // Classes auxiliares para testes
    private static class TestModuleA implements Module {
        @Override
        public void onEnable() {
        }

        @Override
        public void onDisable() {
        }

        @Override
        public void reload() {
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public List<Class<? extends Module>> getDependencies() {
            return Collections.emptyList();
        }
    }

    private static class TestModuleB implements Module {
        @Override
        public void onEnable() {
        }

        @Override
        public void onDisable() {
        }

        @Override
        public void reload() {
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public List<Class<? extends Module>> getDependencies() {
            return Collections.emptyList();
        }
    }
}