plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven("https://folia-inquisitors.github.io/FoliaDevBundle/")
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    paperweight.foliaDevBundle("1.21.5-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
}