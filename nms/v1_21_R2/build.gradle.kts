plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
<<<<<<< HEAD
}

repositories {
    maven("https://github.com/Euphillya/FoliaDevBundle/raw/gh-pages/")
=======
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
<<<<<<< HEAD
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
=======
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}

dependencies {
    paperweight.foliaDevBundle("1.21.3-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
<<<<<<< HEAD
=======
    compileOnly(project(":support:common"))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}