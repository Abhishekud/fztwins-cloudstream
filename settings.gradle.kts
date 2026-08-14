rootProject.name = "FzTwinsPlugins"

// This file sets what projects are included. Every folder that has a build.gradle.kts
// is automatically included as a plugin module unless listed in "disabled" below.

val disabled = listOf<String>()

File(rootDir, ".").eachDir { dir ->
    if (!disabled.contains(dir.name) && File(dir, "build.gradle.kts").exists()) {
        include(dir.name)
    }
}

fun File.eachDir(block: (File) -> Unit) {
    listFiles()?.filter { it.isDirectory }?.forEach { block(it) }
}
