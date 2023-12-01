plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.lge.devicecare"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lge.devicecare"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    ndkVersion = "26.1.10909125"
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.room:room-runtime:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")




    //work manager libraries

    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:2.8.1")


    // optional - GCMNetworkManager support
    implementation("androidx.work:work-gcm:2.8.1")

    // optional - Test helpers
    androidTestImplementation("androidx.work:work-testing:2.8.1")

    // optional - Multiprocess support
    implementation("androidx.work:work-multiprocess:2.8.1")

   // implementation("io.github.xmaihh:serialport:2.1.1")
}