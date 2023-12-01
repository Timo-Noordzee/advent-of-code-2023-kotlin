plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.9"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.9.21"
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.9")
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.5"
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    configurations {
        for (day in 1..25) {
            register("day${day.toString().padStart(2, '0')}") {
                include("Day${day.toString().padStart(2, '0')}")
            }
        }
    }

    targets {
        register("main")
    }
}