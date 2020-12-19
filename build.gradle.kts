import Build_gradle.OS.Type.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import java.io.File.separatorChar
import java.lang.System.getenv
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

buildscript {
  repositories {
    jcenter()
    mavenCentral()
  }
  dependencies {
    classpath("joda-time:joda-time:2.10.8")
  }
}

plugins {
  kotlin("multiplatform") version "1.4.20"
  id("co.riiid.gradle") version "0.4.2"
}

repositories {
  jcenter()
  mavenCentral()
}

group = "me.angrybyte.kotlin"
version = "1.0.0"
val artifact = "tema"
val sourceName = "native"
val author = "milosmns"
val binaryLocation: String
  get() = "${project.buildDir}${separatorChar}bin${separatorChar}native${separatorChar}temaReleaseExecutable${separatorChar}"

class OS {
  enum class Type(val longName: String) { MAC("Mac OS"), WINDOWS("Windows"), LINUX("Linux") }

  val current: Type
    get() = System.getProperty("os.name")?.let { hostOs ->
      when {
        hostOs == "Mac OS X" -> MAC
        hostOs == "Linux" -> LINUX
        hostOs.startsWith("Windows") -> WINDOWS
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
      }
    } ?: throw GradleException("Host OS is unknown")
}

kotlin {

  when (OS().current) {
    MAC -> macosX64(sourceName)
    LINUX -> linuxX64(sourceName)
    WINDOWS -> mingwX64(sourceName)
  }.apply {
    binaries {
      executable(artifact) {
        entryPoint = "main"
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

tasks {

  fun renameBinary() {
    val extension = when (OS().current) {
      MAC, LINUX -> ".kexe"
      WINDOWS -> ".exe"
    }

    val finalFile = file("$binaryLocation$artifact")
    if (finalFile.exists()) {
      finalFile.delete()
      println("Old binary '${finalFile.absolutePath}' deleted")
    }

    val binaryFile = file("$binaryLocation$artifact$extension")
    if (!binaryFile.exists()) error("Missing binary at ${binaryFile.absolutePath}")

    val copiedFile = binaryFile.copyTo(finalFile)
    if (!copiedFile.exists()) error("Couldn't strip extension at ${copiedFile.absolutePath}")

    println("Stripped '$extension' from binary at '${copiedFile.absolutePath}'")
  }

  fun updateBinaryPermissions() {
    if (OS().current == WINDOWS) return
    val binaryFile = file("$binaryLocation$artifact")
    // make executable and grant all permissions
    binaryFile.setExecutable(true, false)
    Files.setPosixFilePermissions(binaryFile.toPath(), PosixFilePermission.values().toSet())
  }

  val renameBinary by registering {
    description = "Renames the final binary to strip the file extension"
    group = "Publishing"
    doLast {
      renameBinary()
      updateBinaryPermissions()
    }
  }

  named("linkTemaReleaseExecutableNative") {
    finalizedBy(renameBinary)
  }

  named("githubRelease") {
    dependsOn(renameBinary)
  }

  withType<Test> {
    testLogging.exceptionFormat = TestExceptionFormat.FULL
    testLogging.events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
  }

}

github {
  val writeToken = getenv("GITHUB_TOKEN") ?: "invalid"
  if (writeToken == "invalid") println("Set 'GITHUB_TOKEN' environment variable for GitHub releases")
  val platform = OS().current
  val platformSuffix = platform.name.first().toLowerCase()
  val quality = getenv("BUILD_QUALITY") ?: "Debug"
  val nowTag = DateTimeFormat.forPattern("yyyy_MM_dd_HH_mm_ss")
    .withZoneUTC()
    .let { formatter -> Instant.now().toString(formatter) }
  val nowName = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
    .withZoneUTC()
    .let { formatter -> Instant.now().toString(formatter) }
  val qualitySuffix = when (quality) {
    "GA" -> "_ga"
    "PR" -> "_pr_$nowTag"
    else -> "_dev_$nowTag"
  }
  val binaryFile = file("$binaryLocation$artifact")

  println("Configured for upload: '${binaryFile.absolutePath}'")

  owner = author
  repo = artifact
  token = writeToken
  tagName = "v${project.version}_$platformSuffix$qualitySuffix"
  name = "[${platform.longName}] $quality ${project.version} / $nowName"
  isPrerelease = quality != "GA"
  setAssets(binaryFile.path)
}

apply {
  plugin("co.riiid.gradle")
}
