import Build_gradle.OS.Type.*
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat
import java.io.File.separatorChar
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

// region Build script setup

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

// endregion

// region Project configuration

object Project {
  val group = Env.get("PROJECT_GROUP", default = "me.angrybyte.kotlin")
  val artifact = Env.get("PROJECT_ARTIFACT", default = "tema")
  val version = Env.get("PROJECT_VERSION", default = "1.1.0")
  val author = Env.get("PROJECT_AUTHOR", default = "milosmns")

  object Location {
    val binaryDir = "/bin/native/${artifact}ReleaseExecutable/".replace('/', separatorChar)
  }

  object Source {
    const val NATIVE = "native"
  }
}

group = Project.group
version = Project.version

// endregion

// region Modules setup

kotlin {

  when (OS.current) {
    MAC -> macosX64(Project.Source.NATIVE)
    LINUX -> linuxX64(Project.Source.NATIVE)
    WINDOWS -> mingwX64(Project.Source.NATIVE)
  }.apply {
    binaries {
      executable(Project.artifact) {
        entryPoint = "main"
      }
    }
  }

  @Suppress("UNUSED_VARIABLE") // it's all used
  sourceSets {

    val nativeMain by getting

    val nativeTest by getting {
      dependsOn(nativeMain)
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

  }

}

// endregion

// region Tasks setup

tasks {

  fun renameBinary() {
    val extension = when (OS.current) {
      MAC, LINUX -> ".kexe"
      WINDOWS -> ".exe"
    }

    // locate the binary
    val binaryLocation = "${project.buildDir}${Project.Location.binaryDir}"
    val binaryFile = file("$binaryLocation${Project.artifact}$extension").also {
      if (!it.exists()) error("Missing binary at ${it.absolutePath}")
    }
    // strip the extension
    val finalFile = binaryFile.copyTo(
      target = file("$binaryLocation${Project.artifact}"),
      overwrite = true
    ).also {
      if (!it.exists()) error("Couldn't strip extension at ${it.absolutePath}")
    }
    println("Stripped '$extension' from binary at '${finalFile.absolutePath}'")

    if (OS.current == WINDOWS) return

    // make the new binary executable and grant all permissions
    finalFile.setExecutable(true, false)
    Files.setPosixFilePermissions(finalFile.toPath(), PosixFilePermission.values().toSet())
    println("Updated permissions for '${finalFile}'")
  }

  val renameBinary by registering {
    description = "Renames the final binary to strip the file extension"
    group = "Publishing"
    doLast { renameBinary() }
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

// endregion

// region Other plugins

github {
  val writeToken = Env.get("GITHUB_TOKEN")
  if (writeToken == Env.INVALID) println("Set 'GITHUB_TOKEN' environment variable to enable GitHub releases")
  val platformSuffix = OS.current.name.first().toLowerCase()
  val quality = Env.get("BUILD_QUALITY", default = "Debug")
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

  owner = Project.author
  repo = Project.artifact
  token = writeToken
  tagName = "v${Project.version}_$platformSuffix$qualitySuffix"
  name = "[${OS.current.longName}] $quality ${Project.version} - $nowName"
  isPrerelease = quality != "GA"

  val binaryLocation = "${project.buildDir}${Project.Location.binaryDir}"
  val binaryFile = file("$binaryLocation${Project.artifact}")
  setAssets(binaryFile.path)

  println("Configured for upload: '${binaryFile.absolutePath}'")
}

apply {
  plugin("co.riiid.gradle")
}

// endregion

// region Helpers

object Env {
  const val INVALID = "<invalid>"
  fun get(
    name: String,
    default: String = INVALID
  ) = System.getenv(name)
    .takeIf { !it.isNullOrBlank() }
    ?: default
}

object OS {
  enum class Type(val longName: String) { MAC("Mac OS"), WINDOWS("Windows"), LINUX("Linux") }

  val current: Type = System.getProperty("os.name")?.let { hostOs ->
    when {
      hostOs == "Mac OS X" -> MAC
      hostOs == "Linux" -> LINUX
      hostOs.startsWith("Windows") -> WINDOWS
      else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
  } ?: throw GradleException("Host OS is unknown")
}

// endregion