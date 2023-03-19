plugins {
    id("kotlin-setup")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:database"))
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlin)
                implementation(Deps.ArkIvanov.MVIKotlin.rx)
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlinExtensionsCoroutines)
                implementation(Deps.ArkIvanov.Decompose.decompose)
            }
        }
        commonTest {
            dependencies {
                implementation(Deps.ArkIvanov.MVIKotlin.mvikotlinMain)
            }
        }
    }
}