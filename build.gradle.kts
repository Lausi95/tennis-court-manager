plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.5.5"
  id("io.spring.dependency-management") version "1.1.7"
}

group = "de.lausi"
version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-actuator")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("com.squareup.retrofit2:retrofit:2.11.0")
  implementation("com.squareup.retrofit2:converter-jackson:2.11.0")
  implementation("io.github.serpro69:kotlin-faker:1.16.0")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.bootJar {
  archiveFileName = "application.jar"
}
