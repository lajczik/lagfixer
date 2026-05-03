plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

paperweight {
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}