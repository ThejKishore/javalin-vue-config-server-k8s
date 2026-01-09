plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application
}

dependencies {
    // Project dependencies
    implementation(project(":shared"))
    implementation(project(":configserver"))

    // Javalin and core dependencies
    implementation(libs.javalin)
    implementation(libs.slf4jSimple)
    implementation(libs.javalinMicrometer)

    // DI
    implementation(libs.koinCore)
    implementation(libs.koinLoggerSlf4j)

    // JSON
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonKotlin)
    implementation(libs.jacksonJsr310)

    // Database
    implementation(libs.h2)
    implementation(libs.hikariCp)
    implementation(platform(libs.jdbiBom))
    implementation(libs.jdbiCore)
    implementation(libs.jdbiKotlin)
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "com.tk.learn.app.AppKt"
}
