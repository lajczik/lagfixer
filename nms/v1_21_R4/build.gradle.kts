plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

paperweight {
    paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

dependencies {
    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
    compileOnly(project(":plugin"))
    //compileOnly(project(":nms:v1_21_R3"))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
}