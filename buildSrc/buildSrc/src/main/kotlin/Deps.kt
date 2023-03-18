object Deps {
    object JetBrains {
        object Kotlin {
            private val VERSION = "1.8.0"
            val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$VERSION"
            val testCommon = "org.jetbrains.kotlin:kotlin-test-common:$VERSION"
            val testJunit = "org.jetbrains.kotlin:kotlin-test-junit5:$VERSION"
            val testCommonAnnotation = "org.jetbrains.kotlin:kotlin-test-annotations-common:$VERSION"
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
            const val sqliteDriver = "com.squareup.sqldelight:sqlite-driver:$VERSION"
        }
    }

    object Badoo {
        object Reaktive {
            private const val VERSION = "1.2.3"
            const val reaktive = "com.badoo.reaktive:reaktive:$VERSION"
            const val reaktiveTesting = "com.badoo.reaktive:reaktive-testing:$VERSION"
        }
    }
}