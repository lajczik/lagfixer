plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

paperweight {
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
<<<<<<< HEAD
    paperweight.foliaDevBundle("26.1.2.build.+")
    compileOnly(project(":plugin"))
}

configurations.all {
    exclude(group = "net.kyori", module = "adventure-text-serializer-ansi")
=======
    paperweight.paperDevBundle("26.1.2.build.+")
    compileOnly(project(":plugin"))
    compileOnly(project(":support:common"))
}

tasks {
    reobfJar {
        enabled = false
    }
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}