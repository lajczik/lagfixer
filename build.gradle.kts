plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
}

val spigotRepo = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
val paperRepo = "https://repo.papermc.io/repository/maven-public/"
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/"
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net"

version = "1.6.3"
extra["lagfixer_version"] = version
extra["lagfixer_build"] = "145"

dependencies {
    implementation(project(":plugin"))

    implementation(project(":nms:v1_16_R3"))
    implementation(project(":nms:v1_17_R1"))
    implementation(project(":nms:v1_18_R2"))
    implementation(project(":nms:v1_19_R3"))
    implementation(project(":nms:v1_20_R1"))
    implementation(project(":nms:v1_20_R2"))
    implementation(project(":nms:v1_20_R3"))
    implementation(project(":nms:v1_20_R4"))
    implementation(project(":nms:v1_21_R1"))
    implementation(project(":nms:v1_21_R2"))
    implementation(project(":nms:v1_21_R3"))
    implementation(project(":nms:v1_21_R4"))
    implementation(project(":nms:v1_21_R5"))
    implementation(project(":nms:v1_21_R6"))
    implementation(project(":nms:v1_21_R7"))
    implementation(project(":nms:v26_1"))
    implementation(project(":nms:v26_2"))

    implementation(project(":support:common"))
    implementation(project(":support:spigot"))
    implementation(project(":support:paper"))
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("LagFixer")
        archiveClassifier.set("")

        relocate("net.kyori", "xyz.lychee.lagfixer.libs.kyori")
        destinationDirectory.set(file("C:/Users/lajczi/Desktop/testowy/plugins"))
    }
}

allprojects {
    group = "xyz.lychee"

    apply(plugin = "java")

    repositories {
        mavenLocal()
        mavenCentral()
        maven(spigotRepo)
        maven(paperRepo)
        maven(sonatypeRepo)
        maven(mojang)
        maven(jitpack)
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.46")
        annotationProcessor("org.projectlombok:lombok:1.18.46")
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