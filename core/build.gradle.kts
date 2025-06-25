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

    implementation("org.pkl-lang:pkl-config-java-all:0.28.2")
}

tasks.test {
    useJUnitPlatform()
}