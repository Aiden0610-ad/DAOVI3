plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "vn.daovi.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.daovi.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    // Khóa ký cố định để bản cập nhật cài đè lên bản cũ mà không mất dữ liệu.
    // Chỉ dùng cho cài đặt cá nhân; hãy để kho mã ở chế độ riêng tư.
    signingConfigs {
        create("daovi") {
            storeFile = file("daovi.keystore")
            storePassword = "daovi-dao"
            keyAlias = "daovi"
            keyPassword = "daovi-dao"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("daovi")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("daovi")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}
