plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.wellbeing"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wellbeing"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("com.airbnb.android:lottie:6.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")
    implementation ("org.jsoup:jsoup:1.14.3")
    implementation("com.android.volley:volley:1.2.1")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("org.ocpsoft.prettytime:prettytime:5.0.1.Final")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("androidx.activity:activity:1.8.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}