plugins {
    id("kotlin-multiplatform")
    id("com.squareup.sqldelight")
}

sqldelight {
    database("MyOwnRecipes") {
        packageName = "com.olt.mor.database"
    }
}

kotlin {
    jvm("desktop")

    sourceSets {
        commonMain {
            dependencies {
                implementation(Deps.Badoo.Reaktive.reaktive)
            }
        }
        commonTest {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testCommon)
                implementation(Deps.JetBrains.Kotlin.testCommonAnnotation)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testJunit)
                implementation(Deps.Badoo.Reaktive.reaktiveTesting)
                implementation(Deps.Squareup.SQLDelight.sqliteDriver)
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