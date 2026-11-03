plugins {
    id("com.android.application")
}

android {
    namespace = "io.github.thinca.xtoany"
    compileSdk = 37

    defaultConfig {
        applicationId = "io.github.thinca.xtoany"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.11.0")
    testImplementation("junit:junit:4.13.2")
}
