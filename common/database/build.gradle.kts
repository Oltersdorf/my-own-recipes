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
                implementation(Deps.Badoo.Reaktive.reaktive)
            }
        }
        named("desktopTest") {
            dependencies {
                implementation(Deps.Badoo.Reaktive.reaktiveTesting)
                implementation(Deps.Squareup.SQLDelight.sqliteDriver)
            }
        }
    }
}