plugins {
    id("kotlin-multiplatform")
}

kotlin {
    jvm("desktop")

    sourceSets {
        named("commonTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testCommon)
                implementation(Deps.JetBrains.Kotlin.testCommonAnnotation)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testJunit)
            }
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
