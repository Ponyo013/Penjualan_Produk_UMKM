plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
//    id("kotlin-kapt")
    id("kotlin-parcelize")
//    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.penjualan_produk_umkm"
    compileSdk = 36

    // Enable ML Model Binding
    buildFeatures {
        mlModelBinding = true
    }

    // Prevent compression of model files
    aaptOptions {
        noCompress("tflite")
        noCompress("json")
    }

    defaultConfig {
        applicationId = "com.example.penjualan_produk_umkm"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
        compose = true
        dataBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11

    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.1") // Atau versi terbaru
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // TensorFlow Lite
    implementation("org.tensorflow:tensorflow-lite:2.17.0")
    //Biometric Auth
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    // JSON parsing (if not already included)
    implementation("org.json:json:20230227")
    // Database
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.ui.test)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.dataconnect)
//    ksp(libs.room.compiler)
    //OkHttp untuk Image
    implementation("com.squareup.okhttp3:okhttp:4.11.0")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Coil for image loading
    implementation(libs.coil.kt)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Compose
    implementation(libs.threetenabp)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.material3)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.animation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.ui.tooling)
}
