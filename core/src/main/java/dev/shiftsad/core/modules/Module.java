package dev.shiftsad.core.modules;

public interface Module {
    void onEnable();
    void onDisable();
    void reload();
    boolean isReady();
}