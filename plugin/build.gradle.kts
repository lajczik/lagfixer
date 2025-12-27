plugins {
    id("java")
    id("com.gradleup.shadow")
}

group = "xyz.lychee.lagfixer"

repositories {
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.bg-software.com/repository/api/")
    maven("https://mvn.lumine.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://nexus.sirblobman.xyz/public/")
    maven("https://maven.google.com")
}

dependencies {
    compileOnly(project(":support:common"))
    compileOnly(project(":support:spigot"))
    compileOnly(project(":support:paper"))

    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")

    compileOnly("com.github.placeholderapi:placeholderapi:2.11.6")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("com.bgsoftware:WildStackerAPI:2025.2")
    compileOnly("dev.rosewood:rosestacker:1.5.33")
    compileOnly("com.ticxo.modelengine:api:R3.2.0")
    compileOnly("io.lumine:Mythic-Dist:5.6.1")
    compileOnly("com.songoda:UltimateStacker-API:1.0.0-SNAPSHOT")
    compileOnly("uk.antiperson.stackmob:StackMob:5.10.3")
    compileOnly(files("libs/LevelledMobs.jar"))

    compileOnly("com.mojang:authlib:3.11.50")
    compileOnly("org.apache.logging.log4j:log4j-core:2.22.1")
    compileOnly("org.jetbrains:annotations:24.1.0")
    compileOnly("com.github.oshi:oshi-core:6.9.1")
    compileOnly("org.apache.commons:commons-lang3:3.19.0")

    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
    implementation("net.kyori:adventure-text-minimessage:4.25.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        minimize()
    }

    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
        outputs.upToDateWhen { false }
    }
}