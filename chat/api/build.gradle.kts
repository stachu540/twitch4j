dependencies {
	api(project(":core"))
}

publishing.publications.withType<MavenPublication> {
	pom {
		name.set("Twitch4J Chat API")
		description.set("Rest API for Chat.")
	}
}
