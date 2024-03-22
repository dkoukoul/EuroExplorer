import java.util.Calendar
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
    kotlin("plugin.serialization")
}


android {
    namespace = "co.adaptive.euroexplorer"
    compileSdk = 34

    defaultConfig {
        applicationId = "co.adaptive.euroexplorer"
        minSdk = 30
        targetSdk = 34
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        versionCode = 10000 + month * 100 + day
        versionName = "1.$month.$day"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val properties = Properties()
        properties.load(project.rootProject.file("secrets.properties").inputStream())
        val googleMapsApiKey = properties.getProperty("GoogleMapsApiKey")
        buildConfigField("String", "GoogleMapsApiKey", "\"${googleMapsApiKey}\"")
        resValue("string", "Google_Maps_Api_Key", googleMapsApiKey)
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation(libs.play.services.ads.lite)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}