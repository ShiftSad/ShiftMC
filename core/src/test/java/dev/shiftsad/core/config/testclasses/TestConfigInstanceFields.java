package dev.shiftsad.core.config.testclasses;

import dev.shiftsad.core.config.Value;

public class TestConfigInstanceFields {

    @Value("app.testString")
    public String instanceString;

    @Value("non.existent.key")
    public String missingKeyValue;

    @Value("badType")
    public int badTypeInt;

    public TestConfigInstanceFields() {
    }
}
