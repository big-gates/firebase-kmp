import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.cocoapods)
    alias(libs.plugins.mavenPublish)
}

kotlin {
    jvmToolchain(17)

    androidLibrary {
        namespace = "com.biggates.firebase.storage"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
    macosX64()
    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()
    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    cocoapods {
        ios.deploymentTarget = libs.versions.ios.deploymentTarget.get()
        osx.deploymentTarget = libs.versions.macos.deploymentTarget.get()
        tvos.deploymentTarget = libs.versions.tvos.deploymentTarget.get()
        watchos.deploymentTarget = libs.versions.watchos.deploymentTarget.get()
        framework {
            baseName = "FirebaseStorage"
        }
        noPodspec()
        pod("FirebaseStorage") {
            version = libs.versions.firebase.cocoapods.get()
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    sourceSets {
        all {
            languageSettings.apply {
                if (name.lowercase().contains("ios")
                    || name.lowercase().contains("apple")
                    || name.lowercase().contains("tvos")
                    || name.lowercase().contains("macos")
                    || name.lowercase().contains("watchos")
                ) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
        androidMain.dependencies {
            api(project.dependencies.platform(libs.firebase.bom))
            api(libs.firebase.storage)
        }

        commonMain.dependencies {
            api(projects.firebaseCommon)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(libs.firebase.admin)
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    val isLocalPublish = gradle.startParameter.taskNames.any { it.contains("publishToMavenLocal") }
    if (!isLocalPublish) {
        signAllPublications()
    }

    coordinates(
        groupId = "io.github.big-gates",
        artifactId = "firebase-storage",
        version = "0.0.1",
    )

    pom {
        name = "Firebase Storage (Kotlin Multiplatform)"
        description = "KMP bindings/wrappers for Firebase Storage (Android/Apple/JVM)."
        inceptionYear = "2025"
        url = "https://github.com/big-gates/firebase-kmp"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                id = "big-gates"
                name = "Big Gates"
                email = "biggatescorp@gamil.com"
                url = "https://github.com/big-gates"
            }
        }

        scm {
            url = "https://github.com/big-gates/firebase-kmp"
            connection = "scm:git:https://github.com/big-gates/firebase-kmp.git"
            developerConnection = "scm:git:ssh://git@github.com/big-gates/firebase-kmp.git"
        }
    }
}
