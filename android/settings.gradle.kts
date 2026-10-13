@Suppress("UNCHECKED_CAST")
try {
    System.getenv()
    val peClass = Class.forName("java.lang.ProcessEnvironment")
    val envField = peClass.getDeclaredField("theEnvironment")
    envField.isAccessible = true
    val envMap = envField.get(null) as MutableMap<String, String>
    envMap.remove("ANDROID_PREFS_ROOT")
    
    val caseInsensitiveEnvField = peClass.getDeclaredField("theCaseInsensitiveEnvironment")
    caseInsensitiveEnvField.isAccessible = true
    val caseInsensitiveEnvMap = caseInsensitiveEnvField.get(null) as MutableMap<String, String>
    caseInsensitiveEnvMap.remove("ANDROID_PREFS_ROOT")
} catch (_: Exception) {
}

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AGAPE"
include(":app")

