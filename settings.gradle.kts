pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url  = uri("https://plugins.gradle.org/m2/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {  url  = uri("https://maven.google.com") }
        maven {  url  = uri("https://jitpack.io") }
    }
}

rootProject.name = "AndroidWidgetApp"
include(":app")
//include(":stories")
include(":ketch")
