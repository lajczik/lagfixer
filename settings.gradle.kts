pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "LagFixer"

include("plugin")

include("nms:v1_20_R1")
include("nms:v1_20_R2")
include("nms:v1_20_R3")
include("nms:v1_20_R4")
include("nms:v1_21_R1")
include("nms:v1_21_R2")
include("nms:v1_21_R3")
include("nms:v1_21_R4")
include("nms:v1_21_R5")
<<<<<<< HEAD
include("nms:v1_21_R7")
include("nms:v26_1")
=======
include("nms:v1_21_R6")
include("nms:v1_21_R7")
include("nms:v26_1")

include("support:paper")
include("support:spigot")
include("support:common")
>>>>>>> 559dd4fc5cf73115924d60b1ed04a0a70832ae90
