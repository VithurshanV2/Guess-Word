plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mad.guessword"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mad.guessword"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DREAMLO_PUBLIC_KEY","\"670538128f40c6124c1a6b96\"")
        buildConfigField("String", "DREAMLO_PRIVATE_KEY","\"rPfuV3b8yUmUbVZle385eABlMStX7Cuk2I5oSEB59FhQ\"")
        buildConfigField("String", "API_KEY","\"FZGrxmZmz1WQUmtyZyBdgQ==m1STvzY4BfNBKTH8\"")

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
    buildFeatures{
        buildConfig = true;
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.android.volley:volley:1.2.1")
}