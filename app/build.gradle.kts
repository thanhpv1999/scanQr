plugins {
    id (Plugin.androidApplication)
    id (Plugin.androidKotlin)
    kotlin(Plugin.kotlinAndroidKapt)
    id (Plugin.daggerHilt)
    id ("com.google.gms.google-services")
}

android {
    namespace = Apps.applicationId
    compileSdk = Apps.compileSdk

    defaultConfig {
        applicationId = Apps.applicationId
        minSdk = Apps.minSdk
        targetSdk = Apps.targetSdk
        versionCode = Apps.versionCode
        versionName = Apps.versionName

        testInstrumentationRunner = TestDependencies.instrumentationRunner
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    android.apply {
        dataBinding.enable = true
        viewBinding.enable = true
    }
}

dependencies {
    implementation(project(":design"))
    implementation(project(":scanner"))
    androidX()
    daggerHilt()
    testEspressoCore()
    jUnit()
    navigation()

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
}