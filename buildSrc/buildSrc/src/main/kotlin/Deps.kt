object Deps {
    object JetBrains {
        object Kotlin {
            private const val VERSION = "1.8.10"
            const val gradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:$VERSION"
            const val testCommon = "org.jetbrains.kotlin:kotlin-test-common:$VERSION"
            const val testJunit = "org.jetbrains.kotlin:kotlin-test-junit5:$VERSION"
            const val testCommonAnnotation = "org.jetbrains.kotlin:kotlin-test-annotations-common:$VERSION"
            private const val XVERSION = "1.6.4"
            const val testCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$XVERSION"
            const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$XVERSION"
        }

        object Compose {
            private const val VERSION = "1.3.0"
            const val gradlePlugin = "org.jetbrains.compose:compose-gradle-plugin:$VERSION"
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
            const val coroutineExtensions = "com.squareup.sqldelight:coroutines-extensions:$VERSION"
        }
    }

    object ArkIvanov {
        object MVIKotlin {
            private const val VERSION = "3.2.0"
            const val rx = "com.arkivanov.mvikotlin:rx:$VERSION"
            const val mvikotlin = "com.arkivanov.mvikotlin:mvikotlin:$VERSION"
            const val mvikotlinMain = "com.arkivanov.mvikotlin:mvikotlin-main:$VERSION"
            const val mvikotlinExtensionsCoroutines = "com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines:$VERSION"
        }

        object Decompose {
            private const val VERSION = "1.0.0"
            const val decompose = "com.arkivanov.decompose:decompose:$VERSION"
        }
    }
}