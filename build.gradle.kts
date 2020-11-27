plugins {
  kotlin("multiplatform") version "1.4.20"
}

group = "me.angrybyte.kotlin"
version = "0.1"
val artifact = "tema"

repositories {
  mavenCentral()
}

kotlin {

  val hostOs = System.getProperty("os.name")
  val nativeTarget = when {
    hostOs == "Mac OS X" -> macosX64("native")
    hostOs == "Linux" -> linuxX64("native")
    hostOs.startsWith("Windows") -> mingwX64("native")
    else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
  }

  nativeTarget.apply {
    binaries {
      executable(artifact) {
        entryPoint = "main"
      }
    }
  }

  @Suppress("UNUSED_VARIABLE")
  sourceSets {
    val nativeMain by getting
    val nativeTest by getting
  }

}
