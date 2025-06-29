package dev.shiftsad.core.modules;

import java.util.List;

public interface Module {
    void onEnable();
    void onDisable();
    void reload();
    boolean isReady();
    List<Class<? extends Module>> getDependencies();
}