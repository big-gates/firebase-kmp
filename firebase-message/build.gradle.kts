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
        namespace = "com.biggates.firebase.message"
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

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        ios.deploymentTarget = libs.versions.ios.deploymentTarget.get()
        framework {
            baseName = "FirebaseMessaging"
        }
        noPodspec()
        pod("FirebaseMessaging") {
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
            api(project.dependencies.platform(libs.firebase.bom))
            api(libs.firebase.messaging)
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
            implementation(libs.google.cloud.pubsub)
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.testExt.junit)
            }
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
        artifactId = "firebase-message",
        version = "0.0.1",
    )

    pom {
        name = "Firebase Message (Kotlin Multiplatform)"
        description = "KMP bindings/wrappers for Firebase Message (Android/iOS/JVM)."
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
