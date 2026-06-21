plugins {
    id("java")
}

group = "xyz.lychee.lagfixer"

repositories {
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.bg-software.com/repository/api/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://nexus.sirblobman.xyz/public/")
}

dependencies {
    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("com.mojang:authlib:3.11.50")
    compileOnly("org.apache.logging.log4j:log4j-core:2.22.1")
    compileOnly("com.github.placeholderapi:placeholderapi:2.11.6")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("com.bgsoftware:WildStackerAPI:2025.1")
    compileOnly("dev.rosewood:rosestacker:1.5.33")
    compileOnly("com.ticxo.modelengine:api:R3.2.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("com.songoda:UltimateStacker-API:1.0.0-SNAPSHOT")
    compileOnly("uk.antiperson.stackmob:StackMob:5.10.3")
    compileOnly(files("libs/LevelledMobs.jar"))

    compileOnly("commons-io:commons-io:2.22.0")
    compileOnly("com.github.oshi:oshi-core:7.3.1")
    compileOnly("org.apache.commons:commons-lang3:3.20.0")
}

tasks {
    processResources {
        filesMatching("**/paper-plugin.yml") {
            expand(rootProject.project.properties)
        }

        outputs.upToDateWhen { false }
    }
}