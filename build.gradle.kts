plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "com.thunderbase.tg"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/io.projectreactor/reactor-core
    implementation("io.projectreactor:reactor-core:3.6.10")

    // https://mvnrepository.com/artifact/jakarta.servlet/jakarta.servlet-api
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")

    // https://mvnrepository.com/artifact/io.projectreactor.netty/reactor-netty-core
    implementation("io.projectreactor.netty:reactor-netty-core:1.1.22")
    // https://mvnrepository.com/artifact/io.projectreactor.netty/reactor-netty-http
    implementation("io.projectreactor.netty:reactor-netty-http:1.1.22")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")

    // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
    implementation("org.slf4j:slf4j-simple:2.0.16")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.0")

    // https://mvnrepository.com/artifact/com.typesafe/config
    implementation("com.typesafe:config:1.4.3")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.thunderbase.tg.Run") // Replace with your main class
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("tg-bot-notifier-server-$version")
    archiveClassifier.set("")
    archiveVersion.set("")
}

tasks.test {
    useJUnitPlatform()
}