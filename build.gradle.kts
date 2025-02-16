import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    application
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.spring") version "2.0.10"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.10.4"
}

group = "santannaf.demo.brc.redis-reactive"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive") {
        implementation("redis.clients:jedis")
    }
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

application {
    mainClass.set("santannaf.demo.brc.demoredisreactive.ApplicationKt")
}

tasks.bootJar {
    mainClass.set("santannaf.demo.brc.demoredisreactive.ApplicationKt")
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("my_app")
            mainClass.set("santannaf.demo.brc.demoredisreactive.ApplicationKt")
            buildArgs.add("--native-image-info")
            buildArgs.add("--enable-preview")
            buildArgs.add("--color=always")
            buildArgs.add("--verbose")
            buildArgs.add("-g")
            buildArgs.add("--parallelism=5")
            buildArgs.add("-march=compatibility")
            buildArgs.add("-O3")
            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
        }
    }
}