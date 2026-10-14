plugins {
    id("fabric-loom") version "1.14-SNAPSHOT"
}

group = "com.example"
version = "1.0.0"

repositories {
    maven("https://maven.meteordev.org/releases")
    maven("https://maven.meteordev.org/snapshots")
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings("net.fabricmc:yarn:1.21.11+build.4:v2")
    modImplementation("net.fabricmc:fabric-loader:0.18.4")
    modImplementation("meteordevelopment:meteor-client:1.21.11-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile> {
    options.release = 21
}
