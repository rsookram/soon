plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")

    id("dagger.hilt.android.plugin")
    id("com.squareup.wire")
}

wire {
    kotlin {
        android = true
    }
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "io.github.rsookram.soon"

        minSdk = 28
        targetSdk = 31

        versionCode = 1
        versionName = "1.0"

        resourceConfigurations += setOf("en", "anydpi")
    }

    lint {
        isCheckReleaseBuilds = false

        textReport = true

        isWarningsAsErrors = true
        isAbortOnError = true
    }

    packagingOptions.resources {
        excludes +=
            setOf(
                "kotlin/**",
                "**/DebugProbesKt.bin",
                "META-INF/*.version",
            )
    }

    dependenciesInfo { includeInApk = false }

    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        if (file("app.keystore").exists()) {
            create("release") {
                storeFile = file("app.keystore")
                storePassword = project.property("STORE_PASSWORD").toString()
                keyAlias = project.property("KEY_ALIAS").toString()
                keyPassword = project.property("KEY_PASSWORD").toString()
            }
        }
    }

    buildTypes {
        debug { signingConfig = signingConfigs.getByName("debug") }

        release {
            signingConfig =
                signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
    }

    buildFeatures { compose = true }

    composeOptions { kotlinCompilerExtensionVersion = libs.versions.compose.get() }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.hiltNavigationCompose)
    implementation(libs.androidx.viewmodelCompose)
    implementation(libs.androidx.navigation)

    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.uiTooling)

    implementation(libs.glance.core)
    implementation(libs.glance.appwidget)

    implementation(libs.accompanist.insets)
    implementation(libs.accompanist.insetsUi)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.datastore)

    implementation(libs.wire.runtime)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
