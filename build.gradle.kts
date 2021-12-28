buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.agp)
        classpath(libs.kotlin.gradle)

        classpath(libs.hilt.gradle)
        classpath(libs.wire.gradle)
    }
}

tasks.register("clean", Delete::class) { delete(rootProject.buildDir) }
