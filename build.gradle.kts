import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.versions)
    alias(libs.plugins.dependencyAnalysis)
}

allprojects {
    // Gradle Dependency Check
    apply(plugin = "com.github.ben-manes.versions") // ./gradlew dependencyUpdates -Drevision=release
    val excludeVersionContaining = listOf("alpha", "eap", "dev") // example: "alpha", "beta"
    val ignoreArtifacts = emptyList<String>() // some artifacts may be OK to check for "alpha"... add these exceptions here

    tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
        resolutionStrategy {
            componentSelection {
                all {
                    if (ignoreArtifacts.contains(candidate.module).not()) {
                        val rejected = excludeVersionContaining.any { qualifier ->
                            candidate.version.matches(Regex("(?i).*[.-]$qualifier[.\\d-+]*"))
                        }
                        if (rejected) {
                            reject("Release candidate")
                        }
                    }
                }
            }
        }
    }
}

// ===== Dependency Analysis =====
// ./gradlew projectHealth
dependencyAnalysis {
    issues {
        all {
            onAny {
                ignoreKtx(true)
                severity("fail")
            }
            onUnusedDependencies {}
            onUsedTransitiveDependencies { severity("ignore") }
            onIncorrectConfiguration { severity("ignore") }
            onCompileOnly { severity("ignore") }
            onRuntimeOnly { severity("ignore") }
            onUnusedAnnotationProcessors { }
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
