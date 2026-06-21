plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
<<<<<<< HEAD
        languageVersion = JavaLanguageVersion.of(21)
    }
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
=======
        languageVersion = JavaLanguageVersion.of(17)
    }
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}

dependencies {
    paperweight.foliaDevBundle("1.20.2-R0.1-SNAPSHOT")
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