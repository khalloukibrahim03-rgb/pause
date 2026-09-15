pluginManagement {
    gradlePluginPortal()
    google()
    mavenCentral()
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_BUILD_DIR_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "PAUSE"
include(":shared", ":intelligence", ":keyboard", ":app")
