plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    // Project dependencies
    implementation(project(":shared"))

    // Kotlin stdlib
    implementation(libs.kotlinStdlib)

    // Javalin Web Framework
    implementation(libs.javalin)
    implementation(libs.slf4jSimple)
    implementation(libs.javalinMicrometer)

    // Database
    implementation(libs.h2)
    implementation(libs.hikariCp)
    implementation(platform(libs.jdbiBom))
    implementation(libs.jdbiCore)
    implementation(libs.jdbiKotlin)

    // Dependency Injection
    implementation(libs.koinCore)
    implementation(libs.koinLoggerSlf4j)

    // JSON & Serialization
    implementation(libs.jacksonDatabind)
    implementation(libs.jacksonKotlin)
    implementation(libs.jacksonJsr310)

    // Kotlinx
    implementation(libs.kotlinxDatetime)
    implementation(libs.kotlinxSerialization)

    // YAML
    implementation(libs.snakeyaml)

    // Validation
    implementation(libs.valiktorCore)

    // Metrics
    implementation(libs.micrometerPrometheus)

    // OpenAPI
    implementation(libs.openapiPlugin)
    implementation(libs.openapiSwagger)

    // Testing
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitJupiter)
    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
}

