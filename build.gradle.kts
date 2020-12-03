plugins {
  kotlin("multiplatform") version "1.4.20"
}

group = "me.angrybyte.kotlin"
version = "0.1"
val artifact = "tema"
val sourceName = "native"

repositories {
  jcenter()
  mavenCentral()
}

kotlin {

  System.getProperty("os.name").let { hostOs ->
    when {
      hostOs == "Mac OS X" -> macosX64(sourceName)
      hostOs == "Linux" -> linuxX64(sourceName)
      hostOs.startsWith("Windows") -> mingwX64(sourceName)
      else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }.apply {
      binaries {
        executable(artifact) {
          entryPoint = "main"
        }
      }
    }
  }

  sourceSets {

    @Suppress("UNUSED_VARIABLE") // reflect
    val nativeMain by getting

    @Suppress("UNUSED_VARIABLE") // reflect
    val nativeTest by getting {
      dependsOn(nativeMain)
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

  }

}