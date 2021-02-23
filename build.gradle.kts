plugins {
    signing
    `java-library`
    `maven-publish`
    id("io.freefair.lombok") version "5.3.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

allprojects {
    repositories {
        mavenCentral()
		mavenLocal()
		jcenter()
    }

	tasks {
		withType<JavaCompile> {
			options.encoding = "UTF-8"
		}
		withType<Javadoc> {
			if (JavaVersion.current().isJava9Compatible) {
				(options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
			}
		}
		// prevent to generate 'lombok.config' - more about: https://projectlombok.org/features/configuration
		withType<io.freefair.gradle.plugins.lombok.tasks.GenerateLombokConfig> {
			enabled = false
		}
	}
}

subprojects {
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
	apply(plugin = "java-library")
	apply(plugin = "io.freefair.lombok")
	apply(plugin = "com.github.johnrengelman.shadow")

	// Source Compatibility
	java {
		sourceCompatibility = JavaVersion.VERSION_11 // source code to 11
		targetCompatibility = JavaVersion.VERSION_1_8 // compilation to 1.8
		// TODO modularity for Jigsaw: https://github.com/twitch4j/twitch4j/issues/218
//		modularity.apply {
//			inferModulePath.set(true)
//		}
		withSourcesJar()
		withJavadocJar()
	}

	base {
		archivesBaseName = artifactId
	}

	dependencies {
		// Apache Commons
		api("commons-io:commons-io:2.8.0")
		api("org.apache.commons:commons-lang3:3.11")
		api("org.apache.commons:commons-collections4:4.4")

		// Logging
		api("org.slf4j:slf4j-api:1.7.30")
		//
		implementation("org.jetbrains:annotations:20.1.0")

		// Resilience4J
		implementation(platform("io.github.resilience4j:resilience4j-bom:1.7.0"))

		// Jackson BOM
		implementation(platform("com.fasterxml.jackson:jackson-bom:2.12.1"))

		// OkHttp
		implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.0"))

		// Reactor
		implementation(platform("io.projectreactor:reactor-bom:2020.0.4"))

		// Test
		testImplementation(platform("org.junit:junit-bom:5.7.0"))
		testImplementation("org.junit.jupiter:junit-jupiter")
		testImplementation("ch.qos.logback:logback-classic:1.2.3")

	}

    val maven by publishing.publications.creating(MavenPublication::class) {
        from(components["java"])
        artifactId = project.artifactId
		pom {
			packaging = "jar"
		}
    }

    signing {
        sign(maven)
    }

    publishing {
		repositories {
			maven {
				val releaseUri = "https://oss.sonatype.org/content/repositories/releases"
				val snapshotUri = "https://oss.sonatype.org/content/repositories/releases"
				name = "Nexus"
				setUrl(if (project.isSnapshot) snapshotUri else releaseUri)
				credentials {
					username = project.nexusUser
					password = project.nexusPassword
				}
			}
		}
    }

	tasks {
		val delombok by getting(io.freefair.gradle.plugins.lombok.tasks.Delombok::class)
		val jar by getting(Jar::class)
		val relocateShadowJar by creating(com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation::class) {
			target = shadowJar.get()
			prefix = "com.github.twitch4j.shaded.${"$version".replace(".", "_")}"
		}

		javadoc {
			dependsOn(delombok)
			source(delombok)
			options {
				title = "${project.artifactId} (v${project.version})"
				windowTitle = "${project.artifactId} (v${project.version})"
				encoding = "UTF-8"
			}
		}

		shadowJar {
			dependsOn(relocateShadowJar)
			archiveClassifier.set("shaded")
			manifest.inheritFrom(jar.manifest)
		}

		test {
			useJUnitPlatform {
				includeTags("unittest")
				excludeTags("integration")
			}
		}
	}
}

apply<Github>()
