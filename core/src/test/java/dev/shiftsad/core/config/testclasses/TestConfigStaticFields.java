package dev.shiftsad.core.config.testclasses;

import dev.shiftsad.core.config.Value;

public class TestConfigStaticFields {

    @Value("app.testString")
    public static String myString;

    @Value("app.testInt")
    public static int myInt;

    @Value("app.testBoolean")
    public static boolean myBoolean;

    @Value("db.username")
    private static String dbUsername;

    public static void reset() {
        myString = null;
        myInt = 0;
        myBoolean = false;
        dbUsername = "";
    }

    public static String getMyString() {
        return myString;
    }

    public static int getMyInt() {
        return myInt;
    }

    public static boolean isMyBoolean() {
        return myBoolean;
    }

    public static String getDbUsername() {
        return dbUsername;
    }
}
