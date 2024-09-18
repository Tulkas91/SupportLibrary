plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "it.mm.support_library"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.mm.support_library"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {

    implementation(libs.android.volley)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.prefser)
    implementation(libs.reactiveandroid)
    implementation(libs.progressview)
    implementation(libs.androidx.databinding.runtime)
//    implementation(libs.apache.httpcomponents)
    implementation("org.apache.httpcomponents:httpmime:4.5.6") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    implementation("org.apache.httpcomponents:httpcore:4.4.16") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}