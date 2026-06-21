plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
}

<<<<<<< HEAD
=======
val spigotRepo = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
val paperRepo = "https://repo.papermc.io/repository/maven-public/"
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/"
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net"

version = "1.6.1"
<<<<<<< HEAD
=======
extra["lagfixer_version"] = version
extra["lagfixer_build"] = "143"
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90

dependencies {
    implementation(project(":plugin"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation(project(":nms:v1_20_R4"))
    implementation(project(":nms:v1_21_R1"))
    implementation(project(":nms:v1_21_R2"))
    implementation(project(":nms:v1_21_R3"))
    implementation(project(":nms:v1_21_R4"))
    implementation(project(":nms:v1_21_R5"))
    implementation(project(":nms:v1_21_R7"))
    implementation(project(":nms:v26_1"))
<<<<<<< HEAD
=======

    implementation(project(":support:common"))
    implementation(project(":support:spigot"))
    implementation(project(":support:paper"))
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("LagFixer")
<<<<<<< HEAD
        archiveClassifier.set("folia")

=======
        archiveClassifier.set("")

        relocate("net.kyori", "xyz.lychee.lagfixer.libs.kyori")
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
        destinationDirectory.set(file("C:/Users/lajczi/Desktop/testowy/plugins"))
    }
}

allprojects {
    group = "xyz.lychee"

    apply(plugin = "java")

    repositories {
        mavenLocal()
        mavenCentral()
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(mojang)
        maven(jitpack)
    }

    dependencies {
<<<<<<< HEAD
        compileOnly("org.projectlombok:lombok:1.18.44")
        annotationProcessor("org.projectlombok:lombok:1.18.44")
=======
        compileOnly("org.projectlombok:lombok:1.18.46")
        annotationProcessor("org.projectlombok:lombok:1.18.46")
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks {
        compileJava {
            options.encoding = Charsets.UTF_8.name()
            options.release = 21
        }
    }

    configurations {
        compileClasspath {
            attributes {
                attribute(TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE, 25)
            }
        }
    }
}