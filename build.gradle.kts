import com.lagradost.cloudstream3.gradle.CloudstreamExtension
import com.android.build.gradle.BaseExtension

buildscript {
    repositories {
        google()
        mavenCentral()
        // Shitpack repo which contains the CloudStream tools and dependencies
        maven("https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.4.2")
        // CloudStream gradle plugin which makes everything work and builds the .cs3 plugins
        classpath("com.github.recloudstream:gradle:-SNAPSHOT")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

fun Project.cloudstream(configuration: CloudstreamExtension.() -> Unit) =
    extensions.getByName<CloudstreamExtension>("cloudstream").configuration()

fun Project.android(configuration: BaseExtension.() -> Unit) =
    extensions.getByName<BaseExtension>("android").configuration()

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    cloudstream {
        // When running through the GitHub workflow, GITHUB_REPOSITORY holds the current repo name.
        // Change the fallback below to YOUR GitHub repo (user/repo) once you fork/create it.
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "https://github.com/Abhishekud/fztwins-cloudstream")

        authors = listOf("FzTwins")
    }

    android {
        defaultConfig {
            minSdk = 21
            compileSdkVersion(33)
            targetSdk = 33
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8
        }

        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions {
                jvmTarget = "1.8" // Required
                freeCompilerArgs = freeCompilerArgs +
                    "-Xno-call-assertions" +
                    "-Xno-param-assertions" +
                    "-Xno-receiver-assertions"
            }
        }
    }

    dependencies {
        val apk by configurations
        val implementation by configurations

        // Stubs for all CloudStream classes
        apk("com.lagradost:cloudstream3:pre-release")

        implementation(kotlin("stdlib")) // standard kotlin features
        implementation("com.github.Blatzar:NiceHttp:0.4.11") // http library
        implementation("org.jsoup:jsoup:1.17.2") // html parser
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    }
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}
