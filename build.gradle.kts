plugins {
    id("java-library")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(libs.com.formdev.flatlaf)
    api(libs.com.google.code.gson.gson)
    api(libs.net.lingala.zip4j.zip4j)
    api(libs.org.apache.commons.commons.text)
    api(libs.com.beust.jcommander)
    api(libs.com.squareup.okhttp3.okhttp)
    api(libs.net.java.dev.jna.jna)
    api(libs.net.java.dev.jna.jna.platform)
    api(libs.org.apache.logging.log4j.log4j.api)
    api(libs.org.apache.logging.log4j.log4j.core)

    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
}

tasks.shadowJar {
    archiveClassifier = ""
    archiveBaseName = project.name
    archiveVersion = project.version.toString()

    manifest {
        attributes["Main-Class"] = application.mainClass
        attributes["Implementation-Version"] = project.version.toString()
    }
}

buildConfig {
    packageName("me.theentropyshard.teslauncher")
    className("BuildConfig")
    useJavaOutput()

    buildConfigField("APP_NAME", provider { project.name })
    buildConfigField("APP_VERSION", provider { project.version.toString() })
}

application.mainClass = "me.theentropyshard.teslauncher.Main"

group = "me.theentropyshard"
version = "0.11.0"
description = "TESLauncher"

java.sourceCompatibility = JavaVersion.VERSION_1_8
java.targetCompatibility = JavaVersion.VERSION_1_8

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
