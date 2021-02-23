dependencies {
	api("com.fasterxml.jackson.core:jackson-core")
	api("com.fasterxml.jackson.core:jackson-annotations")
	api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

	api("io.github.resilience4j:resilience4j-all")

	api("com.squareup.okhttp3:okhttp")

	// reactive streams
	implementation("io.reactivex.rxjava3:rxjava:3.0.10")
	implementation("io.projectreactor.addons:reactor-extra")
	implementation("io.projectreactor:reactor-core")

	testImplementation("com.squareup.okhttp3:mockwebserver")
}

publishing.publications.withType<MavenPublication> {
	pom {
		name.set("Twitch4J Core")
		description.set("Core dependency with OAuth companion.")
	}
}
