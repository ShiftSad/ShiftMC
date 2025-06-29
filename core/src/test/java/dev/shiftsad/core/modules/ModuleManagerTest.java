package dev.shiftsad.core.modules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

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
        Module module = spy(new TestModuleA());
        assertDoesNotThrow(() -> moduleManager.registerModule(module));
    }

    @Test
    void shouldThrowExceptionWhenRegisteringDuplicateModule() {
        Module module = spy(new TestModuleA());
        moduleManager.registerModule(module);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> moduleManager.registerModule(module)
        );

        assertTrue(exception.getMessage().contains("Module already registered"));
    }



    @Test
    void shouldEnableModuleWithoutDependencies() {
        Module module = spy(new TestModuleA());

        moduleManager.registerModule(module);
        moduleManager.enableModules();

        verify(module, times(1)).onEnable();
    }

    @Test
    void shouldEnableModulesInCorrectOrder() {
        Module moduleA = spy(new TestModuleA());
        Module moduleB = spy(new TestModuleB());

        when(moduleB.getDependencies()).thenReturn(List.of(TestModuleA.class));

        moduleManager.registerModule(moduleA);
        moduleManager.registerModule(moduleB);
        moduleManager.enableModules();

        InOrder inOrder = inOrder(moduleA, moduleB);
        inOrder.verify(moduleA).onEnable();
        inOrder.verify(moduleB).onEnable();
    }

    @Test
    void shouldNotEnableModuleTwice() {
        Module module = spy(new TestModuleA());

        moduleManager.registerModule(module);
        moduleManager.enableModules();
        moduleManager.enableModules();

        verify(module, times(1)).onEnable();
    }

    @Test
    void shouldThrowExceptionForMissingDependency() {
        Module moduleB = spy(new TestModuleB());
        when(moduleB.getDependencies()).thenReturn(List.of(TestModuleA.class));

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

        when(moduleA.getDependencies()).thenReturn(List.of(TestModuleB.class));
        when(moduleB.getDependencies()).thenReturn(List.of(TestModuleA.class));

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
        Module moduleA = spy(new TestModuleA());
        Module moduleB = spy(new TestModuleB());
        Module moduleC = spy(new TestModuleC());

        when(moduleB.getDependencies()).thenReturn(List.of(TestModuleA.class));
        when(moduleC.getDependencies()).thenReturn(List.of(TestModuleB.class));

        moduleManager.registerModule(moduleC);
        moduleManager.registerModule(moduleA);
        moduleManager.registerModule(moduleB);

        moduleManager.enableModules();

        InOrder inOrder = inOrder(moduleA, moduleB, moduleC);
        inOrder.verify(moduleA).onEnable();
        inOrder.verify(moduleB).onEnable();
        inOrder.verify(moduleC).onEnable();
    }

    private static class TestModuleA implements Module {
        @Override public void onEnable() {}
        @Override public void onDisable() {}
        @Override public void reload() {}
        @Override public boolean isReady() { return false; }
        @Override public List<Class<? extends Module>> getDependencies() { return Collections.emptyList(); }
    }

    private static class TestModuleB implements Module {
        @Override public void onEnable() {}
        @Override public void onDisable() {}
        @Override public void reload() {}
        @Override public boolean isReady() { return false; }
        @Override public List<Class<? extends Module>> getDependencies() { return Collections.emptyList(); }
    }

    // Added for the complex dependency test
    private static class TestModuleC implements Module {
        @Override public void onEnable() {}
        @Override public void onDisable() {}
        @Override public void reload() {}
        @Override public boolean isReady() { return false; }
        @Override public List<Class<? extends Module>> getDependencies() { return Collections.emptyList(); }
    }
}
