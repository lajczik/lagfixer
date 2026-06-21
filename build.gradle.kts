plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
}

val paperRepo = "https://repo.papermc.io/repository/maven-public/"
val sonatypeRepo = "https://oss.sonatype.org/content/groups/public/"
val jitpack = "https://jitpack.io"
val mojang = "https://libraries.minecraft.net"

version = "1.6.1"

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
}

tasks {
    shadowJar {
        archiveBaseName.set("LagFixer")
        archiveClassifier.set("folia")

        destinationDirectory.set(file("C:/Users/lajczi/Desktop/testowy/plugins"))
    }
}

allprojects {
    group = "xyz.lychee";

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
        compileOnly("org.projectlombok:lombok:1.18.44")
        annotationProcessor("org.projectlombok:lombok:1.18.44")
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