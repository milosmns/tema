import Build_gradle.OS.Type.*
import com.github.breadmoirai.githubreleaseplugin.ChangeLogSupplier
import java.io.File.separatorChar
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.joda.time.Instant
import org.joda.time.format.DateTimeFormat

// region Build script setup

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("joda-time:joda-time:2.+")
    classpath("org.jlleitschuh.gradle:ktlint-gradle:10.+")
  }
}

plugins {
  kotlin("multiplatform") version "1.8.0"
  id("com.github.breadmoirai.github-release") version "2.+"
}

repositories {
  mavenCentral()
}

// endregion

// region Project configuration

object Project {
  val group = Env.get("PROJECT_GROUP", default = "xyz.marinkovic.milos")
  val artifact = Env.get("PROJECT_ARTIFACT", default = "tema")
  val version = Env.get("PROJECT_VERSION", default = "2.0.0")
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

    val commonMain by getting

    val commonTest by getting {
      dependsOn(commonMain)
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    val nativeMain by getting {
      dependsOn(commonMain)
    }

    val nativeTest by getting {
      dependsOn(nativeMain)
      dependsOn(commonTest)
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

githubRelease {
  val writeToken = Env.get("GITHUB_TOKEN")
  if (writeToken == Env.INVALID) println("Set 'GITHUB_TOKEN' environment variable to enable GitHub releases")

  val commitish = Env.get("GITHUB_SHA", default = "local")

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
  val tag = "v${Project.version}_$platformSuffix$qualitySuffix"

  val name = "[${OS.current.longName}] $quality ${Project.version} - $nowName"

  token(writeToken)
  owner(Project.author)
  repo(Project.artifact)
  tagName(tag)
  releaseName(name)
  targetCommitish(commitish)
  prerelease(quality != "GA")

  val maxFetched = 20
  val maxReported = 7
  val bullet = "\n* "
  val changelogConfig = closureOf<ChangeLogSupplier> {
    currentCommit("HEAD")
    lastCommit("HEAD~$maxFetched")
    options("--format=oneline", "--abbrev-commit", "--max-count=$maxFetched")
  }
  val ignoredMessagesRegex = setOf(
    "(?i).*bump.*version.*",
    "(?i).*increase.*version.*",
    "(?i).*version.*bump.*",
    "(?i).*version.*increase.*",
    "(?i).*merge.*request.*",
    "(?i).*request.*merge.*",
  ).map(String::toRegex)
  val changes = try {
    changelog(changelogConfig)
      .call()
      .trim()
      .split("\n")
      .map { it.trim() }
      .filterNot { ignoredMessagesRegex.any(it::matches) }
      .take(maxReported)
  } catch (t: Throwable) {
    System.err.println("Failed to fetch history")
    t.printStackTrace(System.err)
    emptyList()
  }

  body(
    when {
      changes.isNotEmpty() -> "## Latest changes\n${changes.joinToString(separator = bullet, prefix = bullet)}"
      else -> "See commit history for latest changes."
    }
  )

  val binaryLocation = "${project.buildDir}${Project.Location.binaryDir}"
  val binaryFile = file("$binaryLocation${Project.artifact}")
  releaseAssets(
    arrayOf(binaryFile)
  )

  println("Configured for upload: '${binaryFile.absolutePath}'")
}
apply(plugin = "com.github.breadmoirai.github-release")
// endregion

// region Helpers

object Env {
  const val INVALID = "<invalid>"
  fun get(
    name: String,
    default: String = INVALID,
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
