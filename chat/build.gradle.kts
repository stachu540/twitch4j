dependencies {
	api(project(":core"))
}

publishing.publications.withType<MavenPublication> {
	pom {
		name.set("Twitch4J Chat (TMI)")
		description.set("Message Interface component.")
	}
}
