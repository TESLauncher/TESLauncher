group = "me.theentropyshard"
version = "0.12.2"
description = "TESLauncher"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
    id("edu.sc.seis.launch4j") version "3.0.5"
}

dependencies {
    implementation(libs.com.formdev.flatlaf)
    implementation(libs.com.google.code.gson.gson)
    implementation(libs.net.lingala.zip4j.zip4j)
    implementation(libs.org.apache.commons.commons.text)
    implementation(libs.com.beust.jcommander)
    implementation(libs.com.squareup.okhttp3.okhttp)
    implementation(libs.net.java.dev.jna.jna)
    implementation(libs.net.java.dev.jna.jna.platform)
    implementation(libs.org.apache.logging.log4j.log4j.api)
    implementation(libs.org.apache.logging.log4j.log4j.core)

    testImplementation(libs.org.junit.jupiter.junit.jupiter.api)
    testImplementation(libs.org.junit.jupiter.junit.jupiter.engine)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass = "me.theentropyshard.teslauncher.Main"
    applicationDefaultJvmArgs = listOf(
        "-Djna.nosys=true"
    )
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

launch4j {
    mainClassName = application.mainClass
    outfile = "${project.name}-${project.version}.exe"
    copyConfigurable = emptyArray<Any>()
    setJarTask(project.tasks.shadowJar.get())
    jreMinVersion = "${project.java.targetCompatibility}"
    version = "${project.version}"
    textVersion = "${project.version}"
    copyright = "2023-2024 ${project.name}"
    companyName = project.name
    fileDescription = "A simple Minecraft launcher"
    jvmOptions = listOf(
        "-Djna.nosys=true"
    )
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
