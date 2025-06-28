package dev.shiftsad.core.config.testclasses;

import dev.shiftsad.core.config.Value;

public class TestConfigFinalField {

    @Value("app.testString")
    public static final String FINAL_STRING = "initial";
}
