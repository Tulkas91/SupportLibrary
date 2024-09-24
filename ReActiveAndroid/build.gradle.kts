plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "it.sikuel.reactiveandroid"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.support.annotations)
    implementation(libs.rxjava)

    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}