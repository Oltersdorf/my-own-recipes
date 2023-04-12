plugins {
    id("kotlin-setup")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(Deps.JetBrains.Kotlin.coroutines)
            }
        }
    }
}