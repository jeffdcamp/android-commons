import com.android.build.gradle.tasks.GenerateBuildConfig

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.download)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependencyAnalysis)
    `maven-publish`
    signing
}

android {
    namespace = "org.dbtools.android.commons"

    compileSdk = AndroidSdk.COMPILE

    defaultConfig {
        minSdk = AndroidSdk.MIN
        targetSdk = AndroidSdk.TARGET
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf("-module-name", Pom.LIBRARY_ARTIFACT_ID)
        freeCompilerArgs += listOf(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlinx.coroutines.FlowPreview",
                "-Xopt-in=kotlinx.serialization.ExperimentalSerializationApi",
                "-Xopt-in=coil.annotation.ExperimentalCoilApi",
                "-Xopt-in=kotlin.uuid.Uuid",
        )
    }

    lint {
        abortOnError = true
        disable.addAll(listOf("InvalidPackage"))
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

// This prevents a BuildConfig from being created.
tasks.withType<GenerateBuildConfig> {
    isEnabled = false
}

dependencies {
    // Android
    coreLibraryDesugaring(libs.android.desugar)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.process)
    compileOnly(libs.androidx.room.common)
    compileOnly(libs.androidx.datastorePrefs)

    // Firebase
    compileOnly(libs.google.firebase.analytics)
    compileOnly(libs.google.firebase.auth)
    compileOnly(libs.google.firebase.config)
    compileOnly(libs.google.firebase.crashlytics)
    compileOnly(libs.google.firebase.firestore)

    // Code
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization.json)
    implementation(libs.kotlin.datetime)
    implementation(libs.okio)

    // Network
    compileOnly(libs.ktor.client.core)
    compileOnly(libs.ktor.client.logging)

    implementation(libs.okhttp)

    // Logging
    implementation(libs.kermit)

    // Test (Unit)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)
    testImplementation(libs.mockK)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.assertk)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation(libs.ktor.client.okhttp)
    testImplementation(libs.ktor.client.logging)
    testImplementation(libs.ktor.client.serialization)
    testImplementation(libs.ktor.client.resources)
}

// ===== TEST TASKS =====

// create JUnit reports
tasks.withType<Test> {
    useJUnitPlatform()
}

// ===== Detekt =====

// download detekt config file
tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadDetektConfig") {
    download {
        onlyIf { !file("build/config/detektConfig.yml").exists() }
        src("https://raw.githubusercontent.com/jeffdcamp/kmp-commons/master/detekt/detektConfig-latest.yml")
        dest("build/config/detektConfig.yml")
    }
}

// make sure when running detekt, the config file is downloaded
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    // Target version of the generated JVM bytecode. It is used for type resolution.
    this.jvmTarget = "17"
    dependsOn("downloadDetektConfig")
}

// ./gradlew detekt
detekt {
    allRules = true // fail build on any finding
    buildUponDefaultConfig = true // preconfigure defaults
    config.setFrom(files("$projectDir/build/config/detektConfig.yml")) // point to your custom config defining rules to run, overwriting default behavior
//    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
    }
}

// ===== Maven Deploy =====

// ./gradlew clean check assembleRelease publishReleasePublicationToMavenLocal
// ./gradlew clean check assembleRelease publishReleasePublicationToSoupbowlRepository
// ./gradlew clean check assembleRelease publishReleasePublicationToMavenCentralRepository
publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = Pom.GROUP_ID
            artifactId = Pom.LIBRARY_ARTIFACT_ID
            version = Pom.VERSION_NAME

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set(Pom.LIBRARY_NAME)
                description.set(Pom.POM_DESCRIPTION)
                url.set(Pom.URL)
                licenses {
                    license {
                        name.set(Pom.LICENCE_NAME)
                        url.set(Pom.LICENCE_URL)
                        distribution.set(Pom.LICENCE_DIST)
                    }
                }
                developers {
                    developer {
                        id.set(Pom.DEVELOPER_ID)
                        name.set(Pom.DEVELOPER_NAME)
                    }
                }
                scm {
                    url.set(Pom.SCM_URL)
                    connection.set(Pom.SCM_CONNECTION)
                    developerConnection.set(Pom.SCM_DEV_CONNECTION)
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                val sonatypeNexusUsername: String? by project
                val sonatypeNexusPassword: String? by project
                username = sonatypeNexusUsername ?: ""
                password = sonatypeNexusPassword ?: ""
            }
        }
    }
    repositories {
        maven {
            name = "Soupbowl"
            url = uri("http://192.168.0.5:8082/nexus/content/repositories/releases/")
            credentials {
                val soupbowlNexusUsername: String? by project
                val soupbowlNexusPassword: String? by project
                username = soupbowlNexusUsername ?: ""
                password = soupbowlNexusPassword ?: ""
            }
            isAllowInsecureProtocol = true
        }
    }
}

signing {
    sign(publishing.publications["release"])
}
