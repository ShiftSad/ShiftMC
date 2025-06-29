plugins {
    id("java")
}

group = "dev.shiftsad"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.mockito:mockito-core:5.18.0")
    mockitoAgent("org.mockito:mockito-core:5.18.0") { isTransitive = false }

    implementation("com.typesafe:config:1.4.3")
    implementation("com.intellij:annotations:12.0")
    implementation("org.reflections:reflections:0.10.2")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs!!.add("-javaagent:${mockitoAgent.asPath}")
}