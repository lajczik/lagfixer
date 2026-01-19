plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
}

val spigotRepo = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/";
val paperRepo = "https://repo.papermc.io/repository/maven-public/";
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/";
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net";

version = "1.5.1.9"
extra["lagfixer_version"] = version
extra["lagfixer_build"] = "140"

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
        archiveVersion.set("")

        relocate("net.kyori", "xyz.lychee.lagfixer.libs.kyori")
        destinationDirectory.set(file("C:/Users/lajczi/Desktop/testowy/plugins"))  // Nowa lokalizacja
    }
}

allprojects {
    group = "xyz.lychee";

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
        compileOnly("org.projectlombok:lombok:1.18.32")
        annotationProcessor("org.projectlombok:lombok:1.18.32")
    }

    tasks {
        compileJava {
            options.encoding = "UTF-8"
        }
    }
}


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}