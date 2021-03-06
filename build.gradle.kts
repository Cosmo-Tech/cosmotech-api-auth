// Copyright (c) Cosmo Tech.
// Licensed under the MIT license.
import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import pl.allegro.tech.build.axion.release.domain.TagNameSerializationConfig

plugins {
    val kotlinVersion = "1.7.0"
    kotlin("jvm") version kotlinVersion
    id("com.diffplug.spotless") version "6.4.2"
    id("io.gitlab.arturbosch.detekt") version "1.19.0"
    id("pl.allegro.tech.build.axion-release") version "1.13.6"
    `maven-publish`
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

scmVersion { tag(closureOf<TagNameSerializationConfig> { prefix = "" }) }

val kotlinJvmTarget = 17

java { toolchain { languageVersion.set(JavaLanguageVersion.of(kotlinJvmTarget)) } }

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.Cosmo-Tech"
            artifactId = "cosmotech-api-auth"
            version = scmVersion.version

            from(components["java"])
        }
    }
}

repositories {
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

configure<SpotlessExtension> {
    isEnforceCheck = false

    val licenseHeaderComment =
        """
        // Copyright (c) Cosmo Tech.
        // Licensed under the MIT license.
      """.trimIndent()

    java {
        googleJavaFormat()
        target("**/*.java")
        licenseHeader(licenseHeaderComment)
    }
    kotlin {
        ktfmt("0.30")
        target("**/*.kt")
        licenseHeader(licenseHeaderComment)
    }
    kotlinGradle {
        ktfmt("0.30")
        target("**/*.kts")
        //      licenseHeader(licenseHeaderComment, "import")
    }
}

tasks.withType<Detekt>().configureEach {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.from(file("$rootDir/.detekt/detekt.yaml"))
    jvmTarget = kotlinJvmTarget.toString()
    ignoreFailures = project.findProperty("detekt.ignoreFailures")?.toString()?.toBoolean() ?: false
    // Specify the base path for file paths in the formatted reports.
    // If not set, all file paths reported will be absolute file path.
    // This is so we can easily map results onto their source files in tools like GitHub Code
    // Scanning
    basePath = rootDir.absolutePath
    reports {
        html {
            // observe findings in your browser with structure and code snippets
            required.set(true)
            outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.html"))
        }
        xml {
            // checkstyle like format mainly for integrations like Jenkins
            required.set(false)
            outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.xml"))
        }
        txt {
            // similar to the console output, contains issue signature to manually edit baseline files
            required.set(true)
            outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.txt"))
        }
        sarif {
            // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations
            // with Github Code Scanning
            required.set(true)
            outputLocation.set(file("$buildDir/reports/detekt/${project.name}-detekt.sarif"))
        }
    }
}

tasks.jar {
    manifest {
        attributes(
            mapOf("Implementation-Title" to project.name, "Implementation-Version" to project.version))
    }
}
// Dependencies version
// Implementation
val cosmotechApiCommonVersion = "0.1.2-SNAPSHOT"
val springOauthVersion = "5.7.1"
val springSecurityJwtVersion = "1.1.1.RELEASE"
val springOauthAutoConfigureVersion = "2.6.8"
val azureSpringBootBomVersion = "3.14.0"
val zalandoSpringProblemVersion = "0.27.0"
val azureSDKBomVersion = "1.2.0"

dependencies {
    // Workaround until Detekt adds support for JVM Target 17
    // See https://github.com/detekt/detekt/issues/4287
    detekt("io.gitlab.arturbosch.detekt:detekt-cli:1.19.0")
    detekt("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.6.21")

    api("com.github.Cosmo-Tech:cosmotech-api-common:$cosmotechApiCommonVersion")
    implementation("com.azure:azure-identity:1.5.2")
    implementation("com.azure.spring:azure-spring-boot-starter-active-directory:4.0.0")
    implementation("com.azure.spring:azure-spring-boot:$azureSpringBootBomVersion")
    implementation("org.springframework.boot:spring-boot-starter-security:2.7.1")
    implementation(
        "org.springframework.security.oauth.boot:spring-security-oauth2-autoconfigure:${springOauthAutoConfigureVersion}")
    implementation("org.springframework.security:spring-security-jwt:${springSecurityJwtVersion}")
    implementation("org.springframework.security:spring-security-oauth2-jose:${springOauthVersion}")
    implementation("org.zalando:problem-spring-web-starter:${zalandoSpringProblemVersion}")
    implementation(
        "org.springframework.security:spring-security-oauth2-resource-server:${springOauthVersion}")
}
