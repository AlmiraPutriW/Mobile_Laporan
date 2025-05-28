plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mobilelaporanapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.mobilelaporanapp"
        minSdk = 29
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
    buildFeatures {
        viewBinding = true
    }
}
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:deprecation")
}


    dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Tambahan untuk Volley (dengan format Kotlin DSL)
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.material:material:1.9.0")
// Retrofit dan GSON
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("com.google.code.gson:gson:2.10.1")

        implementation("com.github.bumptech.glide:glide:4.15.1")
        annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    }
