plugins {
    id("java")
    id("com.gradleup.shadow")
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("26.1.2.build.+")
    compileOnly(project(":plugin"))
}

tasks {
    reobfJar {
        enabled = false
    }
}