plugins {
    id("kotlin-setup")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlin)
                implementation(Deps.ArkIvanov.MVIKotlin.rx)
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlinExtensionsCoroutines)
                implementation(Deps.ArkIvanov.Decompose.decompose)
                implementation(Deps.JetBrains.Kotlin.coroutines)
            }
        }
    }
}