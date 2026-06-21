plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    maven("https://github.com/Euphillya/FoliaDevBundle/raw/gh-pages/")
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    paperweight.foliaDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
}

configurations.all {
    exclude(group = "me.lucko", module = "spark-paper")
}