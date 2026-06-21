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
    paperweight.foliaDevBundle("26.1.2.build.+")
    compileOnly(project(":plugin"))
}

configurations.all {
    exclude(group = "net.kyori", module = "adventure-text-serializer-ansi")
}