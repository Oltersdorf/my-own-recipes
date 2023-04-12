plugins {
    id("kotlin-setup")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:api"))
                implementation(project(":common:utils"))
                implementation(Deps.JetBrains.Kotlin.coroutines)
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlin)
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlinExtensionsCoroutines)
                implementation(Deps.ArkIvanov.Decompose.decompose)
            }
        }
        commonTest {
            dependencies {
                implementation(project(":common:database"))
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlinMain)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.testCoroutines)
                implementation(Deps.Squareup.SQLDelight.sqliteDriver)
            }
        }
    }
}