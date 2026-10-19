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
    paperweight.paperDevBundle("26.3.build.+")
    compileOnly(project(":plugin"))
    compileOnly(project(":support:common"))
}