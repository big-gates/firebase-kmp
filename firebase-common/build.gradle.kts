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
        namespace = "com.biggates.firebase.common"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_17)
            }
        }

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    cocoapods {
        ios.deploymentTarget = libs.versions.ios.deploymentTarget.get()
        framework {
            baseName = "FirebaseCommon"
        }
        noPodspec()
        pod("FirebaseCore") {
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
                ) {
                    optIn("kotlinx.cinterop.ExperimentalForeignApi")
                }
            }
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            api(libs.firebase.common)
        }

        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifactId = "firebase-common"
        pom {
            name.set(artifactId)
            description.set("KMP Firebase common")
        }
    }
}