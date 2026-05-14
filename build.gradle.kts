plugins {
    kotlin("multiplatform") version "2.1.21"
}

repositories {
    mavenCentral()
}

kotlin {
    linuxX64("native") {
        binaries {
            executable {
                entryPoint = "net.kigawa.dilot.main"
            }
        }
    }
    mingwX64 {
        binaries {
            executable {
                entryPoint = "net.kigawa.dilot.main"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
