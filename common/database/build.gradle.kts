plugins {
    id("kotlin-setup")
    id("com.squareup.sqldelight")
}

sqldelight {
    database("MyOwnRecipes") {
        packageName = "com.olt.mor.database"
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:api"))
                implementation(Deps.Squareup.SQLDelight.coroutineExtensions)
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