plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.Test"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.Test"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

    implementation("com.naver.maps:map-sdk:3.21.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")                    //HTTP 통신을 위해
    implementation("com.google.android.gms:play-services-location:21.3.0")  //현위치
    implementation("com.google.android.material:material:1.12.0")           //버튼

}