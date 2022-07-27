plugins {
  id("org.jetbrains.kotlin.jvm") version "1.6.20"
  application
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk9:1.6.2")
  implementation("org.testcontainers:postgresql:1.17.2")
  implementation("org.postgresql:r2dbc-postgresql:0.9.1.RELEASE")
  implementation("org.jooq:jooq:3.17.2")
  implementation("org.jooq:jooq-kotlin:3.17.2")
  implementation("org.jooq:jooq-kotlin-coroutines:3.17.2")
  runtimeOnly("ch.qos.logback:logback-classic:1.2.11")
}

application {
  mainClass.set("ca.cutterslade.jooq.alreadyresumedbug.Already_resumedKt")
}
