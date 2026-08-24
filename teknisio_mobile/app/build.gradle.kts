plugins {
    alias(libs.plugins.android.application)
}

val releaseApiBaseUrl = providers.gradleProperty("TEKNISIO_API_BASE_URL")
    .orElse("https://api.example.invalid/")
    .get()

require(releaseApiBaseUrl.startsWith("https://")) {
    "TEKNISIO_API_BASE_URL untuk release wajib menggunakan HTTPS"
}

val debugApiBaseUrl = providers.gradleProperty("TEKNISIO_DEBUG_API_BASE_URL")
    .orElse("http://10.0.2.2:8080/")
    .get()

android {
    namespace = "com.teknisio.mobile"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.teknisio.mobile"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "0.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
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
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Map — OpenStreetMap (no API key required)
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
