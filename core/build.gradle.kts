plugins {
    id("java")
}

group = "dev.shiftsad"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("com.typesafe:config:1.4.3")
    implementation("org.pkl-lang:pkl-config-java-all:0.28.2")
    implementation("com.intellij:annotations:12.0")
    implementation("org.reflections:reflections:0.10.2")
}

tasks.test {
    useJUnitPlatform()
}