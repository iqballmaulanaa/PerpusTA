plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.perpussapp.perpusapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.perpussapp.perpusapp"
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
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
   /* implementation("some.library:that-brings-support") {
        exclude( group = ("com.android.support"), module = ("support-compat"))
    }*/
   /* implementation("androidx.core:core-ktx:1.12.0") {
        exclude( group = ("com.android.support"), module = ("support-compat"))
    }*/
    implementation(platform("com.google.firebase:firebase-bom:29.0.0"))
    implementation ("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-storage")
    implementation ("com.joooonho:selectableroundedimageview:1.0.1")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.11.0")
    implementation ("com.balysv:material-ripple:1.0.2")
    implementation ("com.github.f0ris.sweetalert:library:1.5.6")
    implementation ("com.github.dhaval2404:imagepicker:2.0")
    implementation ("com.github.mohammadatif:Animatoo:master")
    implementation ("com.github.cachapa:ExpandableLayout:2.9.2")
    implementation ("com.github.jrizani:JRSpinner:androidx-SNAPSHOT")
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.github.chrisbanes:PhotoView:2.3.0")
}