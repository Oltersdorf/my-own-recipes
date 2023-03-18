plugins {
    id("kotlin-setup")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":common:database"))
            }
        }
    }
}