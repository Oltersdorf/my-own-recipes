object Deps {
    object JetBrains {
        object Kotlin {
            private val VERSION = "1.8.0"
            val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$VERSION"
        }

        object Compose {
            private val VERSION = "1.3.0"
            val gradlePlugin = "org.jetbrains.compose:compose-gradle-plugin:$VERSION"
        }
    }

    object Android {
        object Tools {
            object Build {
                const val gradlePlugin = "com.android.tools.build:gradle:7.2.0"
            }
        }
    }

    object Squareup {
        object SQLDelight {
            private const val VERSION = "1.5.5"
            const val gradlePlugin = "com.squareup.sqldelight:gradle-plugin:$VERSION"
        }
    }
}