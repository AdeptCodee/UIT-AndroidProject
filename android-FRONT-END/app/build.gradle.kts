plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.chatrt"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.chatrt"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 1. Gọi API (Retrofit) - Vũ khí này thay thế hoàn toàn cho 'axios' bên Web của bạn
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // 2. Real-time (Socket.io) - Giúp nhắn tin nhảy liên tục không cần tải lại trang
    implementation("io.socket:socket.io-client:2.1.0")

    // 3. Hiển thị ảnh (Glide) - Xử lý việc tải ảnh từ Cloudinary về máy mượt mà
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 4. Giao diện (CircleImageView) - Công cụ hỗ trợ cắt ảnh đại diện thành hình tròn siêu nhanh
    implementation("de.hdodenhof:circleimageview:3.1.0")
}