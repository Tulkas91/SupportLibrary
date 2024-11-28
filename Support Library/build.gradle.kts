plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("maven-publish") // Plugin per la pubblicazione
}

android {
    namespace = "it.mm.supportlibrary"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

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

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Dipendenze principali
    implementation(libs.org.altbeacon)
    api(libs.androidx.activity)
    api(libs.androidx.appcompat)
    api(libs.androidx.biometric)
    api(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.databinding.runtime)
    api(libs.android.volley)
    api(libs.material)
    api(libs.prefser)
    api(libs.progressview)
    implementation(libs.play.services.location)
    implementation(libs.support.annotations)
    implementation(libs.commons.lang3)
    api(libs.retrofit)
    api(libs.converter.gson)
    api(libs.simplesearchview)

    implementation(libs.okhttp)
    implementation(libs.okhttp.urlconnection) // Supporto per HttpURLConnection
    implementation(libs.logging.interceptor)

    // Esclusione delle dipendenze inutili per httpmime e httpcore
    implementation("org.apache.httpcomponents:httpmime:4.5.6") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }
    implementation("org.apache.httpcomponents:httpcore:4.4.16") {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
    }

    // Dipendenze per ReactiveAndroid e Test
    implementation(libs.rxjava)
    implementation(libs.review.ktx)
    implementation(libs.core.ktx)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.assertj.core)
    testImplementation(libs.robolectric)
}

//git tag v1.2.16
//git push origin v1.2.16

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"]) // Usa il componente 'release' per le librerie Android
            }

            groupId = "com.github.Tulkas91" // ID del gruppo, usa il tuo username GitHub
            artifactId = "SupportLibrary" // Nome della libreria/repository su GitHub
            version = "1.3.7" // Versione della libreria
        }
    }

    repositories {
        mavenLocal() // Pubblica localmente, necessario per JitPack
        // Se in futuro vuoi pubblicare su MavenCentral, puoi aggiungere qui la configurazione per Maven Central
        // maven {
        //     name = "MavenCentral"
        //     url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        //     credentials {
        //         username = project.findProperty("mavenUsername") as String? ?: ""
        //         password = project.findProperty("mavenPassword") as String? ?: ""
        //     }
        // }
    }
}
