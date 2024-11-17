val multidexVersion = "2.0.1"
val coreVersion = "1.12.0"
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("androidx.navigation.safeargs.kotlin")
   // id("com.google.gms.google-services")
  //  id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.softwareupdate"
    compileSdk = 34

    defaultConfig {
        applicationId = "ehtechapp.softwareupdate.supdate"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        setProperty("archivesBaseName", "SoftwareUpdate-v$versionCode($versionName)")

    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
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
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
 //   implementation("com.google.firebase:firebase-analytics:22.1.2")
 //   implementation("com.google.firebase:firebase-crashlytics:19.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.core:core-ktx:$coreVersion")

    implementation("com.airbnb.android:lottie:6.2.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    /** multidex dependency */
    implementation("androidx.multidex:multidex:$multidexVersion")
    /** hilt dependencies */
    implementation("com.google.dagger:hilt-android:2.44")
    kapt("com.google.dagger:hilt-android-compiler:2.44")
    /** dimens dependencies */
    implementation("com.intuit.ssp:ssp-android:1.0.5")
    implementation("com.intuit.sdp:sdp-android:1.0.5")

    /** glide dependency */
    implementation("com.github.bumptech.glide:glide:4.16.0")
//    /** ads dependency */
//    implementation("com.google.android.gms:play-services-ads:22.6.0")

    /** dexter for permission dependency */
    implementation("com.karumi:dexter:6.2.3")

    /** ads dependency */
//    implementation("com.google.android.gms:play-services-ads:22.6.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("org.jsoup:jsoup:1.13.1")

    implementation("org.greenrobot:eventbus:3.3.1")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")
}

kapt {
    correctErrorTypes = true
}