import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

    plugins {
    kotlin("jvm") version "2.0.0"
    application
    kotlin("plugin.serialization") version "1.8.22"
}


application {
    mainClass.set("com.example.MainKt")
}


repositories {
    mavenCentral()
}


val ktorVersion = "2.3.4"
val exposedVersion = "0.41.1"


dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    implementation("at.favre.lib:bcrypt:0.9.0")

}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

    tasks.jar {
        manifest {
            attributes["Main-Class"] = "com.example.MainKt"
        }

        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(configurations.runtimeClasspath.get().map {
            if (it.isDirectory) it
            else zipTree(it).matching {
                exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
            }
        })
    }

