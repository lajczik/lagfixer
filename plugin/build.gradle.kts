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
    maven("https://repo.helpch.at/releases/")
    maven("https://central.sonatype.com/")
    maven("https://maven.google.com")
}

dependencies {
    compileOnly(project(":support:common"))
    compileOnly(project(":support:spigot"))
    compileOnly(project(":support:paper"))

    compileOnly("org.spigotmc:spigot-api:1.13.2-R0.1-SNAPSHOT")

    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("com.bgsoftware:WildStackerAPI:2025.2")
    compileOnly("dev.rosewood:rosestacker:1.5.38")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
    compileOnly("io.lumine:Mythic-Dist:5.9.5")
    compileOnly("com.songoda:UltimateStacker-API:1.0.0-SNAPSHOT")
    compileOnly("uk.antiperson.stackmob:StackMob:5.10.3")
    compileOnly("io.github.arcaneplugins:levelledmobs-plugin:4.0.3.1")

    compileOnly("com.mojang:authlib:3.11.50")
    compileOnly("org.apache.logging.log4j:log4j-core:2.17.2")
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("com.github.oshi:oshi-core:7.2.0")
    compileOnly("org.apache.commons:commons-lang3:3.20.0")

    implementation("net.kyori:adventure-platform-bukkit:4.4.1")
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
        outputs.upToDateWhen { false }
    }
}