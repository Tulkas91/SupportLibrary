plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish") // Aggiungi questo plugin
}

android {
    namespace = "it.mm.support_library"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34

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

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.org.altbeacon)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.databinding.runtime)
    implementation(libs.android.volley)
    implementation(libs.material)
    implementation(libs.prefser)
    implementation(libs.progressview)
    implementation(libs.play.services.location)
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

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["release"]) // Usa il componente di tipo release

            groupId = "com.github.Tulkas91" // Il nome del gruppo (utente GitHub)
            artifactId = "SupportLibrary" // Il nome del repository GitHub
            version = "1.0.3" // La versione della tua libreria
        }
    }

    repositories {
        mavenLocal() // Per pubblicare in locale, necessario per JitPack
    }
}
