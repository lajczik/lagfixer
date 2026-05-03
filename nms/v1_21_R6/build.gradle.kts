plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

paperweight {
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
    compileOnly(project(":support:common"))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}