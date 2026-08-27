plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.agroaid"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.agroaid"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "PLANT_ID_API_KEY", "\"Jh4oZGttde5jcGbJmfQLdjEAeMsAW0Kt00T3wkGK1NfJwEC1cJ\"")
        buildConfigField("String", "ANTHROPIC_API_KEY", "\"sk-ant-YOUR_KEY_HERE\"")
    }

    signingConfigs {
        create("release") {
            storeFile = file("C:/Users/DEEPAK/keystore/agroaid.jks")
            storePassword = "AgroAid123"
            keyAlias = "AgroAid"
            keyPassword = "AgroAid123"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.11.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")

    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("com.google.mlkit:translate:17.0.1")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.firebase:firebase-messaging")
}